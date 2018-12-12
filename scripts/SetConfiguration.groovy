includeTargets << grailsScript("_GrailsArgParsing")

/*
Run with;
grails set-configuration "--user=opentele" "--pw=opentele" "--db=opentele" "--server=localhost:3306" "--context=citizen-demo" "--configGroovyPath=grails-app/conf/Config.groovy"
 */

target(main: "Replace configuration settings") {
    depends(parseArguments)

    def input = argsMap.findAll { it.key != 'params' }

    println("Generating war specific properties file...")
    println("Raw args: $args")
    println("Properties provided: $input")

    def properties = buildProperties(input)
    def propertiesFileName = savePropertiesFile(properties, argsMap.context)
    pointAppToPropertiesFile(argsMap.configGroovyPath, propertiesFileName)
}

def buildProperties(args) {
    def props = new Properties()

    def dbVendor = args.dbVendor
    def dbDialect, dbUrlPrefix, dbSeparator

    switch (dbVendor) {
        case "mysql":
            dbDialect = "org.opentele.server.core.util.MySQLInnoDBDialect"
            dbUrlPrefix = "mysql"
            dbSeparator = "/"
            break;
        case "sqlserver":
            dbDialect = "org.opentele.server.core.util.SQLServerDialect"
            dbUrlPrefix = "jtds:sqlserver"
            dbSeparator = ":"
            break;
        default:
            throw new RuntimeException("Unknown database vendor passed: ${dbVendor}")
    }

    props.setProperty("dataSource.pooled", "true")
    props.setProperty("dataSource.dialect", dbDialect)
    props.setProperty("dataSource.driverClassName", "com.mysql.jdbc.Driver")
    props.setProperty("dataSource.username", "${args.user}")
    props.setProperty("dataSource.password", "${args.pw}")
    props.setProperty("dataSource.url", "jdbc:${dbUrlPrefix}://${args.server}${dbSeparator}${args.db}")
    props.setProperty("languageTag", "${args.language}")
    props.setProperty("logging.suffix", "${args.context}")
    props.setProperty("video.enabled", "${args.enableVideo ?: true}")

    return props
}

def savePropertiesFile(properties, contextRoot) {
    def file = new File('grails-app/conf', "datamon-${contextRoot}-config.properties")
    def writer = file.newWriter('UTF-8', false)
    try {
        properties.store(writer, 'Properties specific to a single war file.')
    }
    finally {
        writer.close()
    }
    println("Properties file '${file.absolutePath}' saved")

    return file.name
}

def pointAppToPropertiesFile(String pathToConfigGroovy, String propertiesFileName) {
    def config = new File(pathToConfigGroovy.toString())
    println("Setting path to properties file in: ${pathToConfigGroovy}")
    def text = ""
    config.eachLine { line ->
        if (line =~ /grails.config.locations/) {
            line = "grails.config.locations = [\"file:\${userHome}/.opentele/${propertiesFileName}\"]"
        }
        text += line + System.getProperty("line.separator")
    }
    config.write(text, 'UTF-8')
}

setDefaultTarget(main)
