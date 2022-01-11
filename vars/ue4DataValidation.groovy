#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {

    if ( !ue4_config.Project.DataValidation.Run ) {
        log.warning "Data Validation disabled"
        return
    }

    def buildgraph_task_name = ue4_config.Project.DataValidation.BuildGraphTaskName

    if ( !buildgraph_task_name?.trim() ) {
        log.warning "No Data Validation will be done. Did you forget to fill in the DataValidation.BuildGraphTaskName section of the config file?"
        return
    }

    ue4DeleteLogs( ue4Config )

    stage( "Data Validation" ) {
        ue4RunBuildGraph( 
            ue4_config,
            buildgraph_task_name,
            buildgraph_params
            )

        ue4ParseLogs( ue4_config, ue4_config.Project.DataValidation.LogParsers )
        ue4ZipLogs( ue4_config, "DataValidation" )
    }
}