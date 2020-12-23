#!/usr/bin/groovy

def call( String type, String platform, ue4_config, buildgraph_params ) {
    def zip_file_name = "${ue4_config.project.Name}_${type}_${platform}"
    def relative_zip_file_path = "${ue4_config.project.RelativeOutputDirectory}\\${zip_file_name}.zip"
    
    buildgraph_params[ "ZipFile" ] = "${env.WORKSPACE}\\${relative_zip_file_path}"

    def buildgraph_task_name = "Package ${ue4_config.project.Name} ${type} ${platform}"

    stage( buildgraph_task_name ) {

        fileOperations( [ 
            fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' )
        ] )
        
        runBuildGraph(
            ue4_config,
            buildgraph_task_name,
            buildgraph_params
        )

        if ( ue4_config.project.MustPackage ) {
            archiveArtifacts artifacts: relative_zip_file_path, followSymlinks: false, onlyIfSuccessful: true
        }

        publishIssues name: "MSBuild Issues", issues: msbuild_reports, qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true], [threshold: 1, type: 'NEW', unstable: false]]
        
        def logs_zip_file_name = "Saved\\${zip_file_name}_Logs.zip"
        zip archive: true, dir: 'Saved\\Logs\\', glob: '', zipFile: logs_zip_file_name
    }
}