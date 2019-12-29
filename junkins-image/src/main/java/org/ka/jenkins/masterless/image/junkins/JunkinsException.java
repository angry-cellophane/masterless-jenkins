package org.ka.jenkins.masterless.image.junkins;

public class JunkinsException extends RuntimeException {
    public JunkinsException(String message) {
        super(message);
    }

    public JunkinsException(Throwable cause) {
        super(cause);
    }
}
