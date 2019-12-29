package org.ka.jenkins.masterless.image;

import org.apache.commons.lang.StringUtils;
import org.ka.jenkins.masterless.image.junkins.Junkins;
import org.ka.jenkins.masterless.image.junkins.JunkinsException;

import java.io.File;

public class App {

    static class EnvVars {
        static final String ROOT_DIR = "JUNKINS_ROOT_DIR";
        static final String WAR_EXPLODED = "JUNKINS_WAR_EXPLODED";

    }

    static final String PIPELINE = "node('master') { sh 'echo hello' }";

    public static void main(String[] args) {
        var rootDir = envVar(EnvVars.ROOT_DIR);
        var warExploded = envVar(EnvVars.WAR_EXPLODED);

        var junkins = Junkins.builder()
                .rootDir(new File(rootDir))
                .warExplodedDir(new File(warExploded))
                .build();
        junkins.runOneOffPipeline(PIPELINE);
        junkins.stop();
        System.exit(0);
    }

    static String envVar(String name) {
        String value = System.getenv(name);
        if (StringUtils.isBlank(value)) {
            throw new JunkinsException("required env var " + name + " not defined");
        }

        return value;
    }
}
