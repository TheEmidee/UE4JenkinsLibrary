#!/usr/bin/groovy

def call( ue4_config ) {
    fileOperations( [ 
        fileDeleteOperation( excludes: '', includes: 'Saved\\*.zip' ), 
        fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' ), 
        fileDeleteOperation( excludes: '', includes: 'Saved\\UnitTestsReport\\*.*'),
        folderDeleteOperation( 'Saved\\LocalBuilds' ),
        folderDeleteOperation( 'Saved\\Tests' ),
        folderDeleteOperation( ue4_config.project.RelativeOutputDirectory )
    ] )

    buildgraph_params = [
        "Clean" : params.CLEAN_PROJECT,
        "ProjectDir" : env.WORKSPACE,
        "BuildConfiguration": params.DEBUG_BUILDS ? "Debug" : "Development"
    ]

    ue4DataValidation ue4_config, buildgraph_params

    if ( ue4_config.project.Tests.Run ) {
        ue4RunTests ue4_config, buildgraph_params
    }

    buildgraph_params[ "ArchivePackage" ] = ue4_config.project.Package.Archive
    buildgraph_params[ "ZipPackage" ] = ue4_config.project.Package.Archive && ue4_config.project.Package.Zip

    // ATM its not possible to run in parallel out of the box. To do so, each task run in parallel must allocate a new node

    //def tasks = [:]

    ue4_config.project.Package.Targets.each { iterator -> 
        def target = iterator.Target

        //tasks[ "${target.Type} - ${target.Platform}" ] = {
            ue4PackageTarget target.Type, target.Platform, ue4_config, buildgraph_params
        //}
    }

    //parallel tasks
}