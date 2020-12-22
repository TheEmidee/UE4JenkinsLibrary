#!/usr/bin/groovy

def call( ue4_config, List tasks, Map parameters = [:] ) {

    def UE4 = new unreal.UE4()
    UE4.initialize( ue4_config )

    tasks.each {
        UE4.runBuildGraph( it, parameters )
    }
}