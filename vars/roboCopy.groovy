#!/usr/bin/groovy

def call( source, destination, arguments ) {
    def status = bat returnStatus: true, script: "robocopy.exe ${source} ${destination} ${arguments}"

    if (status < 0 || status > 3) {
        log.fatal( "ROBOCOPY failed" )
    }
}