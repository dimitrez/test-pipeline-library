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
        script.node('master'){
            script.stage('git clone'){
                script.git "git@github.com:glebsamsonov-nbcuni/test-maven-project.git"
            }
        }

//    ===================== Parse configuration file ==================
        def config = new Yaml().load(new FileReader("/var/jenkins_home/workspace/test/config.yml").text)

        def email = config['notifications']['email']['recipients']
//        def emailOnStart = config['notifications']['email']['on_start']
//        def emailOnFailure = config['notifications']['email']['on_failure']
//        def emailOnSuccesss = config['notifications']['email']['on_success']

        def buildProjectFolder = config['build']['projectFolder']
        String buildCommand = config['build']['buildCommand'].toString()

        def databaseFolder = config['database']['databaseFolder']
        String databaseCommand = config['database']['databaseCommand'].toString()

        String deploy = config['deploy']['deployCommand'].toString()

        def testData = config['test']
        def testsFolder = testData['testFolder'].getAt(0)
        def performanceTestCommand = testData.getAt(0)['testCommand'].toString()
        def regressionTestCommand = testData.getAt(1)['testCommand'].toString()
        def integrationTestCommand = testData.getAt(2)['testCommand'].toString()

        def failedStepName = 'null'
        def projectDir = "/var/jenkins_home/workspace/test/"

//    ===================== Run pipeline stages =======================
        script.node('master'){

            def status = true
            script.stage('build'){
              script.dir(projectDir + buildProjectFolder){
                  def buildStatus = script.sh(script: buildCommand, returnStatus: true)
                  //script.sh(script: "echo " + buildStatus)
                  if (buildStatus != 0){
                      script.sh("exit 1")
                      status = false
                      failedStepName = 'build'
                  }
              }
            }
            script.stage('database'){
                if (status){
                    script.dir(projectDir + databaseFolder){
                        def databaseStatus = script.sh(script: databaseCommand, returnStatus: true)
                        if (databaseStatus != 0){
                            script.sh("exit 1")
                            status = false
                            failedStepName = 'database'
                        }
                    }
                }
            }
            script.stage('deploy'){
                if (status){
                    script.dir(projectDir + buildProjectFolder){
                        def deployStatus = script.sh(script: deploy, returnStatus: true)
                        if (deployStatus != 0){
                            script.sh("exit 1")
                            status = false
                            failedStepName = 'deploy'
                        }
                    }
                }
            }
            script.stage('tests') {
                if (status) {
                    script.dir(projectDir + testsFolder) {
//                        script.parallel {
                            script.stage('performanceTest') {
                                script.steps {
                                    def performanceTestStatus = script.sh(script: performanceTestCommand, returnStatus: true)
                                    if (performanceTestStatus != 0) {
                                        script.sh("exit 1")
                                        failedStepName = 'performanceTest'
                                    }
                                }
                            }
                            script.stage('regressionTest') {
                                script.steps {
                                    def regressionTestStatus = script.sh(script: regressionTestCommand, returnStatus: true)
                                    if (regressionTestStatus != 0) {
                                        script.sh("exit 1")
                                        failedStepName = 'regressionTest'
                                    }
                                }
                            }
                            script.stage('integrationTest') {
                                script.steps {
                                    def integrationTestStatus = script.sh(script: integrationTestCommand, returnStatus: true)
                                    if (integrationTestStatus != 0) {
                                        script.sh("exit 1")
                                        failedStepName = 'integrationTest'
                                    }
                                }
                            }
//                        }
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
