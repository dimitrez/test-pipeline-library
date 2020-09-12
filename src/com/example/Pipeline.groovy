package com.example

//import org.yaml.snakeyaml.Yaml

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
//        Yaml yaml = new Yaml()
//        def file = yaml.load(configurationFile)
        //def item = Jenkins.instance.getItemByFullName("test")
        script.stage('git clone'){
            //def gitUrl = item.getScm().getUserRemoteConfigs()[0].getUrl()
            ["git", "clone", "git@github.com:glebsamsonov-nbcuni/test-maven-project.git"].execute()
        }

        //script.stage('clone git'){
        //    ["git", "clone", ""]
        //}
//    ===================== Parse configuration file ==================

//    ===================== Run pipeline stages =======================

//    ===================== End pipeline ==============================
    }
}
