package org.opentele.server.citizen

import org.apache.commons.lang.NullArgumentException

class ResponsePipelineService {

    def listeners = []

    def registerResponseListener(Closure listener) {
        if (!listener) {
            throw new NullArgumentException("listener")
        }

        listeners << listener
    }

    def invokeResponseListeners(resourceType, resource) {
        if (!listeners) {
            return
        }
        listeners.each { it(resourceType, resource) }
    }

    def clearResponseListeners() {
        if (!listeners) {
            return
        }
        listeners.clear()
    }
}
