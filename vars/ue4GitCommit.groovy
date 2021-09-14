#!/usr/bin/groovy

def call( ue4_config, String commit_message ) {
    
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

    bat "git switch -C ${git_branch} HEAD"
    bat "git config user.email ${git_email}"
    bat "git config user.name ${git_username}"
    bat "git commit -am \"${commit_message}\" -n"
}