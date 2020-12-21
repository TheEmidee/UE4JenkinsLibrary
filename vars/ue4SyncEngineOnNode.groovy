#!/usr/bin/groovy

def call( config ) {
    stage ( "SyncUE" ) {
        log.info "Check if the engine must be synchronized on the node ${env.NODE_NAME}"

        if ( mustSyncUE( config ) ) {
            log.warning "Must Sync Engine on node ${env.NODE_NAME}"
            roboCopy( config.engine.ReferenceBuildLocation, env.UE4_ROOT_WINDOWS, "/J /NOOFFLOAD /S /R:5 /W:5 /TBD /NP /V /MT:16 /MIR" )
        } else {
            log.info "No need to sync"
        }
    }
}

def mustSyncUE( config ) {
    def jenkins_build_version = "JenkinsBuild.version"
    def jenkins_build_version_reference = "${jenkins_build_version}.reference"
    def jenkins_build_version_local = "${jenkins_build_version}.local"
    def saved_jenkins_build_version = "Saved\\${jenkins_build_version}"
    def saved_jenkins_build_version_reference = "Saved\\${jenkins_build_version_reference}"
    def saved_jenkins_build_version_local = "Saved\\${jenkins_build_version_local}"

    fileOperations([fileDeleteOperation(excludes: '', includes: 'Saved\\JenkinsBuild.*')])

    roboCopy( "${config.engine.ReferenceBuildLocation}\\Engine\\Build", "${env.WORKSPACE}\\Saved", jenkins_build_version )
    def exists = fileExists saved_jenkins_build_version
    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the network folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: saved_jenkins_build_version_reference, source: saved_jenkins_build_version)])
    
    try {
        roboCopy( "${env.UE4_ROOT}\\Windows\\Engine\\Build", "${env.WORKSPACE}\\Saved", jenkins_build_version )
    } catch ( Exception e ) {
        return true
    }
    exists = fileExists saved_jenkins_build_version

    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the local folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: saved_jenkins_build_version_local, source: saved_jenkins_build_version )])

    def version_reference = readFile encoding: 'utf-8', file: saved_jenkins_build_version_reference
    def version_local = readFile encoding: 'utf-8', file: saved_jenkins_build_version_local

    log.info "UE4 Reference version ${version_reference}"
    log.info "UE4 Local version ${version_local}"

    if ( version_reference != version_local ) {
        log.warning "Different UE versions"
        return true
    }

    return false
}