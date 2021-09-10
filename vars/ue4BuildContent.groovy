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
            def ssh_credentials = ue4_config.Git.SSHAgentCredentials
            def git_username = ue4_config.Git.UserName
            def git_email = ue4_config.Git.Email

            if ( !ssh_credentials?.trim() ) {
                log.warning "Can not commit content because the option Git.SSHAgentCredentials is not set"
                return
            }

            if ( !git_username?.trim() ) {
                log.warning "Can not commit content because the option Git.UserName is not set"
                return
            }

            if ( !git_email?.trim() ) {
                log.warning "Can not commit content because the option Git.Email is not set"
                return
            }

            def git_branch = env.GIT_BRANCH
            def origin_str = "origin/"
            if ( git_branch.startsWith( origin_str ) ) {
                git_branch = git_branch.substring( origin_str.length() )
            }

            sshagent( [ ssh_credentials ] ) {
                bat "git config user.email ${git_email}"
                bat "git config user.name ${git_username}"
                bat "git switch -C ${git_branch} HEAD"
                bat "git commit -am \"Built Content\" -n"
                bat "git push --set-upstream origin ${git_branch}"
            }
        }
    }
}