#!/usr/bin/groovy

def call( ue4_config, buildgraph_params ) {

    if ( !ue4_config.Project.BuildContent.CanBuildContent ) {
        log.info( "Build Content disabled" )
        return
    }

    def buildgraph_task_name = ue4_config.Project.BuildContent.BuildGraphTaskName

    if ( !buildgraph_task_name?.trim() ) {
        log.warning "No Build Content will be done. Did you forget to fill in the BuildContent.BuildGraphTaskName section of the config file?"
        return
    }

    stage( "Build Content" ) {
        bat 'git checkout -- Content'

        def buildlighting_quality = ue4_config.Project.BuildContent.BuildLighting_Quality

        if ( buildlighting_quality?.trim() ) {
            buildgraph_params[ "BuildLighting_Quality" ] = buildlighting_quality
        }

        ue4RunBuildGraph( 
            ue4_config,
            buildgraph_task_name,
            buildgraph_params
            )

        ue4ParseLogs( ue4_config.Project.BuildContent.LogParsers )

        if ( ue4_config.Project.BuildContent.CanCommitContent ) {
            ue4GitCommit(
                ue4_config, 
                "Built Content"
            )

            if ( ue4_config.Project.BuildContent.CanPushContent ) {
                sshagent( [ ssh_credentials ] ) {
                    bat "git push --set-upstream origin ${git_branch}"
                }
            }
        }
    }
}