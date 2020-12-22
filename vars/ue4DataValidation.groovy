#!/usr/bin/groovy

def call( ue4_config ) {
    def buildgraph_tasks = []
    ue4_config.project.DataValidation.each{ task -> 
        buildgraph_tasks.add( task.BuildGraphTask )
    }

    stage( "Data Validation" ) {
        ue4RunBuildGraph( 
            ue4_config,
            buildgraph_tasks,
            buildgraph_params//,
            //swarms_buildgraph_arguments
            )

        msbuild_reports += scanForIssues filters: [excludeCategory('ModuleManager|SwarmsEditor')], tool: msBuild()

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