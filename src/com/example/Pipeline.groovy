package com.example
import org.yaml.snakeyaml.Yaml

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
//        def rConf = new FileReader(configurationFile)
//        def config = new Yaml().load(rConf)
//
//        def email = config.notifications.email.recipients
//        def emailOnStart = config.notifications.email.on_start
//        def emailOnFailure = config.notifications.email.on_failure
//        def emailOnSuccesss = config.notifications.email.on_success
//
//        def buildProjectFolder = config.build.projectFolder
//        def buildCommand = config.build.buildCommand
//
//        def databaseFolder = config.database.databaseFolder
//        def databaseCommand = config.database.databaseCommand
//
//        def deploy = config.deploy.deployCommand
//
//        def testsFolder = config.test.testFolder
//
//        def performanceTestCommand = config.test.name['performance'].testCommand
//        def regressionTestCommand = config.test.name['regression'].testCommand
//        def integrationTestCommand = config.test.name['integration'].testCommand

        def failedStepName = 'null'

//    ===================== Run pipeline stages =======================
        script.node('master'){
            script.stage('git clone'){
                script.git "git@github.com:glebsamsonov-nbcuni/test-maven-project.git"
                script.sh(script: "pwd")
            }
            def status = true
            script.stage('build'){
                dir(buildProjectFolder)
                def buildStatus = sh(script: buildCommand, returnStatus: true, returnStdout: true)
                if (buildStatus != 0){
                    sh("exit 1")
                    status = false
                    failedStepName = 'build'
                }
            }
            script.stage('database'){
                if (status){
                    dir(databaseFolder)
                    def databaseStatus = sh(script: databaseCommand, returnStatus: true, returnStdout: true)
                    if (databaseStatus != 0){
                        sh("exit 1")
                        status = false
                        failedStepName = 'database'
                    }
                }
            }
            script.stage('deploy'){
                if (status){
                    def deployStatus = sh(script: deploy, returnStatus: true, returnStdout: true)
                    if (deployStatus != 0){
                        sh("exit 1")
                        status = false
                        failedStepName = 'deploy'
                    }
                }
            }
            script.stage('tests'){
                if (status){
                    dir(testsFolder)
                    script.parallel{
                        script.stage('performanceTest'){
                            script.steps{
                                def performanceTestStatus = sh(script: performanceTestCommand, returnStatus: true, returnStdout: true)
                                if (performanceTestStatus != 0){
                                    sh("exit 1")
                                    failedStepName = 'performanceTest'
                                }
                            }
                        }
                        script.stage('regressionTest'){
                            script.steps{
                                def regressionTestStatus = sh(script: regressionTestCommand, returnStatus: true, returnStdout: true)
                                if (regressionTestStatus != 0){
                                    sh("exit 1")
                                    failedStepName = 'regressionTest'
                                }
                            }
                        }
                        script.stage('integrationTest'){
                            script.steps{
                                def integrationTestStatus = sh(script: integrationTestCommand, returnStatus: true, returnStdout: true)
                                if (integrationTestStatus != 0){
                                    sh("exit 1")
                                    failedStepName = 'integrationTest'
                                }
                            }
                        }
                    }
                }
            }
            script.stage('notifications'){
                script.emailext body: failedStepName,
                                subject: 'Failed of Pipeline'
                                to: email
            }
        }
//    ===================== End pipeline ==============================
    }
}
