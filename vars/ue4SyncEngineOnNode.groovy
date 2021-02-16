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

        def ue4_version_to_sync = getUE4FileToSync( ue4_config )
        if ( ue4_version_to_sync == "" ) {
            log.info "No need to sync"
        } else {
            log.warning "Must Sync Engine on node ${env.NODE_NAME} with version ${ue4_version_to_sync}"

            ue4_version_to_sync = "UE${ue4_version_to_sync}.7z"
            syncUEOnNode ue4_config, ue4_version_to_sync
        }
    }
}

def getUE4FileToSync( ue4_config ) {
    def jenkins_build_version = "JenkinsBuild.version"
    def jenkins_build_version_reference = "${jenkins_build_version}.reference"
    def jenkins_build_version_local = "${jenkins_build_version}.local"
    def saved_jenkins_build_version = "Saved\\${jenkins_build_version}"
    def saved_jenkins_build_version_reference = "Saved\\${jenkins_build_version_reference}"
    def saved_jenkins_build_version_local = "Saved\\${jenkins_build_version_local}"

    fileOperations( [ fileDeleteOperation( excludes: '', includes: 'Saved\\JenkinsBuild.*' ) ] )

    log.info "Scan ${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}"

    File dir = new File( "${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}" );
    String[] list = dir.list();

    log.info "Found ${list.length} files"

    String extension = ".7z"
    String prefix = "UE"

    List version_numbers = []

    list.each { ite ->
        log.info ite

        if ( ite.endsWith( extension ) ) {
            String version_number = ite.substring( prefix.length(), ite.length() - extension.length() )
            log.info version_number

            version_numbers << version_number
        }
    }

    String most_recent_version = mostRecentVersion( version_numbers )

    log.info "Most recent version in the remote location: ${most_recent_version}"

    // Now copy from the engine location on the node the JenkinsBuild.version file into the Saved folder, and name it JenkinsBuild.version.local
    if ( !roboCopy( "${ue4_config.Engine.Location}\\Engine\\Build", "${env.WORKSPACE}\\Saved", jenkins_build_version ) ) {
        log.warning "Failed to copy ${jenkins_build_version}"
        return most_recent_version
    }

    exists = fileExists saved_jenkins_build_version

    if ( !exists ) {
        log.warning "Could not find a JenkinsBuild.version file in the local folder"
        return most_recent_version
    }

    log.info "Rename ${saved_jenkins_build_version} into ${saved_jenkins_build_version_local}"
    fileOperations( [ fileRenameOperation( destination: saved_jenkins_build_version_local, source: saved_jenkins_build_version ) ] )

    List tokens = most_recent_version.tokenize( '.' )
    def version_reference = tokens[ tokens.size - 1 ]
    def version_local = readFile encoding: 'utf-8', file: saved_jenkins_build_version_local

    log.info "UE4 Reference version ${version_reference}"
    log.info "UE4 Local version ${version_local}"

    if ( version_reference != version_local ) {
        log.warning "Different UE versions"
        return most_recent_version
    }

    return ""
}

@NonCPS
String mostRecentVersion(List versions) {
  def sorted = versions.sort(false) { a, b -> 

    List verA = a.tokenize('.')
    List verB = b.tokenize('.')

    def commonIndices = Math.min(verA.size(), verB.size())

    for (int i = 0; i < commonIndices; ++i) {
      def numA = verA[i].toInteger()
      def numB = verB[i].toInteger()

      if (numA != numB) {
        return numA <=> numB
      }
    }

    // If we got this far then all the common indices are identical, so whichever version is longer must be more recent
    verA.size() <=> verB.size()
  }

  println "sorted versions: $sorted"
  sorted[-1]
}

def syncUEOnNode( ue4_config, String ue4_version_to_sync ){
    copyArchiveOnNode( ue4_config, ue4_version_to_sync )
    extractArchive( ue4_config, ue4_version_to_sync )
    deleteArchive( ue4_config, ue4_version_to_sync )
}

def copyArchiveOnNode( ue4_config, String ue4_version_to_sync ) {
    def reference_engine_location = "${ue4_config.Engine.ReferenceBuildLocation}\\${ue4_config.Engine.Version}"

    roboCopy( reference_engine_location, ue4_config.Engine.Location, ue4_version_to_sync )
}

def extractArchive( ue4_config, String ue4_version_to_sync ) {
    powershell "Get-ChildItem -Path ${ue4_config.Engine.Location} -Directory -Recurse | Remove-Item -force -recurse -ErrorAction SilentlyContinue"
    powershell "& \"C:\\Program Files\\7-Zip\\7z.exe\" x -aoa ${ue4_config.Engine.Location}\\${ue4_version_to_sync} \"-o${ue4_config.Engine.Location}\" -y -mmt=on"
}

def deleteArchive( ue4_config, String ue4_version_to_sync ) {
    def zip_path = "${ue4_config.Engine.Location}\\${ue4_version_to_sync}"

    powershell "Remove-Item -Path ${zip_path} -Force"
}