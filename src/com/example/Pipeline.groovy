package com.example
import org.yaml.snakeyaml.*
import java.util.logging.Logger

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
        def command = "mvn clean test -Dscope=regression; exit 1"
        def proc = command.execute()
        proc.waitFor()

        def logger = Logger.getLogger("")
        logger.println(proc.exitValue())

//        if (proc.exitValue() != 0)
//            System.exit(1)
//        else System.exit(0)


//    ===================== Parse configuration file ==================

//    ===================== Run pipeline stages =======================

//    ===================== End pipeline ==============================
    }
}
