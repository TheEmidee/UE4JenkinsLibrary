#!/usr/bin/groovy

package unreal;

def UAT_PATH = ''
def UE4_CMD_PATH = ''
def UBT_PATH = ''
def ScriptInvocationType = ''
def BatchDir = ''
def ProjectRootFolder = ''
def ProjectName = ''
def EngineRootFolder = ''
def DefaultArguments = ''

def initialize( config )
{
    //String project_name, String project_root_folder, String engine_root_folder, String default_arguments = ''

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
    UBT_PATH = "\"${engine_root_folder}/Engine/Binaries/DotNET/UnrealBuildTool.exe\""

    DefaultArguments = default_arguments
}

// script_path is the location of the XML file relative to the project root folder used in the initialize function
def runBuildGraph( String script_path, String target, def parameters = [:] ) {
    String parsed_parameters = ""

    parameters.each
    {
        parameter -> parsed_parameters += "-set:${parameter.key}=\"${parameter.value}\" "
    }

    full_script_path = new File( ProjectRootFolder, script_path ).toString()

    RunCommand( "${UAT_PATH} BuildGraph ${DefaultArguments} -target=\"${target}\" -script=\"${full_script_path}\" -set:ProjectPath=\"${ProjectPath}\" ${parsed_parameters}" )
}

def generateProjectFiles() {
    RunCommand( "${UBT_PATH} -projectfiles -project=${ProjectPath} -game -rocket -vs2019 -progress" )
}

def RunCommand( def Command ) {
    if(isUnix()) {
        sh( script: Command )
    } else {
        bat( script: Command )
    }
}

return this