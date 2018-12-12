package org.opentele.server.citizen.api

import grails.plugin.springsecurity.annotation.Secured
import org.opentele.server.core.model.types.PermissionName
import org.opentele.server.model.LinksCategory
import org.opentele.server.model.Patient

@Secured(PermissionName.NONE)
class LinksCategoriesController {

    def springSecurityService

    @Secured(PermissionName.PATIENT_LOGIN)
    def list() {
        def user = springSecurityService.currentUser
        def patient = Patient.findByUser(user)

        def categories = LinksCategory.byPatientGroups(patient.groups)
        def body = [
            'categories': categories.collect {
                [
                    name : it.name,
                    links: ['linksCategory': createLink(mapping: 'linksCategory', params: [id: it.id], absolute: true)]
                ]
            },
            'links': ['self': createLink(mapping: 'linksCategories', absolute: true)]
        ]

        [resourceType: 'linksCategories', resource: body]
    }

    @Secured(PermissionName.PATIENT_LOGIN)
    def show(long id) {
        def category = LinksCategory.get(id)
        if (!category) {
            return [status: 404, resourceType: 'errors', resource: [
                    'message': 'Links category not found',
                    'errors': [['resource': 'linksCategory', 'field': 'id', 'code': 'missing']]]]
        }

        def body = [
                'name': category.name,
                'categoryLinks': category.links.collect {[
                        'title': it.title,
                        'url': it.url.toString()
                ]},
                'links': ['self': createLink(mapping: 'linksCategory', params: [id: id], absolute: true)]
        ]

        [resourceType: 'linksCategory', resource: body]
    }
}
