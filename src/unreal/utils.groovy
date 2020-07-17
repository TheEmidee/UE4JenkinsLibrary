#!/usr/bin/groovy

package unreal;

enum BranchType {
    Development,
    Release,
    Master,
    PullRequest
}

enum DeploymentEnvironment {
    Development,
    Release,
    Shipping,
    PullRequest
}

enum BuildConfiguration {
    Development,
    Test,
    Shipping,
    DebugGame
}

def initializeNode(Script script) {
    log.info "Initialize Node"

    log.info "UE4_ROOT : ${script.env.UE4_ROOT}"
    if ( script.env.UE4_ROOT == null || script.env.UE4_ROOT.isEmpty() ) {
        log.fatal "Missing environment variable UE4_ROOT. Add it to the node properties."
    }

    script.env.UE4_ROOT_WINDOWS = "${script.env.UE4_ROOT}\\Windows"

    global_workspace = new File( script.env.WORKSPACE ).parent
    project_workspace_name = script.env.PROJECT_NAME
    project_workspace_name += ( script.env.DEPLOYMENT_ENVIRONMENT as DeploymentEnvironment ) == DeploymentEnvironment.Shipping
        ? "_Master"
        : "_Develop"

    script.env.WORKSPACE = new File( global_workspace, project_workspace_name )
    log.info "Workspace : ${script.env.WORKSPACE}"
}

def initializeEnvironment(Script script) {
    log.info "InitializeEnvironment"

    branch_type = getBranchType( script.env.BRANCH_NAME )
    deployment_environment = getBranchDeploymentEnvironment( branch_type )
    client_config = getClientConfig( deployment_environment )

    script.env.PROJECT_NAME = getProjectName( script )
    log.info "ProjectName : ${script.env.PROJECT_NAME}"

    script.env.BRANCH_TYPE = branch_type
    log.info "BranchType : ${script.env.BRANCH_TYPE}"
    
    script.env.DEPLOYMENT_ENVIRONMENT = deployment_environment
    log.info "DeploymentEnvironment ${script.env.DEPLOYMENT_ENVIRONMENT}"

    script.env.CLIENT_CONFIG = client_config
    log.info "ClientConfiguration : ${script.env.CLIENT_CONFIG}"
}

def getGitHubPRTitle( github_token ) {
    branch_name = env.GIT_BRANCH
    if (env.BRANCH_NAME != null) {
        branch_name = env.BRANCH_NAME
    }

    pr_id = branch_name.substring( 3 )
    echo "PR ID : ${pr_id}"

    String url = "https://api.github.com/repos/FishingCactus/${env.PROJECT_NAME}/pulls/${pr_id}"
    
    def text = url.toURL().getText( requestProperties: [ 'Authorization' : "token ${github_token}" ] )
    def json = new JsonSlurper().parseText( text )

    log.info "PR Title : ${json.title}"
    return json.title
}

def getProjectName(def script) {
    split_result = "${script.env.JOB_NAME}".split('/')
    project_name = split_result.length > 1 ? split_result[split_result.length - 2] : split_result.max()
    return project_name
}

def getBranchType( String branch_name ) {
    if ( branch_name =~ ".*develop" ) {
        return BranchType.Development
    } else if ( branch_name =~ ".*release/.*" ) {
        return BranchType.Release
    } else if ( branch_name =~ ".*master" ) {
        return BranchType.Master
    }

    return BranchType.PullRequest
}

def getBranchDeploymentEnvironment( BranchType branch_type ) {
    switch ( branch_type ) {
        case BranchType.Development:
            return DeploymentEnvironment.Development
        case BranchType.Development:
            return DeploymentEnvironment.Release
        case BranchType.Master:
            return DeploymentEnvironment.Shipping
        default:
            return DeploymentEnvironment.PullRequest
    }
}

def getClientConfig( DeploymentEnvironment deployment_environment ) {
    switch ( deployment_environment ) {
        case DeploymentEnvironment.Shipping:
            return BuildConfiguration.Shipping
        default:
            return BuildConfiguration.Development
    }
}

return this