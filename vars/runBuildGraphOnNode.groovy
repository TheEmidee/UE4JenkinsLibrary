#!/usr/bin/groovy

def call( List tasks, Map parameters = [:], String default_arguments = "", Closure pre_steps = null, Closure post_steps = null, Closure error_steps = null, Closure finally_steps = null ) {

    node('UE4') {
        def utils = new unreal.utils()
        utils.initializeNode(this)

        skipDefaultCheckout()

        ws( env.WORKSPACE ) {

            if ( env.UE4_SYNC_ENGINE_ON_NODES ) {
                syncUEOnNode()
            }

            if ( pre_steps != null ) {
                pre_steps()
            }

            parameters[ "ProjectDir" ] = env.WORKSPACE

            stage( 'Checkout' ) {
                checkout scm
            }

            def UE4 = new unreal.UE4()
            UE4.initialize( env.PROJECT_NAME, env.WORKSPACE, env.UE4_ROOT_WINDOWS, default_arguments )

            try {
                tasks.each {
                    stage( it ) {
                        UE4.runBuildGraph( env.UE4_RELATIVE_BUILD_GRAPH_PATH, it, parameters )
                    }

                    if ( post_steps != null ) {
                        post_steps()
                    }
                }
            }
            catch ( e ) {
                if ( error_steps != null ) {
                    error_steps()
                }
                throw e
            }
            finally {
                if ( finally_steps != null ) {
                    finally_steps()
                }
            }
        }
    }
}