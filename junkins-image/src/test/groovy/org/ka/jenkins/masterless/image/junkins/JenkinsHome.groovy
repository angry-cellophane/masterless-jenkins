package org.ka.jenkins.masterless.image.junkins

import org.apache.commons.io.FileUtils
import org.apache.tools.ant.BuildLogger
import org.apache.tools.ant.Project

import java.nio.file.Files

trait JenkinsHome {

    File newDefaultJenkinsHome() {
        def root = Files.createTempDirectory('jenkins').toFile()
        root.deleteOnExit()

        def plugins = root.toPath().resolve('plugins').toFile()
        plugins.mkdir()

        root.toPath().resolve('jenkins.yaml').toFile().text = JenkinsHome.classLoader.getResource('caas/jenkins.yaml').text

        FileUtils.copyDirectory(new File("./build/plugins"), plugins);
        def ant = new AntBuilder()
        ant.project.buildListeners.find { it instanceof BuildLogger }?.each { (it as BuildLogger).messageOutputLevel = Project.MSG_ERR }
        plugins.listFiles().findAll { it.name.endsWith('.hpi') || it.name.endsWith('.jpi') }
            .each { file ->
                def folderName = file.getName().replaceAll('.jpi', '').replaceAll('.hpi', '')
                def unzipped = new File(file.getParent(), folderName)
                unzipped.deleteDir()
                unzipped.mkdir()

                ant.unzip( src: file.getAbsolutePath(), dest: unzipped.getAbsolutePath(), overwrite: false)
            }

        return root
    }

}