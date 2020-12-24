#!/usr/bin/groovy

def call() {

    fileOperations( [ 
        fileDeleteOperation( excludes: '', includes: 'Saved\\Logs\\*.*' )
    ] )
}