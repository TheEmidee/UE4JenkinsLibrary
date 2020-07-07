#!/usr/bin/groovy

package unreal;

def UE4_CMD = ''
def ScriptInvocationType = ''
def BatchDir = ''
def ProjectRootFolder = ''
def ProjectName = ''
def EngineRootFolder = ''

def initialize( String project_name, String project_root_folder, String engine_root_folder )
{
    ProjectName = project_name
    ProjectRootFolder = project_root_folder
    EngineRootFolder = engine_root_folder

    ProjectPath = "${project_root_folder}/${project_name}.uproject"

    BatchDir = isUnix() 
                    ? "${engine_root_folder}/Engine/Build/BatchFiles/Linux" 
                    : "${engine_root_folder}/Engine/Build/BatchFiles"
    ScriptInvocationType = isUnix() ?  "sh" : "bat"

    UAT = "\"${engine_root_folder}/Engine/Build/BatchFiles/RunUAT.${ScriptInvocationType}\""
    UE4_CMD = "\"${engine_root_folder}/Engine/Binaries/Win64/UE4Editor-Cmd.exe\""
}

// script_path is the location of the XML file relative to the project root folder used in the initialize function
def runBuildGraph( String script_path, String target, BuildConfiguration build_configuration, def parameters = [:], String additional_arguments = "" ) {
    String parsed_parameters = ""

    parameters.each
    {
        parameter -> parsed_parameters += "-set:${parameter.key}=\"${parameter.value}\" "
    }

    full_script_path = "${ProjectRootFolder}/${script_path}"

    RunCommand( "${UAT} BuildGraph -target=\"${target}\" -script=\"${full_script_path}\" -set:ProjectPath=\"${ProjectPath}\" -set:UEPath=\"${EngineRootFolder}\" ${parsed_parameters} ${additional_arguments}" )
}

def buildDDC() {
    RunCommand( "${UE4_CMD} ${ProjectFile} -run=DerivedDataCache -fill ${DefaultArguments}" )
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