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
        "ArchivePackage" : ue4_config.project.MustPackage,
        // Don't zip in buildgraph. We'll zip both client and server for Win64 in one go in jenkins
        "ZipPackage" : false,
        "OutputDir" : "${env.WORKSPACE}\\${ue4_config.project.RelativeOutputDirectory}\\Win64"
    ]
}