package org.ka.jenkins.masterless.image.junkins;

import hudson.init.InitMilestone;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDurabilityHint;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.job.properties.DurabilityHintJobProperty;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Junkins {

    private static final Logger LOG = Logger.getLogger(Junkins.class.getName());

    private static final long TIMEOUT = TimeUnit.HOURS.toMillis(4);
    private static final int IMMEDIATELY = 0;

    private final Jenkins jenkins;
    private final Runnable onStop;

    Junkins(Jenkins jenkins, Runnable onStop) {
        this.jenkins = jenkins;
        this.onStop = onStop;
    }

    public WorkflowRun runOneOffPipeline(String script) {
        try {
            try (var acl = ACL.as(ACL.SYSTEM)) {
                var id = UUID.randomUUID();
                var project = jenkins.createProject(WorkflowJob.class, id.toString());
                project.setDefinition(new CpsFlowDefinition(script, true));
                project.addProperty(new DurabilityHintJobProperty(FlowDurabilityHint.PERFORMANCE_OPTIMIZED));

                var task = project.scheduleBuild2(IMMEDIATELY);
                var run = task.waitForStart();

                long startTime = System.currentTimeMillis();
                while (run.isBuilding()) {
                    if (System.currentTimeMillis() - startTime > TIMEOUT) {
                        LOG.log(Level.SEVERE, "cannot finish build " + id + " within " + TimeUnit.MILLISECONDS.toHours(TIMEOUT) + " hours");
                    }
                    Thread.sleep(100);
                }

                return run;
            }
        } catch (Exception e) {
            throw new JunkinsException(e);
        }
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
