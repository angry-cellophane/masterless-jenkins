package org.ka.jenkins.masterless.image.junkins;

import hudson.init.InitMilestone;
import jenkins.model.Jenkins;

public class Junkins {

    private final Jenkins jenkins;
    private final Runnable onStop;

    Junkins(Jenkins jenkins, Runnable onStop) {
        this.jenkins = jenkins;
        this.onStop = onStop;
    }

    public boolean isRunning() {
        return jenkins.getInitLevel() == InitMilestone.COMPLETED;
    }

    public void stop() {
        onStop.run();
    }

    public static Builder builder() {
        return new Builder();
    }
}
