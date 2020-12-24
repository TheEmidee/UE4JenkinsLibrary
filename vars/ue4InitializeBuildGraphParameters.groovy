#!/usr/bin/groovy

def call() {
    def buildgraph_params = [
        "Clean" : params.CLEAN_PROJECT,
        "ProjectDir" : env.WORKSPACE,
        "BuildConfiguration": params.DEBUG_BUILDS ? "Debug" : "Development"
    ]

    return buildgraph_params
}