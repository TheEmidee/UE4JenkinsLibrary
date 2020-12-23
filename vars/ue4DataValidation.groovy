#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {

    def buildgraph_task_name = ue4_config.project.DataValidation.BuildGraphTaskName

    if ( buildgraph_task_name == "" ) {
        log.warning "No Data Validation will be done. Did you forget to fill in the DataValidation.BuildGraphTaskName section of the config file?"
        return
    }

    ue4DeleteLogs

    stage( "Data Validation" ) {
        ue4RunBuildGraph( 
            ue4_config,
            buildgraph_task_name,
            buildgraph_params
            )

        ue4_config.project.DataValidation.Parsers.each { task -> 
            def parser = task.Parser

            try {
                timeout(time: 120, unit: 'SECONDS') {
                    def log_file_path = "Saved\\Logs\\${parser.LogFileName}.log"

                    // recordIssues sometimes times out so first scan then publish
                    def data_validation_issues = scanForIssues blameDisabled: true, forensicsDisabled: true, tool: groovyScript(parserId: "${parser.ParserName}", pattern: "${log_file_path}", reportEncoding: 'UTF-8')
                    publishIssues failOnError: true, qualityGates: [[threshold: 1, type: 'TOTAL', unstable: false]], issues: [ data_validation_issues ]
                }
            } catch ( e ) {
                echo "Error during issues for ${parser.ParserName} " + e.toString()
            }
        }

        ue4ZipLogs "DataValidation"
    }
}