#!/usr/bin/groovy

def call() {

    fileOperations( [ 
        folderDeleteOperation( 'Saved\\Tests\\' ) 
    ] )
}