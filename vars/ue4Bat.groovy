#!/usr/bin/groovy

def call( ue4_config, command ) {

    if ( ue4_config.Options.Stub ) {
        echo "Would execute: bat ${command}"
    } else {
        bat command
    }
}