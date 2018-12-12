package org.opentele.server

import org.hibernate.internal.SessionImpl
import org.springframework.beans.factory.annotation.Value

class SessionStatisticsFilters {

    @Value('${tracing.sessionStatistics:false}')
    boolean statisticsEnabled

    def sessionFactory
    def static sessionTracker = [] as Set

    def filters = {
        all(controller: '*', action: '*') {
            before = {

                if (!statisticsEnabled)
                    return

                def statistics = sessionFactory.statistics
                statistics.statisticsEnabled = true

                log.info("DUMPING SESSION STATISTICS (BEFORE FIlTER):")
                log.info(statistics.toString())

                def sessionTimestamp = ((SessionImpl)sessionFactory.currentSession).timestamp
                def hashCode = sessionFactory.currentSession.hashCode()
                log.info("Session timestamp and hashcode: ${sessionTimestamp} - ${hashCode}")
                log.info("Thread id: ${Thread.currentThread().getId()}")
                log.info("Request: ${request ? request.requestURI : 'null request'}")

                if (sessionTracker.contains(hashCode)) {
                    log.info("SESSION REUSED... Hashcode: $hashCode")
                } else {
                    sessionTracker << hashCode
                }

                log.info("==============================================================")
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
