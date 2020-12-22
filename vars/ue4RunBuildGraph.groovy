#!/usr/bin/groovy

def call( ue4_config, String task, Map parameters = [:] ) {

    def UE4 = new unreal.UE4()
    UE4.initialize( ue4_config )
    UE4.runBuildGraph( task, parameters )
}