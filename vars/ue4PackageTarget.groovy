#!/usr/bin/groovy

def call( String type, String platform, ue4_config, buildgraph_params ) {
    def zip_file_name = "${ue4_config.Project.Name}_${type}_${platform}"
    def zip_file_name_with_extension = "${zip_file_name}.zip"
    def absolute_output_directory = "${env.WORKSPACE}\\${ue4_config.Project.RelativeOutputDirectory}"
    def absolute_zip_file_path = "${absolute_output_directory}\\${zip_file_name_with_extension}"
    def relative_zip_file_path = "${ue4_config.Project.RelativeOutputDirectory}\\${zip_file_name_with_extension}"

    buildgraph_params[ "Cook_ForceIterativeCooking" ] = ue4_config.Project.Package.ForceIterativeCooking
    buildgraph_params[ "ZipFile" ] = absolute_zip_file_path

    def buildgraph_task_name = "Package ${ue4_config.Project.Name} ${type} ${platform}"

    buildgraph_params[ "OutputDir" ] = "${absolute_output_directory}\\${type}\\${platform}"

    ue4DeleteLogs

    stage( buildgraph_task_name ) {
        ue4RunBuildGraph(
            ue4_config,
            buildgraph_task_name,
            buildgraph_params
        )

        if ( ue4_config.Project.Package.Zip ) {
            if ( ue4_config.Project.Package.ArchiveDirectory?.trim() ) {
                roboCopy( absolute_output_directory, ue4_config.Project.Package.ArchiveDirectory, zip_file_name_with_extension )
            }

            if ( ue4_config.Project.Package.ArchiveArtifactOnJenkins ) {
                archiveArtifacts artifacts: relative_zip_file_path, followSymlinks: false, onlyIfSuccessful: true
            }
        }

        def config_excluded_categories = ue4_config.Project.IssuesExcludedCategories.join('|')

        recordIssues(tools: [groovyScript(id: "BuildCookRun_${type}_${platform}", name: "BuildCookRun_${type}_${platform}", parserId: 'UE4_BuildCookRun', pattern: 'Saved/Logs/Log.txt')], , failOnError: true, filters: [excludeCategory( config_excluded_categories )], qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]])
        recordIssues tools: [msBuild(id: "MSBuild_${type}_${platform}", name: "MSBuild_${type}_${platform}")], failOnError: true, filters: [excludeCategory( 'ModuleManager|SwarmsEditor' )], qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false], [threshold: 1, type: 'TOTAL_NORMAL', unstable: true], [threshold: 1, type: 'NEW', unstable: false]]

        ue4ZipLogs zip_file_name
    }
}