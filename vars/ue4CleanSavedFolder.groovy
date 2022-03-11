#!/usr/bin/groovy

def call( ue4_config ) {
    if ( ue4_config.Options.Stub ) {
        echo "Would delete files in the Saved folder"
    } else {
        fileOperations( [ 
            fileDeleteOperation( excludes: '', includes: 'Saved\\*.zip' ), 
            fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' ), 
            fileDeleteOperation( excludes: '', includes: 'Saved\\UnitTestsReport\\*.*'),
            folderDeleteOperation( 'Saved\\LocalBuilds' ),
            folderDeleteOperation( 'Saved\\Tests' ),
            folderDeleteOperation( ue4_config.Project.RelativeOutputDirectory )
        ] )

        if ( ue4_config.Project.DeleteSavedConfigFolder ) {
            fileOperations( [ 
                folderDeleteOperation( 'Saved\\Config' ),
            ])
        }
    }
}