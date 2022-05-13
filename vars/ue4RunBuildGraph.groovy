#!/usr/bin/groovy

def call( ue4_config, String task, Map parameters = [:] ) {
    stage( task ) {
        if ( ue4_config.Options.Stub ) {
            echo "Would run build graph task ${task} with parameters ${parameters}"
            return
        }

        fileOperations( [ 
            fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' )
        ] )

        def UE4 = new unreal.UE4()
        UE4.initialize( ue4_config )
        UE4.runBuildGraph( task, parameters )
    }
}