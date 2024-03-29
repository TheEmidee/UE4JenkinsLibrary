#!/usr/bin/groovy

def call( ue4_config, parsers ) {
    if ( ue4_config.Options.Stub ) {
        
        def parsers_list = ""
        parsers.each { item -> 
            def parser = item.LogParser
            parsers_list += "${parser.ParserName} - "
        }

        echo "Would parse logs with ${parsers_list}"
    } else {
        parsers.each { item -> 
            def parser = item.LogParser

            try {
                timeout(time: 120, unit: 'SECONDS') {
                    def log_folder = parser.LogFolder
                    if ( !log_folder?.trim() ) {
                        log_folder = "Saved\\Logs"
                    }

                    def log_file_path = "${log_folder}\\${parser.LogFileName}.log"

                    def parser_id = parser.ParserId
                    if ( !parser_id?.trim() ) {
                        parser_id = parser.ParserName
                    }

                    // recordIssues sometimes times out so first scan then publish
                    def issues = scanForIssues blameDisabled: true, forensicsDisabled: true, tool: groovyScript(parserId: "${parser.ParserName}", id: "${parser_id}", name: "${parser_id}", pattern: "${log_file_path}", reportEncoding: 'UTF-8'), filters: [excludeCategory('LogTemp')]
                    publishIssues failOnError: true, qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true]], issues: [ issues ]
                }
            } catch ( e ) {
                echo "Error during issues for ${parser.ParserName} " + e.toString()
            }
        }
    }
}