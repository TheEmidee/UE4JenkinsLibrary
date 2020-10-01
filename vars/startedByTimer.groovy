#!/usr/bin/groovy

@NonCPS
def call() {
    boolean startedByTimer = false

    def buildCauses = currentBuild.rawBuild.getCauses()

    for (buildCause in buildCauses) {
        if ("${buildCause}".contains("hudson.triggers.TimerTrigger\$TimerTriggerCause")) {
            startedByTimer = true
        }
    }

    return startedByTimer
}