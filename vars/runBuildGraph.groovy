#!/usr/bin/groovy

def call( List tasks, Map parameters = [:], String default_arguments = "", Closure pre_steps = null, Closure post_steps = null ) {

    node('UE4') {
        def utils = new unreal.utils()
        utils.initializeNode(this)

        def build_configuration = env.CLIENT_CONFIG as unreal.BuildConfiguration
        skipDefaultCheckout()

        ws( env.WORKSPACE ) {

            if ( env.UE4_SYNC_ENGINE_ON_NODES ) {
                echo "env.UE4_SYNC_ENGINE_ON_NODES: " + env.UE4_SYNC_ENGINE_ON_NODES
                syncUEOnNode()
            }

            if ( pre_steps != null ) {
                pre_steps()
            }

            stage( 'Checkout' ) {
                checkout scm
            }

            def UE4 = new unreal.UE4()
            UE4.initialize( env.PROJECT_NAME, env.WORKSPACE, env.UE4_ROOT, default_arguments )

            tasks.each {
                stage( it ) {
                    UE4.runBuildGraph( env.UE4_RELATIVE_BUILD_GRAPH_PATH, it, build_configuration, parameters )
                }
            }

            if ( post_steps != null ) {
                post_steps()
            }
        }
    }
}