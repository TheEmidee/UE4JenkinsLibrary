#!/usr/bin/groovy

package unreal;

def UAT_PATH = ''
def UE4_CMD_PATH = ''
def ScriptInvocationType = ''
def BatchDir = ''
def ProjectRootFolder = ''
def ProjectName = ''
def EngineRootFolder = ''
def DefaultArguments = ''

def initialize( String project_name, String project_root_folder, String engine_root_folder, String default_arguments = '' )
{
    ProjectName = project_name
    ProjectRootFolder = project_root_folder
    EngineRootFolder = engine_root_folder

    ProjectPath = "${project_root_folder}/${project_name}.uproject"

    BatchDir = isUnix() 
                    ? "${engine_root_folder}/Engine/Build/BatchFiles/Linux" 
                    : "${engine_root_folder}/Engine/Build/BatchFiles"
    ScriptInvocationType = isUnix() ?  "sh" : "bat"

    UAT_PATH = "\"${engine_root_folder}/Engine/Build/BatchFiles/RunUAT.${ScriptInvocationType}\""
    UE4_CMD_PATH = "\"${engine_root_folder}/Engine/Binaries/Win64/UE4Editor-Cmd.exe\""

    DefaultArguments = default_arguments
}

// script_path is the location of the XML file relative to the project root folder used in the initialize function
def runBuildGraph( String script_path, String target, BuildConfiguration build_configuration, def parameters = [:] ) {
    String parsed_parameters = ""

    parameters.each
    {
        parameter -> parsed_parameters += "-set:${parameter.key}=\"${parameter.value}\" "
    }

    full_script_path = "${ProjectRootFolder}/${script_path}"

    RunCommand( "${UAT_PATH} BuildGraph ${DefaultArguments} -target=\"${target}\" -script=\"${full_script_path}\" -set:ProjectPath=\"${ProjectPath}\" ${parsed_parameters}" )
}

def buildDDC() {
    RunCommand( "${UE4_CMD_PATH} ${ProjectFile} -run=DerivedDataCache -fill ${DefaultArguments}" )
}

def generateProjectFiles() {
    RunCommand( "\"${BatchDir}/GenerateProjectFiles.${ScriptInvocationType}\" -projectfiles -project=${ProjectFile} -game -engine -progress ${DefaultArguments}" )
}

def RunCommand( def Command ) {
    if(isUnix()) {
        sh( script: Command )
    } else {
        bat( script: Command )
    }
}

return this