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
        //def document = new FileReader("config.yml");
        def obj = yaml.load(configurationFile);
        System.out.println(obj);
        echo "Hello", ${configurationFile};

//    ===================== Parse configuration file ==================

//    ===================== Run pipeline stages =======================

//    ===================== End pipeline ==============================
    }
}
