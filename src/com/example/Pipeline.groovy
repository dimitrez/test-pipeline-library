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
        def rConf = new FileReader(configurationFile)
        def config = new Yaml().load(rConf)

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
            def status = true
            script.stage('build'){
                dir(buildProjectFolder)
                def buildStatus = sh(script: buildCommand, returnStatus: true, returnStdout: true)
                if (buildStatus != 0){
                    sh("exit 1")
                    status = false
                }
            }
            script.stage('database'){
                if (status){
                    dir(databaseFolder)
                    def databaseStatus = sh(script: databaseCommand, returnStatus: true, returnStdout: true)
                    if (databaseStatus != 0){
                        sh("exit 1")
                        status = false
                    }
                }
            }
            script.stage('deploy'){
                if (status){
                    def deployStatus = sh(script: deploy, returnStatus: true, returnStdout: true)
                    if (deployStatus != 0){
                        sh("ecit 1")
                        status = false
                    }
                }
            }
            script.stage('tests'){
                if (status){
                    dir(testsFolder)
                    def testPerfomance = true
                    def testRegression = true
                    def testIntegration = true
                    script.parallel{
                        script.stage('performanceTest'){
                            script.steps{
                                def performanceTestStatus = sh(script: performanceTestCommand, returnStatus: true, returnStdout: true)
                                if (performanceTestStatus != 0){
                                    sh("exit 1")
                                    testPerfomance = false
                                }
                            }
                        }
                        script.stage('regressionTest'){
                            script.steps{
                                def regressionTestStatus = sh(script: regressionTestCommand, returnStatus: true, returnStdout: true)
                                if (regressionTestStatus != 0){
                                    sh("exit 1")
                                    testRegression = false
                                }
                            }
                        }
                        script.stage('integrationTest'){
                            script.steps{
                                def integrationTestStatus = sh(script: integrationTestCommand, returnStatus: true, returnStdout: true)
                                if (integrationTestStatus != 0){
                                    sh("exit 1")
                                    testIntegration false
                                }
                            }
                        }
                    }
                }
            }
            script.stage('notifications'){

            }
        }
//    ===================== End pipeline ==============================
    }
}
