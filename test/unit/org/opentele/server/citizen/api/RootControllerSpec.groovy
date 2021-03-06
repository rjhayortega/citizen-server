package org.opentele.server.citizen.api

import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

@TestFor(RootController)
class RootControllerSpec extends Specification {

    def response

    def getBody() {
        return response.resource
    }

    def setup() {
        response = null
    }

    void "can determine citizen API version"() {
        when:
        response = controller.show()

        then:
        body.apiVersion != null
        body.apiVersion != ""
        body.apiVersion != "0.0.0"
    }

    void "can get links to available resources"() {
        when:
        response = controller.show()

        then:
        body.links.patient.toURI() != null
        body.links.self.toURI() != null
    }
}
