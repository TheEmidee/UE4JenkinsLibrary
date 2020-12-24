#!/usr/bin/groovy

def call() {
    def buildgraph_params = [
        "Clean" : params.CLEAN_PROJECT == null ? false : params.CLEAN_PROJECT,
        "ProjectDir" : env.WORKSPACE,
        "BuildConfiguration": params.DEBUG_BUILDS == null ? "Development" : ( params.DEBUG_BUILDS ? "Debug" : "Development" )
    ]

    return buildgraph_params
}