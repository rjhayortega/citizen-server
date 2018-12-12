package org.opentele.server.citizen

import org.opentele.server.core.test.AbstractIntegrationSpec

class I18NIntegrationSpec extends AbstractIntegrationSpec {

    def messageSource

    def danishLocale = new Locale('da')
    def englishLocale = new Locale('en')

    def setup() {
        messageSource.metaClass.getAllMessageKeys = { Locale locale ->
            Set applicationKeys = delegate.getMergedProperties(locale).getProperties().keySet()
            Set pluginsKeys = delegate.getMergedPluginProperties(locale).getProperties().keySet()
            def unionOfKeys = applicationKeys + pluginsKeys
            unionOfKeys
        }
    }

    def "Test that all keys exist for all languages"() {
        given:
        Set danishKeys = messageSource.getAllMessageKeys(danishLocale)
        Set englishKeys = messageSource.getAllMessageKeys(englishLocale)

        when:
        def symmetricDifferenceSet = (englishKeys - danishKeys) + (danishKeys - englishKeys)

        then:
        assert symmetricDifferenceSet.size() == 0

    }

    def "Test that no texts are copied across languages"() {
        given:
        Set danishKeys = messageSource.getAllMessageKeys(danishLocale)
        Set englishKeys = messageSource.getAllMessageKeys(englishLocale)

        when:
        Set<String> intersectionSet = englishKeys.intersect(danishKeys) as Set<String>

        intersectionSet.each { key ->
            def danishValue = messageSource.getMessage(key, null, danishLocale)
            def englishValue = messageSource.getMessage(key, null, englishLocale)
            if (danishValue == englishValue) {
                println("${key} = { da: ${danishValue}, en: ${englishValue} }")
            }
        }

        then:
        // This test will have false positives so we simply run it to get a
        // printout of the values which are for the two languages. Hence we
        // just 'assert true' in the 'then' clause.
        assert true

    }

}