#!/usr/bin/groovy

def call( ue4_config ) {
    fileOperations( [ 
        fileDeleteOperation( excludes: '', includes: 'Saved\\*.zip' ), 
        fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' ), 
        fileDeleteOperation( excludes: '', includes: 'Saved\\UnitTestsReport\\*.*'),
        fileDeleteOperation( excludes: '', includes: 'Saved\\Tests\\GauntletTestsLogs.zip' ) ,
        folderDeleteOperation( 'Saved\\Tests' ),
        folderDeleteOperation( ue4_config.project.RelativeOutputDirectory )
    ] )

    buildgraph_params = [
        "Clean" : params.CLEAN_PROJECT,
        "ProjectDir" : env.WORKSPACE,
        "BuildConfiguration": params.DEBUG_BUILDS ? "Debug" : "Development",
        "OutputDir": "${env.WORKSPACE}\\${ue4_config.project.RelativeOutputDirectory}"
    ]

    ue4DataValidation ue4_config, buildgraph_params

    if ( ue4_config.project.Tests.Run ) {
        ue4RunTests ue4_config
    }

    buildgraph_params[ "ArchivePackage" ] = ue4_config.project.Archive
    buildgraph_params[ "ZipPackage" ] = ue4_config.project.Archive && ue4_config.project.Zip

    def tasks = [:]

    ue4_config.project.Package.Targets.each { iterator -> 
        def target = iterator.Target

        tasks[ "${target.Type} - ${target.Platform}" ] = {
            ue4PackageTarget target.Type, target.Platform, ue4_config, buildgraph_params
        }
    }

    parallel tasks
}