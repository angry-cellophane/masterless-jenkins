package org.ka.jenkins.masterless.image.junkins

import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class JunkinsTest extends Specification {

    @Shared Junkins junkins

    void setupSpec() {
        def root = Files.createTempDirectory('jenkins').toFile()
        root.deleteOnExit()

        def plugins = root.toPath().resolve('plugins').toFile()
        plugins.mkdir()

        FileUtils.copyDirectory(new File("./build/plugins"), plugins);

        junkins = Junkins.builder()
                .rootDir(root)
                .warExplodedDir(new File("./build/war"))
                .build()
    }

    void cleanupSpec() {
        junkins.stop()
    }

    void 'junkins is running'() {
        expect:
        junkins.running
    }
}
