#!/usr/bin/groovy

def call( parsers ) {
    parsers.each { item -> 
        def parser = item.LogParser

        try {
            timeout(time: 120, unit: 'SECONDS') {
                def log_folder = parser.LogFolder
                if ( !log_folder?.trim() ) {
                    log_folder = "Saved\\Logs"
                }

                def log_file_path = "${log_folder}\\${parser.LogFileName}.log"

                def custom_name = parser.CustomName
                if ( !custom_name?.trim() ) {
                    custom_name = parser.ParserName
                }

                // recordIssues sometimes times out so first scan then publish
                def issues = scanForIssues blameDisabled: true, forensicsDisabled: true, tool: groovyScript(parserId: "${parser.ParserName}", name: "${custom_name}", pattern: "${log_file_path}", reportEncoding: 'UTF-8'), filters: [excludeCategory( excluded_categories )]
                publishIssues failOnError: true, qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true]], issues: [ issues ]
            }
        } catch ( e ) {
            echo "Error during issues for ${parser.ParserName} " + e.toString()
        }
    }
}