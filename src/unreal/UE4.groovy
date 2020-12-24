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
def BuildGraphPath = ''

def initialize( ue4_config )
{
    ProjectName = ue4_config.Project.Name
    ProjectRootFolder = env.WORKSPACE
    EngineRootFolder = "${ue4_config.Engine.Location}/Windows"
    DefaultArguments = ue4_config.Engine.DefaultArguments

    ProjectPath = "${ProjectRootFolder}/${ProjectName}.uproject"

    BatchDir = isUnix() 
                    ? "${EngineRootFolder}/Engine/Build/BatchFiles/Linux" 
                    : "${EngineRootFolder}/Engine/Build/BatchFiles"
    ScriptInvocationType = isUnix() ?  "sh" : "bat"

    UAT_PATH = "\"${EngineRootFolder}/Engine/Build/BatchFiles/RunUAT.${ScriptInvocationType}\""
    UE4_CMD_PATH = "\"${EngineRootFolder}/Engine/Binaries/Win64/UE4Editor-Cmd.exe\""
    UBT_PATH = "\"${EngineRootFolder}/Engine/Binaries/DotNET/UnrealBuildTool.exe\""

    BuildGraphPath = new File( ProjectRootFolder, ue4_config.Project.BuildGraphPath ).toString()
}

// script_path is the location of the XML file relative to the project root folder used in the initialize function
def runBuildGraph( String target, def parameters = [:] ) {
    String parsed_parameters = ""

    parameters.each
    {
        parameter -> parsed_parameters += "-set:${parameter.key}=\"${parameter.value}\" "
    }

    RunCommand( "${UAT_PATH} BuildGraph ${DefaultArguments} -target=\"${target}\" -script=\"${BuildGraphPath}\" -set:ProjectPath=\"${ProjectPath}\" ${parsed_parameters}" )
}

def generateProjectFiles() {
    RunCommand( "${UBT_PATH} -projectfiles -project=${ProjectPath} -game -rocket -vs2019 -progress" )
}

def RunCommand( String command ) {
    if(isUnix()) {
        sh( script: command )
    } else {
        bat( script: command )
    }

    // echo command
}

return this