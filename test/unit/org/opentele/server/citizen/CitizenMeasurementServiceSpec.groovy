package org.opentele.server.citizen

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.opentele.builders.CompletedQuestionnaireBuilder
import org.opentele.builders.MeasurementBuilder
import org.opentele.builders.MeasurementTypeBuilder
import org.opentele.builders.PatientBuilder
import org.opentele.server.core.I18nService
import org.opentele.server.core.util.TimeFilter
import org.opentele.server.model.*
import org.opentele.server.model.cgm.*
import org.opentele.server.model.types.cgm.*
import org.opentele.server.model.patientquestionnaire.CompletedQuestionnaire
import org.opentele.server.model.patientquestionnaire.MeasurementNodeResult
import org.opentele.server.model.patientquestionnaire.PatientBooleanNode
import org.opentele.server.model.patientquestionnaire.PatientQuestionnaire
import org.opentele.server.model.questionnaire.BooleanNode
import org.opentele.server.model.questionnaire.QuestionnaireHeader
import org.opentele.server.core.model.types.MeasurementTypeName

import spock.lang.Specification

@TestFor(CitizenMeasurementService)
@Build([Patient, Measurement, MeasurementNodeResult, QuestionnaireHeader,
        CompletedQuestionnaire, MeasurementType, PatientQuestionnaire,
        Clinician, PatientBooleanNode, BooleanNode, ContinuousBloodSugarEvent,
        ContinuousBloodSugarMeasurement, CoulometerReadingEvent, HyperAlarmEvent,
        HypoAlarmEvent, ImpendingHyperAlarmEvent, ImpendingHypoAlarmEvent,
        MealEvent, StateOfHealthEvent, ExerciseEvent, InsulinEvent, GenericEvent])
@TestMixin(GrailsUnitTestMixin)
class CitizenMeasurementServiceSpec extends Specification {

    Patient patient
    CompletedQuestionnaire completedQuestionnaire
    MeasurementType bloodPressureMeasurementType
    MeasurementType ctgMeasurementType
    MeasurementType pulseMeasurementType
    MeasurementType lungFunctionMeasurementType
    MeasurementType bloodsugarMeasurementType
    MeasurementType continuousBloodSugarMeasurementType

    def setup() {
        // Ugly hack to avoid a null pointer exception, since the springSecurityService can not be injected
        service.i18nService = Mock(I18nService)

        // The encodeAsJavaScript() method is not added to String in unit tests, and it is used within MeasurementService.
        // In fact, the use of encodeAsJavaScript SHOULD probably not happen in the service, but related controllers...
        String.metaClass.encodeAsJavaScript { it }

        patient = new PatientBuilder().build()
        completedQuestionnaire = new CompletedQuestionnaireBuilder()
                .forPatient(patient).build()

        bloodPressureMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.BLOOD_PRESSURE).build()
        ctgMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.CTG).build()
        pulseMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.PULSE).build()
        lungFunctionMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.LUNG_FUNCTION).build()
        bloodsugarMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.BLOODSUGAR).build()
        continuousBloodSugarMeasurementType = new MeasurementTypeBuilder()
                .ofType(MeasurementTypeName.CONTINUOUS_BLOOD_SUGAR_MEASUREMENT).build()

        patient.save()
        patient.thresholds.add(new BloodPressureThreshold(
                type: bloodPressureMeasurementType,
                systolicAlertLow: 70,
                systolicAlertHigh: 190,
                systolicWarningHigh: 180,
                systolicWarningLow: 80,
                diastolicAlertHigh: 100,
                diastolicWarningHigh: 90,
                diastolicWarningLow: 50,
                diastolicAlertLow: 40))
    }

    def "only gives table data for measurement types with data"() {
        setup:
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .inQuestionnaire(completedQuestionnaire).build()

        when:
        def tableData = service.dataForTables(patient, TimeFilter.all())

        then:
        tableData.size() == 1
        tableData[0].type == MeasurementTypeName.BLOOD_PRESSURE
    }

    void "gives all data for tables including ignored measurements"() {
        setup:
        9.times { new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .inQuestionnaire(completedQuestionnaire).build() }
        new MeasurementBuilder().ignored(true).ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .inQuestionnaire(completedQuestionnaire).build()
        new MeasurementBuilder().ofType(MeasurementTypeName.CTG)
                .inQuestionnaire(completedQuestionnaire).build()

        when:
        def tableData = service.dataForTables(patient, TimeFilter.all())
        and:
        def bloodPressureMeasurements = tableData[0]

        then:
        tableData.size() == 1
        bloodPressureMeasurements.type == MeasurementTypeName.BLOOD_PRESSURE
        bloodPressureMeasurements.measurements.size == 10
    }

    void "can get list of raw measurements of specific type"() {
        setup:
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .inQuestionnaire(completedQuestionnaire).build()
        new MeasurementBuilder().ofType(MeasurementTypeName.PULSE)
                .inQuestionnaire(completedQuestionnaire).build()
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .inQuestionnaire(completedQuestionnaire).build()

        when:
        List<Measurement> bloodPressureMeasurements = service.patientMeasurementsOfType(
                patient, TimeFilter.all(), MeasurementTypeName.BLOOD_PRESSURE)
        List<Measurement> pulseMeasurement = service.patientMeasurementsOfType(
                patient, TimeFilter.all(), MeasurementTypeName.PULSE)

        then:
        bloodPressureMeasurements.size() == 2
        bloodPressureMeasurements.findAll { m ->
            m.measurementType.name == MeasurementTypeName.PULSE }.size() == 0

        pulseMeasurement.size() == 1
        pulseMeasurement.findAll { m ->
            m.measurementType.name == MeasurementTypeName.BLOOD_PRESSURE }.size() == 0
    }

    void "list of raw measurements of specific type is sorted newest first"() {
        setup:
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .atTime(2014, 1, 1, 9, 40).inQuestionnaire(completedQuestionnaire).build()
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .atTime(2014, 1, 1, 12, 40).inQuestionnaire(completedQuestionnaire).build()
        new MeasurementBuilder().ofType(MeasurementTypeName.BLOOD_PRESSURE)
                .atTime(2014, 1, 1, 11, 40).inQuestionnaire(completedQuestionnaire).build()

        when:
        List<Measurement> measurements = service.patientMeasurementsOfType(
                patient, TimeFilter.all(), MeasurementTypeName.BLOOD_PRESSURE)

        then:
        measurements.size() == 3
        measurements[0].time > measurements[1].time
        measurements[1].time > measurements[2].time
    }

    void "if no raw measurements of specific type is found an empty list is returned"() {
        when:
        def measurements = service.patientMeasurementsOfType(
                patient, TimeFilter.all(), MeasurementTypeName.BLOOD_PRESSURE)

        then:
        measurements.size() == 0
    }

}
