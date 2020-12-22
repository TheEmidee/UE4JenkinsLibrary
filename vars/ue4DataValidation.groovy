#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {
    def buildgraph_tasks = []
    ue4_config.project.DataValidation.each{ task -> 
        buildgraph_tasks.add( task.BuildGraphTask )
    }

    if ( ue4_config.project.DataValidation.size() == 0 ) {
        log.warning "No Data Validation will be done. Did you forget to fill in the DataValidation section of the config file?"
        return
    }

    stage( "Data Validation" ) {
        ue4RunBuildGraph( 
            ue4_config,
            buildgraph_tasks,
            buildgraph_params
            )

        recordIssues failOnError: true, sourceCodeEncoding: 'UTF-8', tools: [ msbuild() ], qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true], [threshold: 1, type: 'NEW', unstable: false]], filters: [excludeCategory('ModuleManager|SwarmsEditor')]

        ue4_config.project.DataValidation.each{ task -> 
            try {
                timeout(time: 120, unit: 'SECONDS') {
                    def log_file_path = "Saved\\Logs\\${task.LogFileName}.log"

                    // recordIssues sometimes times out so first scan then publish
                    def data_validation_issues = scanForIssues blameDisabled: true, forensicsDisabled: true, tool: groovyScript(parserId: "${task.ParserName}", pattern: "${log_file_path}", reportEncoding: 'UTF-8')
                    publishIssues failOnError: true, qualityGates: [[threshold: 1, type: 'TOTAL', unstable: false]], issues: [ data_validation_issues ]
                }
            } catch ( e ) {
                echo "Error during issues for ${task.ParserName} " + e.toString()
            }
        }

        zip archive: true, dir: 'Saved\\Logs\\', glob: '', zipFile: 'Saved\\DataValidationLogs.zip'
    }
}