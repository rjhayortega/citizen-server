package org.opentele.server.citizen.measurement

import org.opentele.server.core.model.types.MeasurementTypeName

class TableData {
    MeasurementTypeName type
    List<TableMeasurement> measurements
}
