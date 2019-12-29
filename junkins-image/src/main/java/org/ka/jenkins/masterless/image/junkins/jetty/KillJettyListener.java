package org.ka.jenkins.masterless.image.junkins.jetty;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/*
To kill the listener loaded from the web.xml to create a Jenkins instance with any specified home directory
 */
public class KillJettyListener extends AbstractLifeCycle {
    private final WebAppContext context;

    public KillJettyListener(WebAppContext context) {
        this.context = context;
    }

    @Override
    protected void doStart() throws Exception {
        context.setEventListeners(null);
    }
}
