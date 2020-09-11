package com.example
import org.yaml.snakeyaml.*

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
        Yaml yaml = new Yaml();
        def obj = yaml.load(configurationFile);
        //obj.each {println(it.notifications)}
        String currentDir = new File(".").getAbsolutePath()
        println(currentDir)

//    ===================== Parse configuration file ==================

//    ===================== Run pipeline stages =======================

//    ===================== End pipeline ==============================
    }
}
