package org.opentele.server.citizen.model

import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.validation.ValidationException
import org.opentele.server.citizen.RealTimeCTGService
import org.opentele.server.model.Patient
import org.opentele.server.model.RealTimeCtg
import org.opentele.server.model.User
import spock.lang.Specification

@TestFor(RealTimeCTGController)
@Build([User, Patient, RealTimeCtg])
@Mock(RealTimeCtg)
class RealTimeCTGControllerSpec extends Specification {

    static String XML = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxFbnZlbG9wZSB4bWxucz0iaHR0cDovL3NjaGVtYXMueG1sc29hcC5vcmcvc29hcC9lbnZlbG9wZS8iPg0KICA8Qm9keT4NCiAgICA8TmV3TWVzc2FnZSB4bWxucz0iaHR0cDovL3RlbXB1cmkub3JnLyI+DQogICAgICA8bWVzc2FnZT4NCiAgICAgICAgPGN0Z0ZpZWxkIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5kYXRhY29udHJhY3Qub3JnLzIwMDQvMDcvTWlsb3UuU2VydmVyLk9wZW5UZWxlUlQiPg0KICAgICAgICAgIDxDdGdNZXNzYWdlQmxvY2s+DQogICAgICAgICAgICA8ZmhyRmllbGQ+WzEyNC43NSwgMTI4Ljc1LCAxMjguNzUsIDEzNC4yNV08L2ZockZpZWxkPg0KICAgICAgICAgICAgPG1ockZpZWxkPls3MC41LCA2NC43NSwgNjkuNSwgNjQuNV08L21ockZpZWxkPg0KICAgICAgICAgICAgPHNlcXVlbmNlTmJyRmllbGQ+Mjwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzE3LjUsIDExLjc1LCAxNC4yNSwgMTcuMjVdPC90b2NvRmllbGQ+DQogICAgICAgICAgPC9DdGdNZXNzYWdlQmxvY2s+DQogICAgICAgICAgPEN0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICAgIDxmaHJGaWVsZD5bMTI2LjUsIDEyOC4wLCAxMzAuNSwgMTI3LjBdPC9maHJGaWVsZD4NCiAgICAgICAgICAgIDxtaHJGaWVsZD5bNjYuMjUsIDY2Ljc1LCA3MC4wLCA2MS43NV08L21ockZpZWxkPg0KICAgICAgICAgICAgPHNlcXVlbmNlTmJyRmllbGQ+Mzwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzEzLjc1LCAyOS41LCAyMy41LCAyNi4yNV08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMjkuMCwgMTI3Ljc1LCAxMjMuNSwgMTI2LjI1XTwvZmhyRmllbGQ+DQogICAgICAgICAgICA8bWhyRmllbGQ+WzYxLjAsIDYwLjUsIDcwLjc1LCA2MC4wXTwvbWhyRmllbGQ+DQogICAgICAgICAgICA8c2VxdWVuY2VOYnJGaWVsZD40PC9zZXF1ZW5jZU5ickZpZWxkPg0KICAgICAgICAgICAgPHNxRmllbGQ+WzMsIDMsIDMsIDNdPC9zcUZpZWxkPg0KICAgICAgICAgICAgPHRpbWVGaWVsZD4yMDE0LTA4LTI1VDA3OjQ0OjIwWjwvdGltZUZpZWxkPg0KICAgICAgICAgICAgPHRvY29GaWVsZD5bMTkuNSwgMjcuNzUsIDEwLjc1LCAyOS4yNV08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMzcuMCwgMTIyLjAsIDEzMy43NSwgMTI0LjVdPC9maHJGaWVsZD4NCiAgICAgICAgICAgIDxtaHJGaWVsZD5bNjguNzUsIDY5LjUsIDY4LjUsIDYxLjBdPC9taHJGaWVsZD4NCiAgICAgICAgICAgIDxzZXF1ZW5jZU5ickZpZWxkPjU8L3NlcXVlbmNlTmJyRmllbGQ+DQogICAgICAgICAgICA8c3FGaWVsZD5bMywgMywgMywgM108L3NxRmllbGQ+DQogICAgICAgICAgICA8dGltZUZpZWxkPjIwMTQtMDgtMjVUMDc6NDQ6MjBaPC90aW1lRmllbGQ+DQogICAgICAgICAgICA8dG9jb0ZpZWxkPlsyOS4wLCAxMC41LCAxOS41LCAxNS4yNV08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMjQuMCwgMTM0LjAsIDEyNS41LCAxMjMuMjVdPC9maHJGaWVsZD4NCiAgICAgICAgICAgIDxtaHJGaWVsZD5bNjAuNSwgNjQuNzUsIDYxLjUsIDYwLjBdPC9taHJGaWVsZD4NCiAgICAgICAgICAgIDxzZXF1ZW5jZU5ickZpZWxkPjY8L3NlcXVlbmNlTmJyRmllbGQ+DQogICAgICAgICAgICA8c3FGaWVsZD5bMywgMywgMywgM108L3NxRmllbGQ+DQogICAgICAgICAgICA8dGltZUZpZWxkPjIwMTQtMDgtMjVUMDc6NDQ6MjBaPC90aW1lRmllbGQ+DQogICAgICAgICAgICA8dG9jb0ZpZWxkPlsyNS4yNSwgMTEuMjUsIDE2Ljc1LCAyMS4yNV08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMzkuMCwgMTI5Ljc1LCAxMjUuNzUsIDEyMy41XTwvZmhyRmllbGQ+DQogICAgICAgICAgICA8bWhyRmllbGQ+WzYyLjAsIDY1LjI1LCA3MC43NSwgNjAuMF08L21ockZpZWxkPg0KICAgICAgICAgICAgPHNlcXVlbmNlTmJyRmllbGQ+Nzwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzI3Ljc1LCAyMy43NSwgMTIuMjUsIDI3LjBdPC90b2NvRmllbGQ+DQogICAgICAgICAgPC9DdGdNZXNzYWdlQmxvY2s+DQogICAgICAgICAgPEN0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICAgIDxmaHJGaWVsZD5bMTI3LjI1LCAxMjIuNSwgMTIzLjUsIDEzMy4yNV08L2ZockZpZWxkPg0KICAgICAgICAgICAgPG1ockZpZWxkPls3MC41LCA2MS41LCA3MC43NSwgNjkuNV08L21ockZpZWxkPg0KICAgICAgICAgICAgPHNlcXVlbmNlTmJyRmllbGQ+ODwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzMwLjI1LCAyNS4yNSwgMjMuMCwgMTguMF08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMzAuNSwgMTIyLjI1LCAxMzMuMjUsIDEzNi4wXTwvZmhyRmllbGQ+DQogICAgICAgICAgICA8bWhyRmllbGQ+WzYzLjAsIDYwLjI1LCA2NC4wLCA2NS43NV08L21ockZpZWxkPg0KICAgICAgICAgICAgPHNlcXVlbmNlTmJyRmllbGQ+OTwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzIwLjAsIDIyLjAsIDI3LjI1LCAyMC43NV08L3RvY29GaWVsZD4NCiAgICAgICAgICA8L0N0Z01lc3NhZ2VCbG9jaz4NCiAgICAgICAgICA8Q3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgICAgPGZockZpZWxkPlsxMzcuNSwgMTI3LjI1LCAxMjAuNSwgMTQwLjVdPC9maHJGaWVsZD4NCiAgICAgICAgICAgIDxtaHJGaWVsZD5bNjguNzUsIDY4LjUsIDYyLjAsIDYzLjBdPC9taHJGaWVsZD4NCiAgICAgICAgICAgIDxzZXF1ZW5jZU5ickZpZWxkPjEwPC9zZXF1ZW5jZU5ickZpZWxkPg0KICAgICAgICAgICAgPHNxRmllbGQ+WzMsIDMsIDMsIDNdPC9zcUZpZWxkPg0KICAgICAgICAgICAgPHRpbWVGaWVsZD4yMDE0LTA4LTI1VDA3OjQ0OjIwWjwvdGltZUZpZWxkPg0KICAgICAgICAgICAgPHRvY29GaWVsZD5bMjIuMCwgMzAuMCwgMjYuMjUsIDE2LjI1XTwvdG9jb0ZpZWxkPg0KICAgICAgICAgIDwvQ3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICAgIDxDdGdNZXNzYWdlQmxvY2s+DQogICAgICAgICAgICA8ZmhyRmllbGQ+WzEyMS43NSwgMTMwLjUsIDEzMS4yNSwgMTM0LjI1XTwvZmhyRmllbGQ+DQogICAgICAgICAgICA8bWhyRmllbGQ+WzYzLjc1LCA2Ni4wLCA2OS4wLCA2OC41XTwvbWhyRmllbGQ+DQogICAgICAgICAgICA8c2VxdWVuY2VOYnJGaWVsZD4xMTwvc2VxdWVuY2VOYnJGaWVsZD4NCiAgICAgICAgICAgIDxzcUZpZWxkPlszLCAzLCAzLCAzXTwvc3FGaWVsZD4NCiAgICAgICAgICAgIDx0aW1lRmllbGQ+MjAxNC0wOC0yNVQwNzo0NDoyMFo8L3RpbWVGaWVsZD4NCiAgICAgICAgICAgIDx0b2NvRmllbGQ+WzIxLjUsIDE5LjUsIDI0Ljc1LCAyNC41XTwvdG9jb0ZpZWxkPg0KICAgICAgICAgIDwvQ3RnTWVzc2FnZUJsb2NrPg0KICAgICAgICA8L2N0Z0ZpZWxkPg0KICAgICAgICA8ZGV2aWNlSURGaWVsZCB4bWxucz0iaHR0cDovL3NjaGVtYXMuZGF0YWNvbnRyYWN0Lm9yZy8yMDA0LzA3L01pbG91LlNlcnZlci5PcGVuVGVsZVJUIj5zaWx2ZXJidWxsZXQtbW9uaWNhLXRlc3Q8L2RldmljZUlERmllbGQ+DQogICAgICAgIDxtYXJrZXJzRmllbGQgeG1sbnM9Imh0dHA6Ly9zY2hlbWFzLmRhdGFjb250cmFjdC5vcmcvMjAwNC8wNy9NaWxvdS5TZXJ2ZXIuT3BlblRlbGVSVCIgbmlsPSJ0cnVlIi8+DQogICAgICAgIDxwYXRpZW50RmllbGQgeG1sbnM9Imh0dHA6Ly9zY2hlbWFzLmRhdGFjb250cmFjdC5vcmcvMjAwNC8wNy9NaWxvdS5TZXJ2ZXIuT3BlblRlbGVSVCI+DQogICAgICAgICAgPGlkRmllbGQ+MTE8L2lkRmllbGQ+DQogICAgICAgICAgPG5hbWVGaWVsZD4NCiAgICAgICAgICAgIDxmaXJzdEZpZWxkPk5hbmN5IEFubjwvZmlyc3RGaWVsZD4NCiAgICAgICAgICAgIDxsYXN0RmllbGQ+IEJlcmdncmVuPC9sYXN0RmllbGQ+DQogICAgICAgICAgPC9uYW1lRmllbGQ+DQogICAgICAgIDwvcGF0aWVudEZpZWxkPg0KICAgICAgICA8cmVnaXN0cmF0aW9uSURGaWVsZCB4bWxucz0iaHR0cDovL3NjaGVtYXMuZGF0YWNvbnRyYWN0Lm9yZy8yMDA0LzA3L01pbG91LlNlcnZlci5PcGVuVGVsZVJUIj5jOGExMjQ4OC0yMDM0LTRjNDctYmNlYi0zY2E3MmUwNTFmY2U8L3JlZ2lzdHJhdGlvbklERmllbGQ+DQogICAgICA8L21lc3NhZ2U+DQogICAgPC9OZXdNZXNzYWdlPg0KICA8L0JvZHk+DQo8L0VudmVsb3BlPg=="
    static String soapAction = "http://tempuri.org/IOpenTeleRT/NewMessage"

