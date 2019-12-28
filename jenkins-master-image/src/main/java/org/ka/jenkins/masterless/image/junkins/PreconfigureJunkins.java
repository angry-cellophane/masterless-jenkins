package org.ka.jenkins.masterless.image.junkins;

import hudson.ClassicPluginStrategy;
import hudson.DNSMultiCast;
import hudson.Functions;
import hudson.model.DownloadService;
import hudson.model.UpdateSite;
import hudson.model.User;

import java.io.File;

public class PreconfigureJunkins {

    public static void run() {
        try {
            if (Functions.isWindows()) {
                // JENKINS-4409.
                // URLConnection caches handles to jar files by default,
                // and it prevents delete temporary directories on Windows.
                // Disables caching here.
                // Though defaultUseCache is a static field,
                // its setter and getter are provided as instance methods.
                var connection = new File(".").toURI().toURL().openConnection();
                connection.setDefaultUseCaches(false);
            }
            Functions.DEBUG_YUI = false;
            DNSMultiCast.disabled = true;
            ClassicPluginStrategy.useAntClassLoader = false;

            DownloadService.neverUpdate = true;
            UpdateSite.neverUpdate = true;
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }

    private PreconfigureJunkins() {}
}
