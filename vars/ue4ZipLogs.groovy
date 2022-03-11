#!/usr/bin/groovy

def call( ue4_config, String zip_file_name, boolean archive_zip = true ) {

    if ( ue4_config.Options.Stub ) {
        echo "Would zip log files in Saved\\Logs into Saved\\Logs_${zip_file_name}.zip"
    } else {
        def logs_zip_file_name = "Saved\\Logs_${zip_file_name}.zip"
        zip archive: archive_zip, dir: 'Saved\\Logs\\', glob: '', zipFile: logs_zip_file_name
    }
}