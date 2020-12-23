#!/usr/bin/groovy

def call( ue4_config ) {
    stage( "Swarms Tests" ) {
        try {
            notifier.notifyStage slack_response, "Run BuildGraph task : Swarms Tests"
            runBuildGraph( 
                [ "Swarms Tests" ],
                buildgraph_params,
                swarms_buildgraph_arguments
                )
        } finally {
            zip archive: true, dir: 'Saved\\Tests\\Logs\\', glob: '', zipFile: 'Saved\\Tests\\GauntletTestsLogs.zip'
            notifier.uploadFileToMessage slack_response, "Saved\\Tests\\GauntletTestsLogs.zip"
            junit testResults: "Saved\\Tests\\Logs\\FunctionalTestsResults.xml"
        }
    }
}