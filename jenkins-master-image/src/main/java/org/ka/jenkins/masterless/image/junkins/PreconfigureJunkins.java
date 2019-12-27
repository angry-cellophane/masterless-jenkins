package org.ka.jenkins.masterless.image.junkins;

import hudson.ClassicPluginStrategy;
import hudson.DNSMultiCast;
import hudson.Functions;

public class PreconfigureJunkins {

    public static void run() {
        Functions.DEBUG_YUI = false;
        DNSMultiCast.disabled = true;
        ClassicPluginStrategy.useAntClassLoader = false;
    }

    private PreconfigureJunkins() {}
}
