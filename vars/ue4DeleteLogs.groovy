#!/usr/bin/groovy

def call( ue4_config ) {
    if ( ue4_config.Options.Stub ) {
        echo "Would delete files in the Logs folder"
    } else {
        fileOperations( [ 
            fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' )
        ] )
    }
}