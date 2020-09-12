package com.example
import groovy.yaml.*

class Pipeline {
    def script
    def configurationFile

    Pipeline(script, configurationFile) {
        this.script = script
        this.configurationFile = configurationFile
    }

    def execute() {
//    ===================== Your Code Starts Here =====================
//    Note : use "script" to access objects from jenkins pipeline run (WorkflowScript passed from Jenkinsfile)
//           for example: script.node(), script.stage() etc

//    ===================== Parse configuration file ==================
        def config = new YamlSlurper().parseText(configurationFile)

        def email = config.notifications.email.recipients
        def emailOnStart = config.notifications.email.on_start
        def emailOnFailure = config.notifications.email.on_failure
        def emailOnSuccesss = config.notifications.email.on_success

        def buildProjectFolder = config.build.projectFolder
        def buildCommand = config.build.buildCommand

        def databaseFolder = config.database.databaseFolder
        def databaseCommand = config.database.databaseCommand

        def deploy = config.deploy.deployCommand

        def testsFolder = config.test.testFolder

        def performanceTestCommand = config.test.name['performance'].testCommand
        def regressionTestCommand = config.test.name['regression'].testCommand
        def integrationTestCommand = config.test.name['integration'].testCommand

//    ===================== Run pipeline stages =======================
        script.node('master'){
            script.stage('build'){
                dir(buildProjectFolder)
                def buildStatus = sh(script: buildCommand, returnStatus: true, returnStdout: true)
                if (buildStatus != 0){
                    System.exit(1)
                }
            }
        }
//    ===================== End pipeline ==============================
    }
}
