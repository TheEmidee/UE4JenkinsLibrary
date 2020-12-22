#!/usr/bin/groovy

def call( ue4_config, List tasks, Map parameters = [:] ) {

    def UE4 = new unreal.UE4()
    UE4.initialize( ue4_config )

    String all_tasks = ""
    tasks.each {
        all_tasks += "${it},"
    }

    UE4.runBuildGraph( all_tasks, parameters )
}