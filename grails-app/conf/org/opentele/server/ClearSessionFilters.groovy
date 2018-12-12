package org.opentele.server

import org.hibernate.SessionFactory

class ClearSessionFilters {

    SessionFactory sessionFactory

    def filters = {
        all(controller: '*', action: '*') {
            before = {
                def currentSession = sessionFactory.currentSession
                currentSession.flush()
                currentSession.clear()
            }
        }
    }
}
