#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {
    def buildgraph_task_name = ue4_config.project.Tests.BuildGraphTaskName

    if ( buildgraph_task_name == "" ) {
        log.warning "No Tests run will be done. Did you forget to fill in the Tests.BuildGraphTaskName section of the config file?"
        return
    }

    stage( buildgraph_task_name ) {
        try {
            ue4RunBuildGraph( 
                ue4_config,
                buildgraph_task_name,
                buildgraph_params
                )
        } finally {
            def folder = new File( 'Saved\\Tests\\Logs' )

            if ( folder.exists() ) {
                zip archive: true, dir: folder.toString(), glob: '', zipFile: 'Saved\\Tests\\TestsLogs.zip'
                junit testResults: "Saved\\Tests\\Logs\\FunctionalTestsResults.xml"
            }
        }
    }
}