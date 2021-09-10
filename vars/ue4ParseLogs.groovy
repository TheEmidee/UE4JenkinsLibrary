#!/usr/bin/groovy

def call( parsers, excluded_categories ) {
    if ( excluded_categories == null ) {
        excluded_categories = ""
    }

    parsers.each { item -> 
        def parser = item.LogParser

        try {
            timeout(time: 120, unit: 'SECONDS') {
                def log_file_path = "Saved\\Logs\\${parser.LogFileName}.log"

                // recordIssues sometimes times out so first scan then publish
                def issues = scanForIssues blameDisabled: true, forensicsDisabled: true, tool: groovyScript(parserId: "${parser.ParserName}", pattern: "${log_file_path}", reportEncoding: 'UTF-8'), filters: [excludeCategory( excluded_categories )]
                publishIssues failOnError: true, qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true]], issues: [ issues ]
            }
        } catch ( e ) {
            echo "Error during issues for ${parser.ParserName} " + e.toString()
        }
    }
}