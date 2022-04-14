#!/usr/bin/groovy

def call( ue4_config, Closure on_stage_start = null ) {

    ue4CleanSavedFolder ue4_config
    ue4InitializeLogParsers ue4_config

    def buildgraph_params = ue4InitializeBuildGraphParameters()

    if ( on_stage_start != null ) {
        on_stage_start( "Data Validation" )
    }

    ue4DataValidation ue4_config, buildgraph_params

    if ( ue4_config.Project.Package.Archive )
    {
        buildgraph_params[ "ArchivePackage" ] = ue4_config.Project.Package.Archive
        buildgraph_params[ "ZipPackage" ] = ue4_config.Project.Package.Archive && ue4_config.Project.Package.Zip
        buildgraph_params[ "UploadSymbols" ] = ue4_config.Project.Package.UploadSymbols
    }

    ue4BuildContent ue4_config, buildgraph_params

    // ATM its not possible to run in parallel out of the box. To do so, each task run in parallel must allocate a new node

    //def tasks = [:]

    ue4_config.Project.Package.Targets.each { iterator -> 
        def target = iterator.Target

        //tasks[ "${target.Type} - ${target.Platform}" ] = {
            if ( on_stage_start != null ) {
                on_stage_start( "Package ${target.Type} ${target.Platform}" )
            }
            ue4PackageTarget target.Type, target.Platform, ue4_config, buildgraph_params
        //}
    }

    //parallel tasks

    ue4DeleteTestsFolder( ue4_config )

    ue4_config.Project.AdditionalBuildgraphTasks.each { task_iterator ->
        def additional_buildgraph_task = task_iterator.BuildgraphTask

        if ( on_stage_start != null ) {
            on_stage_start( additional_buildgraph_task.TaskName )
        }

        ue4DeleteLogs( ue4_config )

        stage( additional_buildgraph_task.TaskName ) {
            try {
                additional_buildgraph_task.AdditionalBuildgraphProperties.each { set_property_iterator ->
                    def property = set_property_iterator.Property
                    buildgraph_params[ property.Name ] = property.Value
                }

                ue4RunBuildGraph( 
                    ue4_config,
                    additional_buildgraph_task.TaskName,
                    buildgraph_params
                    )
            } finally {
                ue4ParseLogs( ue4_config, additional_buildgraph_task.LogParsers ) 

                additional_buildgraph_task.AdditionalBuildgraphProperties.each { unset_property_iterator ->
                    def property = unset_property_iterator.Property
                    buildgraph_params.remove( property.Name )
                }
            }
        }
    }

    if ( ue4_config.Project.Tests.Run ) {
        if ( on_stage_start != null ) {
            on_stage_start( "Run Tests" )
        }
        ue4RunTests ue4_config, buildgraph_params
    }
}