package org.ka.jenkins.masterless.image.junkins;

import jenkins.model.Jenkins;

public class Junkins {

    private final Jenkins jenkins;

    Junkins(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public static Builder builder() {
        return new Builder();
    }
}
