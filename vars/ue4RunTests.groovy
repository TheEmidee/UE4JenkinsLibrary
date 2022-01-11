#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {
    def buildgraph_task_name = ue4_config.Project.Tests.BuildGraphTaskName

    if ( buildgraph_task_name == "" ) {
        log.warning "No Tests run will be done. Did you forget to fill in the Tests.BuildGraphTaskName section of the config file?"
        return
    }

    ue4DeleteLogs( ue4Config )

    stage( buildgraph_task_name ) {
        try {
            ue4_config.Project.Tests.AdditionalBuildgraphProperties.each { set_property_iterator ->
                def property = set_property_iterator.Property
                buildgraph_params[ property.Name ] = property.Value
            }

            ue4RunBuildGraph( 
                ue4_config,
                buildgraph_task_name,
                buildgraph_params
                )
        } finally {
            ue4_config.Project.Tests.AdditionalBuildgraphProperties.each { unset_property_iterator ->
                def property = unset_property_iterator.Property
                buildgraph_params.remove( property.Name )
            }

            ue4ZipLogs( ue4_config, "Tests" )

            if ( fileExists ( 'Saved\\Tests\\Logs' ) ) {
                    if ( ue4_config.Options.Stub ) {
                        echo "Would archive test results and publish jUnit"
                    } else {
                        zip archive: true, dir: "Saved\\Tests\\Logs", glob: '', zipFile: 'Saved\\Tests\\Tests_Results.zip'
                        junit testResults: "Saved\\Tests\\Logs\\FunctionalTestsResults.xml"
                    }
            }
        }
    }
}