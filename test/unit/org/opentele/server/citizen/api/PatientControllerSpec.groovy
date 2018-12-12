package org.opentele.server.citizen.api

import grails.buildtestdata.mixin.Build
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONObject
import org.opentele.builders.PatientBuilder
import org.opentele.server.citizen.CitizenMessageService
import org.opentele.server.model.Link
import org.opentele.server.model.LinksCategory
import org.opentele.server.model.Patient
import org.opentele.server.model.Patient2PatientGroup
import org.opentele.server.model.PatientGroup
import org.opentele.server.model.Role
import org.opentele.server.model.User
import org.opentele.server.model.UserRole
import spock.lang.Specification

@TestFor(PatientController)
@Mock([Patient, PatientGroup, Patient2PatientGroup, LinksCategory, Link])
@Build([Patient, User, Role, UserRole, PatientGroup, LinksCategory, Link])
class PatientControllerSpec extends Specification{
    Patient patient
    PatientGroup patientGroup
    def mockSpringSecurityService
    def mockCitizenMessageService

    def response

    def getBody() {
        return response.resource
    }

    def setup() {
        response = null
        patient = new PatientBuilder().build()
        patient.user = new User()
        patientGroup = PatientGroup.build()
        def patientGroupRelation = Patient2PatientGroup.link(patient, patientGroup);
        patient.addToPatient2PatientGroups(patientGroupRelation)

        mockSpringSecurityService = mockFor(SpringSecurityService)
        mockSpringSecurityService.metaClass.getCurrentUser = { ->
            patient.getUser()
        }
        controller.springSecurityService = mockSpringSecurityService

        mockCitizenMessageService = mockFor(CitizenMessageService)
        mockCitizenMessageService.metaClass.isMessagesAvailableTo = {p ->
            return true;
        }
        controller.citizenMessageService = mockCitizenMessageService
    }

    def "can get patient"() {
        when:
        response = controller.show()

        then:
        body.firstName == patient.firstName
        body.lastName == patient.lastName
        body.passwordExpired != null
    }

    def "patient password expired is set false when user has no temp password"() {
        given:
        patient.user.cleartextPassword = null

        when:
        response = controller.show()

        then:
        body.passwordExpired == false
    }

    def "patient password expired is set true when user has temp password"() {
        given:
        patient.user.cleartextPassword = 'foo'

        when:
        response = controller.show()

        then:
        body.passwordExpired == true
    }

    def "can get links to available resources for patient"() {
        when:
        response = controller.show()

        then:
        body.links.self.toURI() != null
        body.links.measurements.toURI() != null
        body.links.password.toURI() != null
        body.links.reminders.toURI() != null
        body.links.questionnaires.toURI() != null
        body.links.messageThreads.toURI() != null
        body.links.unreadMessages.toURI() != null
        body.links.acknowledgements.toURI() != null
    }

    def "link to message related resources not present when patient has messages disabled"() {
        given:
        mockCitizenMessageService.metaClass.isMessagesAvailableTo = {p ->
            return false;
        }

        when:
        response = controller.show()

        then:
        body.links.messageThreads == null
        body.links.unreadMessages == null
        body.links.acknowledgements == null
        // the others should still be there...
        body.links.measurements.toURI() != null
        body.links.password.toURI() != null
        body.links.reminders.toURI() != null
        body.links.questionnaires.toURI() != null
    }

    def "link to links categories not available if patient has no links categories assigned"(){
        when:
        response = controller.show()

        then:
        body.links.linksCategories == null
    }

    def "link to links categories available if patient has links categories assigned"() {
        given:
        def category = new LinksCategory(name: 'my category')
        category.addToLinks(new Link(title: 'jp', url: 'http://www.jp.dk'))
        category.addToPatientGroups(patientGroup)
        category.save(failOnError: true)

        when:
        response = controller.show()

        then:
        body.links.linksCategories != null
    }
}
