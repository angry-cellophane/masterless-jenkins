package org.ka.jenkins.masterless.image;

import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDurabilityHint;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.job.properties.DurabilityHintJobProperty;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.JenkinsRule;

public class App {
    static final String PIPELINE = "node('master') { sh 'echo hello' }";

    public static void main(String[] args) throws Throwable {
        var rule = new JenkinsRule();
//        rule.setPluginManager(new LocalPluginManager(new File("./build/plugins")));

        var statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                var project = rule.createProject(WorkflowJob.class, "name");
                project.setDefinition(new CpsFlowDefinition(PIPELINE, true));
                project.addProperty(new DurabilityHintJobProperty(FlowDurabilityHint.PERFORMANCE_OPTIMIZED));

                QueueTaskFuture<WorkflowRun> task = project.scheduleBuild2(0);
                WorkflowRun run = task.waitForStart();
                rule.waitForCompletion(run);
                System.out.println("run = " + run.getDisplayName() + " status = " + run.getBuildStatusSummary().message);
                System.out.println("logs: " + run.getLog());
            }
        };
        var description = Description.createTestDescription(App.class, "jenkins");

        rule.apply(statement, description).evaluate();
        System.out.println("done");
        System.exit(0);
    }
}
