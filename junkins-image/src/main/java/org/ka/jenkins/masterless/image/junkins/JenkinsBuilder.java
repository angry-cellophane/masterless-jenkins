package org.ka.jenkins.masterless.image.junkins;

import hudson.LocalPluginManager;
import hudson.WebAppMain;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.model.UpdateSite;
import hudson.util.PersistedList;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.ka.jenkins.masterless.image.junkins.jetty.Jetty;

import javax.servlet.ServletContextEvent;
import java.io.File;

public class JenkinsBuilder {

    public static Jenkins build(File rootDir, Jetty.JettyContext jetty) {
        try {
            var servlet = jetty.getServletContext();
            var jenkins = new Hudson(rootDir, servlet, new LocalPluginManager(servlet, rootDir));
            jenkins.disableSecurity();

            JenkinsLocationConfiguration.get().setUrl("http://localhost:" + jetty.getPort() + "/jenkins/");

            jenkins.setNoUsageStatistics(true);
            jenkins.servletContext.setAttribute("app", jenkins);
            jenkins.servletContext.setAttribute("version", "?");
            WebAppMain.installExpressionFactory(new ServletContextEvent(jenkins.servletContext));

            // use the current jdk as the default one
            jenkins.getJDKs().add(new JDK("default", System.getProperty("java.home")));
            jenkins.setCrumbIssuer(new DummyCrumbIssuer());

            PersistedList<UpdateSite> sites = jenkins.getUpdateCenter().getSites();
            sites.clear();

            return jenkins;
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
    }
}
