package org.opentele.server.citizen


import grails.test.mixin.*
import org.junit.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ResponsePipelineService)
class ResponsePipelineServiceSpec extends Specification {

    def "can invoke registered response listeners"() {
        given:
        def type = null
        def resource = null
        service.registerResponseListener {t, r ->
            type = t
            resource = r
        }

        when:
        service.invokeResponseListeners("bla", ['foo': 'bar'])

        then:
        type == 'bla'
        resource.foo == 'bar'
    }

    def "can register and clear listeners"() {
        when:
        service.registerResponseListener {t, r -> }
        def countAfterRegister = service.listeners.size()
        service.clearResponseListeners()
        def countAfterClear = service.listeners.size()

        then:
        countAfterRegister == 1
        countAfterClear == 0
    }
}
