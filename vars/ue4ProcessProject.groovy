#!/usr/bin/groovy

def call( ue4_config, Closure on_stage_start = null ) {

    ue4CleanSavedFolder ue4_config

    def buildgraph_params = ue4InitializeBuildGraphParameters()

    if ( on_stage_start != null ) {
        on_stage_start( "Data Validation" )
    }

    ue4DataValidation ue4_config, buildgraph_params

    if ( ue4_config.Project.Tests.Run ) {
        if ( on_stage_start != null ) {
            on_stage_start( "Run Tests" )
        }
        ue4RunTests ue4_config, buildgraph_params
    }

    buildgraph_params[ "ArchivePackage" ] = ue4_config.Project.Package.Archive
    buildgraph_params[ "ZipPackage" ] = ue4_config.Project.Package.Archive && ue4_config.Project.Package.Zip

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
}