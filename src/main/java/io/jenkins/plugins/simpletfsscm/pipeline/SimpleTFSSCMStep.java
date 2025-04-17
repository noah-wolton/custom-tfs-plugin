package io.jenkins.plugins.simpletfsscm.pipeline;

import hudson.Extension;
import hudson.scm.SCM;
import io.jenkins.plugins.simpletfsscm.scm.SimpleTFSSCM;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;
import org.kohsuke.stapler.DataBoundConstructor;

public class SimpleTFSSCMStep extends SCMStep {

    private final String server;
    private final String username;
    private final String password;
    private final String workspaceName;
    private final boolean cleanCopy;

    @DataBoundConstructor
    public SimpleTFSSCMStep(String server, String username, String password, String workspaceName, boolean cleanCopy) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.workspaceName = workspaceName;
        this.cleanCopy = cleanCopy;
    }

    @Override
    protected SCM createSCM() {
        return new SimpleTFSSCM(server, cleanCopy, username, password, workspaceName);
    }

    @Extension
    public static class DescriptorImpl extends SCMStepDescriptor {
        @Override
        public String getFunctionName() {
            return "simpleTfs"; // this is the pipeline function name
        }

        @Override
        public String getDisplayName() {
            return "Simple TFS SCM";
        }
    }
}
