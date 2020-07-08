You can use groovy parsers and the recordIssues task to collect informations about the data validation / packaging.

Here are some useful parsers:

## UE4 - NamingConventionValidation

To be run on the log of the naming convention validation tool : https://github.com/TheEmidee/UE4NamingConventionValidation/

Regular Expression:

`^(.*)NamingConventionValidation: (Warning|Error): ([^\s]+) (.*)$`

Mapping Script:

```
import edu.hm.hafner.analysis.Severity

String type = matcher.group( 2 )
String file = matcher.group( 3 )
String message = matcher.group( 4 )

Severity priority = Severity.WARNING_NORMAL
if (type.equalsIgnoreCase( "error") ) {
    priority = Severity.ERROR
}

return builder.setCategory("Naming Convention")
    .setFileName( file )
    .setMessage( message )
    .setSeverity( priority )
    .buildOptional()
```

Example Log Message:

    [2020.07.08-09.03.06:120][  0]NamingConventionValidation: Warning: /Game/ShooterGame/Effects/ParticleSystems/Weapons/RocketLauncher/Muzzle/P_Launcher_proj has no known naming convention.  Class = ParticleSystem

## UE4 - AssetCheck

To be run on the log of the data validation commandlet

Regular Expression:

`^(.*)AssetCheck: ([^\s]+) (contains invalid data).$`

Mapping Script:

```
import edu.hm.hafner.analysis.Severity

String file = matcher.group( 2 )
String message = matcher.group( 3 )

return builder.setCategory("Asset Check")
    .setFileName( file )
    .setMessage( message )
    .setSeverity( Severity.ERROR)
    .buildOptional()
```

Example Log Message:

    [2020.07.08-09.36.50:574][  0]AssetCheck: /Game/SWARMS/Weapons/RocketLauncher/BP_Projectile_RocketLauncher contains invalid data.

## UE4 - Cook

To be run on the log of the data validation commandlet

Regular Expression:

`^\s*.*0]LogInit: Display: (Log[^Init|^ModuleManager|^Temp].*): (Warning|Error): (.*)$`

Mapping Script:

```
import edu.hm.hafner.analysis.Severity

String category = matcher.group( 1 )
String type = matcher.group( 2 )
String message = matcher.group( 3 )

Severity priority = Severity.WARNING_NORMAL
if (type.equalsIgnoreCase( "error") ) {
    priority = Severity.ERROR
}

return builder.setCategory("Naming Convention")
    .setCategory( category )
    .setMessage( message )
    .setSeverity( priority )
    .buildOptional()
```

Example Log Message:

    [2020.07.08-16.16.57:509][  0]LogInit: Display: LogBlueprint: Error: [Compiler BP_Weapon_RocketLauncher_TargetActor] This blueprint (self) is not a SceneComponent, therefore ' Target ' must have a connection. from Source: /Game/SWARMS/Weapons/RocketLauncher/BP_Weapon_RocketLauncher_TargetActor.BP_Weapon_RocketLauncher_TargetActor