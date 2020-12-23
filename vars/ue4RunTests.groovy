#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {
    def buildgraph_task_name = ue4_config.project.Tests.BuildGraphTaskName

    if ( buildgraph_task_name == "" ) {
        log.warning "No Tests run will be done. Did you forget to fill in the Tests.BuildGraphTaskName section of the config file?"
        return
    }

    stage( buildgraph_task_name ) {
        try {
            runBuildGraph( 
                ue4_config,
                buildgraph_task_name,
                buildgraph_params
                )
        } finally {
            zip archive: true, dir: 'Saved\\Tests\\Logs\\', glob: '', zipFile: 'Saved\\Tests\\TestsLogs.zip'
            junit testResults: "Saved\\Tests\\Logs\\FunctionalTestsResults.xml"
        }
    }
}