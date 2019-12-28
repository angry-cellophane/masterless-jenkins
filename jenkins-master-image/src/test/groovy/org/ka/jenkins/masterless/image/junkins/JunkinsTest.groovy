package org.ka.jenkins.masterless.image.junkins

import hudson.model.Result
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class JunkinsTest extends Specification {

    @Shared Junkins junkins
    @Shared File root

    void setupSpec() {
        root = Files.createTempDirectory('jenkins').toFile()
        root.deleteOnExit()
        println "root = ${root.getAbsolutePath()}"

        def plugins = root.toPath().resolve('plugins').toFile()
        plugins.mkdir()

        root.toPath().resolve('jenkins.yaml').toFile().text = JunkinsTest.classLoader.getResource('caas/vanilla.yaml').text

        FileUtils.copyDirectory(new File("./build/plugins"), plugins);

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
