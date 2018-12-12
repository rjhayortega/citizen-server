package org.opentele.server.citizen.api

import grails.plugin.springsecurity.annotation.Secured
import org.opentele.server.core.model.types.PermissionName
import org.opentele.server.model.LinksCategory
import org.opentele.server.model.Patient

import org.opentele.server.model.Clinician
import org.opentele.server.model.Consultation
import grails.converters.JSON
import org.opentele.server.model.User

@Secured(PermissionName.NONE)
class PatientController {

    static allowedMethods = [show: "GET"]

    def grailsApplication
    def springSecurityService
    def citizenMessageService
     def consultationService
     
    @Secured(PermissionName.PATIENT_LOGIN)
    def show() {
        def user = springSecurityService.currentUser
        print("<><><>"+user);
        
        def patient = Patient.findByUser(user)
       // System.out.print(patient);
        
           log.debug 'save.....'
        log.debug 'params..:' + params
       // print(">>>>"+User.get("Helle Andersen"));
      
        //Clinician clinician = Clinician.findByUser(springSecurityService.currentUser as User)
        Clinician clinician = Clinician.findByUser(User.get(2))        
        print(clinician);
     
      
        Consultation consultation = consultationService.addConsultation(patient, clinician, params)
        print(consultation);
        
        if (!consultation.hasErrors()) {
            render ([success: true, tst:errors, message: message(code: 'consultation.saved.ok')] as JSON)
            return
        }


        def body = [
                'firstName': patient.firstName,
                'lastName': patient.lastName,
                'passwordExpired': patient.user.cleartextPassword ? true : false,
                'links': [
                        'self': createLink(mapping: 'patient', absolute: true),
                        'password': createLink(mapping: 'password', absolute: true),
                        'measurements': createLink(mapping: 'measurements', absolute: true),
                        'questionnaires': createLink(mapping: 'questionnaires', absolute: true),
                        'reminders':  createLink(mapping: 'reminders', absolute: true)
                ]
        ]

        if (citizenMessageService.isMessagesAvailableTo(patient)) {
            body.links['messageThreads'] = createLink(mapping: 'messageThreads', absolute: true)
            body.links['unreadMessages'] = createLink(mapping: 'messages', absolute: true)
            body.links['acknowledgements'] = createLink(mapping: 'acknowledgements', absolute: true)
        }

        if (Boolean.valueOf(grailsApplication.config.video.enabled)) {
            body.links['videoPendingConference'] = createLink(mapping: 'videoPendingConference', absolute: true)
            body.links['patientHasPendingMeasurement'] = createLink(mapping: 'patientHasPendingMeasurement', absolute: true)
            body.links['measurementFromPatient'] = createLink(mapping: 'measurementFromPatient', absolute: true)
        }

        if (hasAnyLinksCategories(patient)) {
            body.links['linksCategories'] = createLink(mapping: 'linksCategories', absolute: true)
        }

        [resource: body, resourceType: 'patient']
    }

    private static hasAnyLinksCategories(Patient patient) {
        def linksCategories = LinksCategory.byPatientGroups(patient.groups)
        linksCategories.size() > 0
    }
}
