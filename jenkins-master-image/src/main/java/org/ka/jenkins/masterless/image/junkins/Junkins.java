package org.ka.jenkins.masterless.image.junkins;

import hudson.init.InitMilestone;
import jenkins.model.Jenkins;

public class Junkins {

    private final Jenkins jenkins;

    Junkins(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public boolean isRunning() {
        return jenkins.getInitLevel() == InitMilestone.COMPLETED;
    }

    public void stop() {
        try {
            jenkins.doQuietDown(true, 0);
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
