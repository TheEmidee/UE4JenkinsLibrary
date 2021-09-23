#!/usr/bin/groovy

def call( ue4_config ) {

    echo "Registering log parsers"

    def config = io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration.getInstance()

    ue4_config.LogsParserDefinitions.each { item -> 
        def parser_definition = item.LogsParserDefinition

        if( !config.contains( parser_definition.Id ) ) {
            def newParser = new io.jenkins.plugins.analysis.warnings.groovy.GroovyParser(
                parser_definition.Id, 
                parser_definition.Name, 
                parser_definition.RegExp, 
                parser_definition.Script, 
                parser_definition.Sample
            )

            config.setParsers( config.getParsers().plus( newParser ) )

            echo "Registered Log Parser '${parser_definition.Id}'"
        } else {
            echo "Log Parser '${parser_definition.Id}' already registered"
        }
    }
}