#!/usr/bin/groovy

def info(message) {
    echo "INFO: ${message}"
}

def warning(message) {
    echo "WARNING: ${message}"
}

def fatal(message) {
    echo "ERROR: ${message}"
    currentBuild.result = "ABORTED"
    error( message )
}