    def patient

    def setup() {
        grailsApplication.config.milou.realtimectg.maxPerPatient = 1000
        patient = Patient.build()
        patient.save(failOnError: true)
        controller.realTimeCTGService = Mock(RealTimeCTGService)

        controller.springSecurityService = Mock(SpringSecurityService)
        controller.springSecurityService.currentUser >> patient.user
    }

    def 'will return 201 on valid request'() {
        given:
        controller.params.xml = XML
        controller.params.soapAction = soapAction
        controller.params.patientId = 1
        controller.params.sequenceNumber = 1
        request.method = 'POST'

        when:
        controller.save()

        then:
        1 * controller.realTimeCTGService.save(_)
        controller.response.status == 201
    }

    def 'will pass patient to service'() {
        given:
        controller.params.xml = XML
        controller.params.soapAction = soapAction
        controller.params.sequenceNumber = 1
        request.method = 'POST'

        when:
        controller.save()

        then:
        1 * controller.realTimeCTGService.save({params -> params.patient == patient})
    }

    def 'will return status code 400 when receiving wrong data'() {
        setup:
        controller.params.xml = XML
        controller.params.soapAction = soapAction
        controller.params.patientId = 1
        controller.params.sequenceNumber = 1

        controller.realTimeCTGService.save(_) >> {throw new ValidationException("Mock validation error", new RealTimeCtg().errors)}
        def inputJSON = ['xml': XML, 'soapAction': soapAction, 'patientId': patient.id]

        request.method = 'POST'

        when:
        controller.params.json = inputJSON as JSON
        controller.save()

        then:
        controller.response.status == 400
    }

    def 'will receive status code 429 when patient has too many pending samples'() {
        given:
        setupForTooManySamples()

        def inputJSON = ['xml': XML, 'soapAction': soapAction, 'patientId': patient.id]
        controller.params.json = inputJSON as JSON
        request.method = 'POST'

        when:
        controller.save()

        then:
        controller.response.status == 429
        0 * controller.realTimeCTGService.save(_)
    }

    def 'will delete all samples for patient when patient has too many pending samples'() {
        given:
        setupForTooManySamples()

        def inputJSON = ['xml': XML, 'soapAction': soapAction, 'patientId': patient.id]
        controller.params.json = inputJSON as JSON
        request.method = 'POST'

        when:
        controller.save()

        then:
        1 * controller.realTimeCTGService.deleteFor(patient)
    }

    private def setupForTooManySamples() {
        grailsApplication.config.milou.realtimectg.maxPerPatient = "1"
        def ctg1 = RealTimeCtg.build(patient: patient)
        def ctg2 = RealTimeCtg.build(patient: patient)
        ctg1.save(failOnError: true)
        ctg2.save(failOnError: true)
    }
}


