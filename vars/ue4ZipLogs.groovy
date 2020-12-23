#!/usr/bin/groovy

def call( String zip_file_name, boolean archive_zip = true ) {

    def logs_zip_file_name = "Saved\\${zip_file_name}_Logs.zip"
    zip archive: archive_zip, dir: 'Saved\\Logs\\', glob: '', zipFile: logs_zip_file_name
}