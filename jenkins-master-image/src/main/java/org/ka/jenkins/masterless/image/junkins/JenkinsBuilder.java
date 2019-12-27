package org.ka.jenkins.masterless.image.junkins;

import hudson.LocalPluginManager;
import hudson.WebAppMain;
import hudson.model.Hudson;
import hudson.model.JDK;
import jenkins.model.Jenkins;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;

public class JenkinsBuilder {

    public static Jenkins build(File rootDir, ServletContext servlet) {
        try {
            var jenkins = new Hudson(rootDir, servlet, new LocalPluginManager(servlet, rootDir));

            jenkins.setNoUsageStatistics(true);
            jenkins.servletContext.setAttribute("app", jenkins);
            jenkins.servletContext.setAttribute("version", "?");
            WebAppMain.installExpressionFactory(new ServletContextEvent(jenkins.servletContext));

            // use the current jdk as the default one
            jenkins.getJDKs().add(new JDK("default", System.getProperty("java.home")));
            jenkins.setCrumbIssuer(new DummyCrumbIssuer());
            return jenkins;
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }
}
