#!/usr/bin/groovy

def call( ue4_config ) {
    if ( ue4_config.Options.Stub ) {
        echo "Would delete the folder Saved\\Tests"
    } else {
        fileOperations( [ 
            folderDeleteOperation( 'Saved\\Tests\\' ) 
        ] )
    }
}