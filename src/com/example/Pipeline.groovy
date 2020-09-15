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
        //def config = new Yaml().load(new FileReader("/var/jenkins_home/workspace/test/config.yml").text)
        def workspace
        script.node('master'){
            workspace = script.WORKSPACE
        }
        def config = new Yaml().load(new FileReader(workspace + "/" + configurationFile).text)

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

        def failedStepName

        //def projectDir = "/var/jenkins_home/workspace/test/"
        def projectDir = workspace + "/"

//    ===================== Run pipeline stages =======================
        try {
            script.node('master') {

                script.stage('build') {
                    script.dir(projectDir + buildProjectFolder) {
                        def buildStatus = script.sh(script: buildCommand, returnStatus: true)
                        if (buildStatus != 0) {
                            failedStepName = script.env.STAGE_NAME
                            script.currentBuild.result = 'FAILURE'
                            script.error('stop')
                        }
                    }
                }
                script.stage('database') {
                    script.dir(projectDir + databaseFolder) {
                        def databaseStatus = script.sh(script: databaseCommand, returnStatus: true)
                        if (databaseStatus != 0) {
                            failedStepName = script.env.STAGE_NAME
                            script.currentBuild.result = 'FAILURE'
                            script.error('stop')
                        }
                    }
                }
                script.stage('deploy') {
                    script.dir(projectDir + buildProjectFolder) {
                        def deployStatus = script.sh(script: deploy, returnStatus: true)
                        if (deployStatus != 0) {
                            failedStepName = script.env.STAGE_NAME
                            script.currentBuild.result = 'FAILURE'
                            script.error('stop')
                        }
                    }
                }
                script.stage('tests') {
                    script.dir(projectDir + testsFolder) {
                        script.parallel runPerformanceTest: {
                            script.stage('performanceTest') {
                                if ( script.sh(script: performanceTestCommand)) {
                                }
                            }
                        }, runRegressionTest: {
                            script.stage('regressionTest') {
                                if (script.sh(script: regressionTestCommand)){
                                }
                            }
                        }, runIntegrationTest: {
                            script.stage('integrationTest') {
                                if ( script.sh(script: integrationTestCommand) ) {
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (e){
            script.node('master') {
                script.stage('notifications') {
                    script.emailext body: script.env.STAGE_NAME,
                            subject: 'Failed of Pipeline',
                            to: email
                    script.currentBuild.result = 'FAILURE'
                }
            }
        }
//        script.node('master'){
//            script.stage('cleanup WorkSpace'){
//                script.dir(workspace){
//                    script.deleteDir()
//                }
//            }
//        }
//    ===================== End pipeline ==============================
    }
}