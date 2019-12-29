package org.ka.jenkins.masterless.image.junkins

import hudson.model.Result
import jenkins.model.Jenkins
import spock.lang.Shared
import spock.lang.Specification

class JunkinsTest extends Specification implements JenkinsHome {

    @Shared Junkins junkins
    @Shared File root

    void setupSpec() {
        root = newDefaultJenkinsHome()

        junkins = Junkins.builder()
                .rootDir(root)
                .warExplodedDir(new File("./build/war"))
                .build()
    }

    void cleanupSpec() {
        junkins.stop()
        root.deleteDir()
    }

    void 'junkins is running'() {
        expect:
        junkins.running
    }

    void 'plugins are loaded'() {
        given:
        def jenkins = Jenkins.get()

        expect:
        jenkins.getPlugin('durable-task') != null
        jenkins.getPlugin('workflow-cps') != null
        jenkins.getPlugin('workflow-job') != null
        jenkins.getPlugin('workflow-support') != null
        jenkins.getPlugin('scm-api') != null
        jenkins.getPlugin('configuration-as-code') != null
        jenkins.getPlugin('script-security') != null
    }

    void 'run a pipeline build'() {
        given:
        String script = "node('master') { sh 'echo hello' }";

        when:
        def run = junkins.runOneOffPipeline(script)
        println run.getLogText().readAll().text

        then:
        run.getResult() == Result.SUCCESS
    }
}
