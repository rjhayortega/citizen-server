package org.opentele.server.citizen


import org.opentele.server.core.model.types.BloodInUrineValue
import org.opentele.server.core.model.types.LeukocytesInUrineValue
import org.opentele.server.core.model.types.NitriteInUrineValue
import org.opentele.server.model.*
import org.opentele.server.core.model.types.GlucoseInUrineValue
import org.opentele.server.core.model.types.MeasurementTypeName
import org.opentele.server.core.model.types.ProteinValue
import org.opentele.server.core.model.types.Unit

class ConsultationService {

    def Consultation addConsultation(Patient patient, Clinician clinician, params) {

        Date now = new Date()
        Consultation consultation = new Consultation(clinician: clinician,
                patient: patient, consultationDate: now)

        if (!params) {
            return consultation
        }

        checkWeight(params, patient, now, consultation)
        checkBloodPressure(params, patient, now, consultation)
        checkSaturation(params, patient, now, consultation)
        checkLungFunction(params, patient, now, consultation)
        checkUrine(params, patient, now, consultation)
        checkBloodSugar(params, patient, now, consultation)
        print(consultation.measurements);
        print(patient)
        print(params);
     //   if (consultation.measurements) {
            consultation.save(flush: true)
     //   }

        consultation
    }

    private static void checkWeight(params, Patient patient, Date now, Consultation consultation) {
        if (params.showWEIGHT) {
            addMeasurement(consultation, patient, now, Unit.KILO, MeasurementTypeName.WEIGHT,
                    ['value': getDouble(params.weight as String)])
        }
    }

    private static void checkBloodPressure(params, Patient patient, Date now, Consultation consultation) {
      //  if (params.showBLOOD_PRESSURE) {
                print("ADDING NOW")
            addMeasurement(consultation, patient, now, Unit.MMHG, MeasurementTypeName.BLOOD_PRESSURE,
                    ['systolic': getDouble(params.systolic as String),
                     'diastolic': getDouble(params.diastolic as String)])

            addMeasurement(consultation, patient, now, Unit.BPM, MeasurementTypeName.PULSE,
                    ['value': getDouble(params.pulse as String)])
       // }
    }
    
    

    private static void checkSaturation(params, Patient patient, Date now, Consultation consultation) {
        if (params.showSATURATION) {
            addMeasurement(consultation, patient, now, Unit.PERCENTAGE, MeasurementTypeName.SATURATION,
                    ['value': getDouble(params.saturation as String)])

            addMeasurement(consultation, patient, now, Unit.BPM, MeasurementTypeName.PULSE,
                    ['value': getDouble(params.saturationPuls as String)])
        }
    }

    private static void checkLungFunction(params, Patient patient, Date now, Consultation consultation) {
        if (params.showLUNG_FUNCTION) {
            addMeasurement(consultation, patient, now, Unit.LITER, MeasurementTypeName.LUNG_FUNCTION,
                    ['value': getDouble(params.lungfunction as String)])
        }
    }

    private static void checkUrine(params, Patient patient, Date now, Consultation consultation) {
        if (params.showURINE_COMBI) {
            addMeasurement(consultation, patient, now, Unit.PROTEIN, MeasurementTypeName.URINE,
                    ['protein': ProteinValue.fromString(params.urine as String)])

            addMeasurement(consultation, patient, now, Unit.GLUCOSE, MeasurementTypeName.URINE_GLUCOSE,
                    ['glucoseInUrine': GlucoseInUrineValue.fromString(params.urine_glucose as String)])

            addMeasurement(consultation, patient, now, Unit.BLOOD, MeasurementTypeName.URINE_BLOOD,
                    ['bloodInUrine': BloodInUrineValue.fromString(params.urine_blood as String)])

            addMeasurement(consultation, patient, now, Unit.NONE, MeasurementTypeName.URINE_NITRITE,
                    ['nitriteInUrine': NitriteInUrineValue.fromString(params.urine_nitrite as String)])

            addMeasurement(consultation, patient, now, Unit.LEUKOCYTES, MeasurementTypeName.URINE_LEUKOCYTES,
                    ['leukocytesInUrine': LeukocytesInUrineValue.fromString(params.urine_leukocytes as String)])
        }
    }

    private static void checkBloodSugar(params, Patient patient, Date now, Consultation consultation) {
        if (params.showBLOODSUGAR) {
            addMeasurement(consultation, patient, now, Unit.MMOL_L, MeasurementTypeName.BLOODSUGAR,
                    ['value': getDouble(params.bloodsugar as String)])
        }
    }

    private static addMeasurement(Consultation consultation, Patient patient,
                                  Date now, Unit unit,
                                  MeasurementTypeName measurementTypeName,
                                  Map measurementKeyValuePairs) {
        Measurement measurement = new Measurement()
        measurement.patient = patient
        measurement.time = now
        measurement.unit = unit
        measurement.measurementType = MeasurementType.findByName(measurementTypeName)
        measurementKeyValuePairs.keySet().each { key ->
            measurement[key as String] = measurementKeyValuePairs[key]
        }
        consultation.addToMeasurements(measurement)
    }

    private static Double getDouble(String value) {
        try {
            value.replaceAll(',', '.').toDouble()
        } catch (Exception ignored) {
            null
        }
    }
}
