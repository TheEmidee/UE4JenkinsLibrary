#!/usr/bin/groovy

def call() {
    if ( mustSyncUE() ) {
        echo "Must Sync"
        //roboCopy( env.UE4SharedFolder, env.UE4_ROOT, "/J /NOOFFLOAD /S /R:5 /W:5 /TBD /NP /V /MT:16 /MIR" )
        echo "roboCopy from ${env.UE4SharedFolder} to ${env.UE4_ROOT} with args /J /NOOFFLOAD /S /R:5 /W:5 /TBD /NP /V /MT:16 /MIR"
    } else {
        echo "No need to sync"
    }
}

def mustSyncUE() {
    fileOperations([fileDeleteOperation(excludes: '', includes: 'Saved\\JenkinsBuild.*')])

    roboCopy( "${env.UE4SharedFolder}\\Engine\\Build", "${env.WORKSPACE}\\Saved", "JenkinsBuild.version" )
    def exists = fileExists "Saved\\JenkinsBuild.version"
    if ( !exists ) {
        echo "Could not find a JenkinsBuild.version file in the network folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: "Saved\\JenkinsBuild.version.reference", source: "Saved\\JenkinsBuild.version")])
    
    roboCopy( "${env.UE4_ROOT}\\Windows\\Engine\\Build", "${env.WORKSPACE}\\Saved", "JenkinsBuild.version" )
    exists = fileExists "Saved\\JenkinsBuild.version"

    if ( !exists ) {
        echo "Could not find a JenkinsBuild.version file in the local folder"
        return true
    }
    fileOperations([fileRenameOperation(destination: "Saved\\JenkinsBuild.version.local", source: "Saved\\JenkinsBuild.version")])

    def version_reference = readFile encoding: 'utf-8', file: 'Saved\\JenkinsBuild.version.reference'
    def version_local = readFile encoding: 'utf-8', file: 'Saved\\JenkinsBuild.version.local'

    echo "UE4 Reference version ${version_reference}"
    echo "UE4 Local version ${version_local}"

    if ( version_reference != version_local ) {
        echo "Different UE versions"
        return true
    }

    return false
}