#!/usr/bin/groovy

def call() {
    log.info "Check if the engine must be synchronized on the node ${env.NODE_NAME}"

    if ( mustSyncUE() ) {
        log.warning "Must Sync Engine on node ${env.NODE_NAME}"
        stage ( "SyncUE" ) {
            roboCopy( env.UE4_REFERENCE_BUILD_LOCATION, env.UE4_ROOT_WINDOWS, "/J /NOOFFLOAD /S /R:5 /W:5 /TBD /NP /V /MT:16 /MIR" )
        }
    } else {
        log.info "No need to sync"
    }
}

def mustSyncUE() {
    fileOperations([fileDeleteOperation(excludes: '', includes: 'Saved\\JenkinsBuild.*')])

    roboCopy( "${env.UE4_REFERENCE_BUILD_LOCATION}\\Engine\\Build", "${env.WORKSPACE}\\Saved", "JenkinsBuild.version" )
    def exists = fileExists "Saved\\JenkinsBuild.version"
    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the network folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: "Saved\\JenkinsBuild.version.reference", source: "Saved\\JenkinsBuild.version")])
    
    roboCopy( "${env.UE4_ROOT}\\Windows\\Engine\\Build", "${env.WORKSPACE}\\Saved", "JenkinsBuild.version" )
    exists = fileExists "Saved\\JenkinsBuild.version"

    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the local folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: "Saved\\JenkinsBuild.version.local", source: "Saved\\JenkinsBuild.version")])

    def version_reference = readFile encoding: 'utf-8', file: 'Saved\\JenkinsBuild.version.reference'
    def version_local = readFile encoding: 'utf-8', file: 'Saved\\JenkinsBuild.version.local'

    log.info "UE4 Reference version ${version_reference}"
    log.info "UE4 Local version ${version_local}"

    if ( version_reference != version_local ) {
        log.warning "Different UE versions"
        return true
    }

    return false
}