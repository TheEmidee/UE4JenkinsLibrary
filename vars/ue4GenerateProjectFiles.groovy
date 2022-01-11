#!/usr/bin/groovy

def call( ue4_config ) {
    if ( ue4_config.Options.Stub ) {
        echo "Would generate the project files"
    } else {
        def UE4 = new unreal.UE4()
        UE4.initialize( ue4_config )

        UE4.generateProjectFiles()
    }
}