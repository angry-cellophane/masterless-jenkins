package org.ka.jenkins.masterless.image.junkins;


import hudson.security.csrf.CrumbIssuer;

import javax.servlet.ServletRequest;

public class DummyCrumbIssuer extends CrumbIssuer {
    @Override
    protected String issueCrumb(ServletRequest request, String salt) {
        return "local";
    }

    @Override
    public boolean validateCrumb(ServletRequest request, String salt, String crumb) {
        return "local".equals(crumb);
    }
}
