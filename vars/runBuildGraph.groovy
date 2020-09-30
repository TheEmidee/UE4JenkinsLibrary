#!/usr/bin/groovy

def call( List tasks, Map parameters = [:], String default_arguments = "" ) {

    def UE4 = new unreal.UE4()
    UE4.initialize( env.PROJECT_NAME, env.WORKSPACE, env.UE4_ROOT_WINDOWS, default_arguments )

    tasks.each {
        stage( it ) {
            UE4.runBuildGraph( env.UE4_RELATIVE_BUILD_GRAPH_PATH, it, parameters )
        }
    }
}