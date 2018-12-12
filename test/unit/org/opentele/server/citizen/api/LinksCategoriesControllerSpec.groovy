package org.opentele.server.citizen.api

import grails.buildtestdata.mixin.Build
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONObject
import org.opentele.server.model.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LinksCategoriesController)
@Mock([Patient, PatientGroup, Patient2PatientGroup, LinksCategory, Link])
@Build([Patient, User, Role, UserRole, PatientGroup, LinksCategory, Link])
class LinksCategoriesControllerSpec extends Specification {

    def mockSpringSecurityService
    def response

    def getBody() {
        return response.resource
    }

    def setup() {
        response = null
        def patient = Patient.build(user: new User())
        def patientGroup = PatientGroup.build().save(failOnError: true)
        Patient2PatientGroup.link(patient, patientGroup);
        patient.save(failOnError: true)

        def category1 = new LinksCategory(name: "category 1")
        category1.addToLinks(new Link(title: "url_1", url: "http://www.jp.dk"))
        category1.addToLinks(new Link(title: "url_2", url: "http://www.bt.dk"))
        category1.addToPatientGroups(patientGroup)
        category1.save(failOnError: true)

        def category2 = new LinksCategory(name: "category 2")
        category2.addToLinks(new Link(title: "url_2", url: "http://www.jp.dk"))
        category2.addToPatientGroups(patientGroup)
        category2.save(failOnError: true)

        def patientGroupNoIncluded = PatientGroup.build().save(failOnError: true)
        def category3 = new LinksCategory(name: "category not belonging to patient")
        category3.addToLinks(new Link(title: "not included", url: "http://www.someurl.com"))
        category3.addToPatientGroups(patientGroupNoIncluded)
        category3.save(failOnError: true)

        mockSpringSecurityService = mockFor(SpringSecurityService)
        mockSpringSecurityService.metaClass.getCurrentUser = { ->
            patient.getUser()
        }
        controller.springSecurityService = mockSpringSecurityService
    }

    def "can get list of links categories for patient"() {
        when:
        response = controller.list()

        then:
        body.categories.size() == 2
        def category1 = body.categories.find { it.name == "category 1"}
        category1 != null
        category1.links.size() == 1
        def category2 = body.categories.find { it.name == "category 2"}
        category2 != null
        category2.links.size() == 1
    }

    def "can get single links category with links"() {
        when:
        params.id = LinksCategory.findByName("category 1").id.toString()
        response = controller.show()

        then:
        body != null
        body.name == 'category 1'
        body.categoryLinks.size() == 2
        body.categoryLinks[0].title == "url_1"
        body.categoryLinks[0].url == "http://www.jp.dk"
        body.categoryLinks[1].title == "url_2"
        body.categoryLinks[1].url == "http://www.bt.dk"
        body.links.self != null
    }

    def "non-existing links category renders error"() {
        when:
        params.id = "42"
        response = controller.show()

        then:
        response.status == 404
        body != null
        body.message.contains('not found')
        body.errors[0].resource == "linksCategory"
        body.errors[0].field == "id"
        body.errors[0].code == "missing"
    }
}
