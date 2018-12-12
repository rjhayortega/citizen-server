package org.opentele.server.citizen

import org.opentele.server.citizen.measurement.TableData
import org.opentele.server.citizen.measurement.TableMeasurement
import org.opentele.server.core.model.types.MeasurementTypeName
import org.opentele.server.core.util.TimeFilter
import org.opentele.server.model.*
import org.opentele.server.model.cgm.*

class CitizenMeasurementService {

    def i18nService

    def patientMeasurementsOfType(Patient patient, TimeFilter timeFilter, MeasurementTypeName type) {
        def measurements = []

        def typeToProcess = MeasurementType.list().find {t -> t.name == type}
        processMeasurements(patient, timeFilter, typeToProcess) {measurementsOfType ->
            measurements.addAll(measurementsOfType)
        }

        measurements.reverse()
    }

    def dataForTables(Patient patient, TimeFilter timeFilter) {
        def tableData = []

        MeasurementType.list().each { type ->
            processMeasurements(patient, timeFilter, type) { List<Measurement> measurementsOfType ->
                addTableData(type, measurementsOfType, tableData)
            }
        }
	
        def nonCTGTables = tableData.findAll{it.type.toString() != 'CTG'} //We cannot meaningfully show CTG data
        nonCTGTables
    }

    private static void processMeasurements(Patient patient, TimeFilter timeFilter,
                                     MeasurementType type, Closure closure) {
        List<Measurement> measurementsOfType = findMeasurements(patient, type, timeFilter)
        if (measurementsOfType) {
            closure.call(measurementsOfType)
        }
    }

    private static void addTableData(MeasurementType type,
                                     List<Measurement> measurementsOfType,
                                     List tableDataList) {
        tableDataList << createTableData(type.name, measurementsOfType)
    }

    private static TableData createTableData(MeasurementTypeName type,
                                             List<Measurement> measurements) {
        def tableMeasurementsNewestFirst = measurements.collect { measurement ->
            new TableMeasurement(measurement)
        }.reverse()

        new TableData(type: type, measurements: tableMeasurementsNewestFirst)
    }

    private static List<Measurement> findMeasurements(Patient patient, MeasurementType type,
                                                      TimeFilter timeFilter) {

        if (type.name == MeasurementTypeName.CONTINUOUS_BLOOD_SUGAR_MEASUREMENT) {
            if (timeFilter.isLimited) {
                def measurements = Measurement.findAllByPatientAndMeasurementType(
                        patient, type, [sort: "time", order: "desc"])
                def cgmMeasurements = ContinuousBloodSugarEvent.findAllByMeasurementInListAndTimeBetween(
                        measurements, timeFilter.start, timeFilter.end, [sort: "time", order: "desc"])

                cgmMeasurements*.measurement.unique()
            } else {
                def measurements = Measurement.findAllByPatientAndMeasurementType(
                        patient, type, [sort: "time", order: "desc"])
                measurements.findAll {
                    !it.continuousBloodSugarEvents.isEmpty()
                }
            }
        } else {
            if (timeFilter.isLimited) {
                Measurement.findAllByPatientAndMeasurementTypeAndTimeBetween(
                        patient, type, timeFilter.start, timeFilter.end, [sort: "time"])
            } else {
                Measurement.findAllByPatientAndMeasurementType(patient, type, [sort: "time"])
            }
        }
    }
}
