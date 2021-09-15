#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {

    if ( !ue4_config.Project.DataValidation.Run ) {
        log.warning "Data Validation disabled"
    }

    def buildgraph_task_name = ue4_config.Project.DataValidation.BuildGraphTaskName

    if ( !buildgraph_task_name?.trim() ) {
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

        def config_excluded_categories = ue4_config.Project.IssuesExcludedCategories.join('|')
        ue4ParseLogs( ue4_config.Project.DataValidation.LogParsers, config_excluded_categories )
        ue4ZipLogs "DataValidation"
    }
}