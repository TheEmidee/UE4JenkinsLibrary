#!/usr/bin/groovy

def call() {
    def buildgraph_params = [
        "Clean" : params.CLEAN_PROJECT == null ? false : params.CLEAN_PROJECT,
        "ProjectDir" : env.WORKSPACE,
        "BuildConfiguration": params.DEBUG_BUILDS == null ? "Development" : ( params.DEBUG_BUILDS ? "Debug" : "Development" ),
        "IsBuildMachine" : "true"
    ]

    return buildgraph_params
}