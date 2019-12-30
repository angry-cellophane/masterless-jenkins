package org.ka.jenkins.masterless.image;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ka.jenkins.masterless.image.junkins.Junkins;
import org.ka.jenkins.masterless.image.junkins.JunkinsException;

import java.io.File;
import java.io.IOException;

public class App {

    static class EnvVars {
        static final String ROOT_DIR = "JUNKINS_ROOT_DIR";
        static final String WAR_EXPLODED = "JUNKINS_WAR_EXPLODED";

    }

    static final String PIPELINE = "node('master') { sh 'echo hello' }";

    public static void main(String[] args) throws IOException {
        var rootDir = fileFromEnvVar(EnvVars.ROOT_DIR);
        var warExploded = fileFromEnvVar(EnvVars.WAR_EXPLODED);

        var optData = new File("/opt/jenkins_data");
        if (optData.exists()) {
            FileUtils.copyDirectoryToDirectory(optData, rootDir);
        }

        var junkins = Junkins.builder()
                .rootDir(rootDir)
                .warExplodedDir(warExploded)
                .build();
        junkins.runOneOffPipeline(PIPELINE);
        junkins.stop();
        System.exit(0);
    }

    static File fileFromEnvVar(String name) {
        String value = System.getenv(name);
        if (StringUtils.isBlank(value)) {
            throw new JunkinsException("required env var " + name + " not defined");
        }

        var file = new File(value);
        if (!file.exists()) {
            throw new JunkinsException("file " + value + " from env var " + name + " does not exist");
        }

        return file;
    }
}
