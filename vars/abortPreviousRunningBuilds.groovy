#!/usr/bin/env groovy

// Note you will have to add some exceptions in the Jenkins security options to allow this function to run
def call() {
  def hi = Jenkins.instance
  def pname = env.JOB_NAME.split('/')[0]

  hi.getItem( pname ).getItem(env.JOB_BASE_NAME).getBuilds().each{ build ->
    def exec = build.getExecutor()

    if ( build.number < currentBuild.number && exec != null ) {
      exec.interrupt(
        Result.ABORTED,
        new CauseOfInterruption.UserInterruption(
          "Aborted by #${currentBuild.number}"
        )
      )
      println("Aborted previous running build #${build.number}")
    }
  }
}