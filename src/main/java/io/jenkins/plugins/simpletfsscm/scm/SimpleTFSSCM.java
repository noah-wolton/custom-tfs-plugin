package io.jenkins.plugins.simpletfsscm.scm;

import hudson.Extension;
import hudson.FilePath;
import java.io.File;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.*;

import java.io.IOException;

import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class SimpleTFSSCM extends SCM {

    private String server, username, password, workspaceName;
    boolean cleanCopy;

    @DataBoundConstructor
    public SimpleTFSSCM(String server, boolean cleanCopy, String username, String password, String workspaceName) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.workspaceName = workspaceName;
        this.cleanCopy = cleanCopy;
    }

    /*
    public SimpleTFSSCM(String server, String projectName, String username, String password, String domain, String workspaceName) {
        this.server = server;
        this.projectName = projectName;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.workspaceName = workspaceName;
    }*/

    @Override
    public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException
    {
        System.out.println("Checking out");



    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        System.out.println("Creating TFSChangeLogParser");
        return new SimpleTFSChangeLogParser();
    }

    public String getServer() {
        return server;
    }

    public boolean isCleanCopy() {
        return cleanCopy;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /*
    public String getDomain() {
        return domain;
    }*/

    public String getWorkspaceName() {
        return workspaceName;
    }

    @Override
    public boolean supportsPolling() {
        return false; // hopefully you do
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false; // hopefully you don't
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Extension
    public static class DescriptorImpl extends SCMDescriptor {
        private String tfExecutable;

        public DescriptorImpl() {
            super(SimpleTFSSCM.class, null);
            load();
        }

        @Override
        public SCM newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            //SimpleTFSSCM scm = (SimpleTFSSCM) super.newInstance(req, formData);
            //return scm;
            return req.bindJSON(SimpleTFSSCM.class, formData);
        }

        @Override
        public String getDisplayName() {
            return "Simple TFS SCM";
        }

        public String getTfExecutable() {
            return tfExecutable;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            tfExecutable = Util.fixEmpty(req.getParameter("tfs.tfExecutable").trim());
            save();
            return true;
        }

        @Override
        public boolean isApplicable(Job project) {
            return true;
        }

        //public FormValidation doCheckServer(@QueryParameter String value)

    }
}
