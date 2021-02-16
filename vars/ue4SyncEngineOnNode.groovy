#!/usr/bin/groovy

def call( ue4_config ) {
    stage ( "SyncUE" ) {
        log.info "Check if the engine must be synchronized on the node ${env.NODE_NAME}"

        if ( ue4_config.Engine.Version == null || ue4_config.Engine.Version == "" ) {
            error "You must define the UE4 engine version in the config file"
            return
        }

        if ( ue4_config.Engine.ReferenceBuildLocation == null || ue4_config.Engine.ReferenceBuildLocation == "" ) {
            error "You must define the UE4 ReferenceBuildLocation in the config file"
            return
        }

        if ( mustSyncUE( ue4_config ) ) {
            log.warning "Must Sync Engine on node ${env.NODE_NAME}"
            syncUEOnNode ue4_config
        } else {
            log.info "No need to sync"
        }
    }
}

def mustSyncUE( ue4_config ) {
    def jenkins_build_version = "JenkinsBuild.version"
    def jenkins_build_version_reference = "${jenkins_build_version}.reference"
    def jenkins_build_version_local = "${jenkins_build_version}.local"
    def saved_jenkins_build_version = "Saved\\${jenkins_build_version}"
    def saved_jenkins_build_version_reference = "Saved\\${jenkins_build_version_reference}"
    def saved_jenkins_build_version_local = "Saved\\${jenkins_build_version_local}"

    fileOperations( [ fileDeleteOperation( excludes: '', includes: 'Saved\\JenkinsBuild.*' ) ] )


    File dir = new File( "${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}" );
    String[] list = dir.list(filter);

    list.each { ite ->
        log.info ite
    }

    // First copy from the network share the JenkinsBuild.version file into the Saved folder, and name it JenkinsBuild.version.reference
    def reference_engine_location = "${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}"

    if ( !roboCopy( reference_engine_location, "${env.WORKSPACE}\\Saved", jenkins_build_version ) ) {
        error "Failed to copy ${jenkins_build_version} from ${reference_engine_location}"
        return false
    }

    def exists = fileExists saved_jenkins_build_version
    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the network folder"
        return true
    }

    log.info "Rename ${saved_jenkins_build_version} into ${saved_jenkins_build_version_reference}"
    fileOperations( [ fileRenameOperation( destination: saved_jenkins_build_version_reference, source: saved_jenkins_build_version ) ] )

    // Now copy from the engine location on the node the JenkinsBuild.version file into the Saved folder, and name it JenkinsBuild.version.local
    if ( !roboCopy( "${ue4_config.Engine.Location}\\Engine\\Build", "${env.WORKSPACE}\\Saved", jenkins_build_version ) ) {
        log.warning "Failed to copy ${jenkins_build_version}"
        return true
    }

    exists = fileExists saved_jenkins_build_version

    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the local folder"
        return true
    }

    log.info "Rename ${saved_jenkins_build_version} into ${saved_jenkins_build_version_local}"
    fileOperations( [ fileRenameOperation( destination: saved_jenkins_build_version_local, source: saved_jenkins_build_version ) ] )

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

def syncUEOnNode( ue4_config ){
    copyArchiveOnNode( ue4_config )
    extractArchive( ue4_config )
    deleteArchive( ue4_config )
}

def copyArchiveOnNode( ue4_config ) {
    def reference_engine_location = "${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}"

    roboCopy( reference_engine_location, ue4_config.Engine.Location, "UE4.zip" )
}

def extractArchive( ue4_config ) {
    powershell "& \"C:\\Program Files\\7-Zip\\7z.exe\" x -aoa ${ue4_config.Engine.Location}\\UE4.zip \"-o${ue4_config.Engine.Location}\" -y"
}

def deleteArchive( ue4_config ) {
    def zip_path = "${ue4_config.Engine.Location}\\UE4.zip"

    powershell "Remove-Item -Path ${zip_path} -Force"
}