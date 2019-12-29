package org.ka.jenkins.masterless.image.junkins

import org.apache.commons.io.FileUtils

import java.nio.file.Files

trait JenkinsHome {

    File newDefaultJenkinsHome() {
        def root = Files.createTempDirectory('jenkins').toFile()
        root.deleteOnExit()

        def plugins = root.toPath().resolve('plugins').toFile()
        plugins.mkdir()

        root.toPath().resolve('jenkins.yaml').toFile().text = JenkinsHome.classLoader.getResource('caas/vanilla.yaml').text

        FileUtils.copyDirectory(new File("./build/plugins"), plugins);
        return root
    }

}