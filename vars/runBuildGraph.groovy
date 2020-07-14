#!/usr/bin/groovy

def call( List tasks, Map parameters = [:] , Closure pre_steps = null, Closure post_steps = null ) {

    node('UE4') {
        def utils = new unreal.utils()
        utils.initializeNode(this)

        def build_configuration = env.CLIENT_CONFIG as unreal.BuildConfiguration
        skipDefaultCheckout()

        ws( env.WORKSPACE ) {
            if ( pre_steps != null ) {
                pre_steps()
            }

            if ( parameters.containsKey( "ArchivePackage" ) && parameters[ "ArchivePackage" ] == true ) {
                dir( env.RELATIVE_ARCHIVE_DIRECTORY ) {
                    deleteDir()
                }
                dir( env.RELATIVE_PACKAGE_DIRECTORY ) {
                    deleteDir()
                }
            }

            stage( 'Checkout' ) {
                checkout scm
            }

            parameters[ "OutputDir" ] = env.ABSOLUTE_PACKAGE_DIRECTORY

            def UE4 = new unreal.UE4()
            UE4.initialize( env.PROJECT_NAME, env.WORKSPACE, env.UE4_ROOT )

            tasks.each {
                stage( it ) {
                    UE4.runBuildGraph( env.BUILD_GRAPH_PATH, it, build_configuration, parameters )
                }
            }

            if ( post_steps != null ) {
                post_steps()
            }
        }
    }
}