package org.opentele.server.citizen.measurement

import org.opentele.server.model.Measurement

class TableMeasurement {

    private Measurement measurement

    TableMeasurement(Measurement measurement) {
        this.measurement = measurement
    }

    def time() { measurement.time }

    def unit() { measurement.unit }

    def value() { measurement.toString() }

    def conference() { measurement.conference }

}
