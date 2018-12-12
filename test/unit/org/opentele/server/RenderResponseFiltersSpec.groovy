package org.opentele.server

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONObject
import org.opentele.server.citizen.ResponsePipelineService
import org.opentele.server.citizen.api.RootController
import spock.lang.Specification

@TestFor(RootController)
@Mock([RenderResponseFilters, ResponsePipelineService])
class RenderResponseFiltersSpec extends Specification {

    def responsePipelineServiceMock

    def setup() {
        responsePipelineServiceMock = Mock(ResponsePipelineService)
        MockedResponsePipelineService.mockService = responsePipelineServiceMock

        defineBeans {
            responsePipelineService(MockedResponsePipelineService)
        }
    }

    def "filter invokes response listeners"() {
        when:
        withFilters(action: 'show') {
            controller.show()
        }

        then:
        1 * responsePipelineServiceMock.invokeResponseListeners(_, _)
    }

    def "model rendered correctly by filter"() {
        when:
        withFilters(action: 'show') {
            controller.show()
        }

        then:
        def body = new JSONObject(response.text)
        body.apiVersion != null
        response.status == 200
        response.contentType.contains('application/json')
    }
}

class MockedResponsePipelineService extends ResponsePipelineService {

    static def mockService

    @Override
    def invokeResponseListeners(resourceType, resource) {
        mockService.invokeResponseListeners(resourceType, resource)
    }
}
