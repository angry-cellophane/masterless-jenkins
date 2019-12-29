package org.ka.jenkins.masterless.image.junkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.model.UpdateCenter;
import jenkins.model.Jenkins;
import org.ka.jenkins.masterless.image.junkins.jetty.Jetty;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Builder {

    private File warExploded;
    private File rootDir;

    public Builder rootDir(File dir) {
        this.rootDir = dir;
        return this;
    }

    public Builder warExplodedDir(File dir) {
        this.warExploded = dir;
        return this;
    }

    Builder() {}

    public Junkins build() {
        try {
            setDefaultValues();
            verifyValues();

            PreconfigureJunkins.run();
            var jetty = Jetty.startNewServer(this.warExploded);

            var enableJenkinsLogs = suppressJenkinsLogs();
            var jenkins = JenkinsBuilder.build(rootDir, jetty);
            enableJenkinsLogs.run();

            return new Junkins(jenkins, onStop(jenkins, jetty::doStop));
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }

    private static Runnable suppressJenkinsLogs() {
        var updateCenterLogger = Logger.getLogger(UpdateCenter.class.getName());
        var updateCenterLogLevel = updateCenterLogger.getLevel();
        updateCenterLogger.setLevel(Level.SEVERE);

        return () -> {
            updateCenterLogger.setLevel(updateCenterLogLevel);
        };
    }

    private Runnable onStop(Jenkins jenkins, Runnable stopJetty) {
        return () -> {
            stopJetty.run();
            jenkins.cleanUp();
            ExtensionList.clearLegacyInstances();
            DescriptorExtensionList.clearLegacyInstances();
        };
    }

    private void setDefaultValues() {
        try {
            if (this.rootDir == null) {
                this.rootDir = Files.createTempDirectory("jenkins").toFile();
            }
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }

    private void verifyValues() {
        if (!this.rootDir.exists()) {
            throw new JunkinsException("root directory " + this.rootDir.getAbsolutePath() + " doesn't exist. Create directory first");
        }

        if (!this.warExploded.exists()) {
            throw new JunkinsException("directory with exploded war " + this.warExploded.getAbsolutePath() + " doesn't exist. Unpack jenkins.war in a directory first");
        }
    }
}
