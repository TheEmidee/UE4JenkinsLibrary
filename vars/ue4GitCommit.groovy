#!/usr/bin/groovy

def call( ue4_config, String commit_message, String files_pattern ) {
    
    def ssh_credentials = ue4_config.Git.SSHAgentCredentials
    def git_username = ue4_config.Git.UserName
    def git_email = ue4_config.Git.Email
    def git_commit_message_prefix = ue4_config.Git.CommitMessagePrefix

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

    def git_branch = getBranchName()
    def origin_str = "origin/"

    if ( git_branch.startsWith( origin_str ) ) {
        git_branch = git_branch.substring( origin_str.length() )
    }

    ue4Bat( ue4_config, "git switch -C ${git_branch} HEAD" )
    ue4Bat( ue4_config, "git config user.email ${git_email}" )
    ue4Bat( ue4_config, "git config user.name ${git_username}" )
    ue4Bat( ue4_config, "git add ${files_pattern}" )
    ue4Bat( ue4_config, "git commit -m \"${git_commit_message_prefix} ${commit_message}\" -n" )
}