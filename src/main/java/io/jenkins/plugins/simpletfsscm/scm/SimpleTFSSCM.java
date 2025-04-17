package io.jenkins.plugins.simpletfsscm.scm;

import hudson.Extension;
import hudson.FilePath;

import java.io.BufferedReader;
import java.io.File;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class SimpleTFSSCM extends SCM {

    private String server, projectPath;
    boolean cleanCopy;

    @DataBoundConstructor
    public SimpleTFSSCM(String server, boolean cleanCopy, String projectPath) {
        this.server = server;
        this.projectPath = projectPath;
        this.cleanCopy = cleanCopy;
    }

    private Map<String, String> runCommand(ProcessBuilder command) throws IOException, InterruptedException {
        System.out.println(String.join(" ", command.command().toArray(new String[0])));
        Process process = command.start();

        BufferedReader Errorreader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();

        StringBuilder builder2 = new StringBuilder();
        String line2 = null;
        while ( (line2 = Errorreader.readLine()) != null) {
            builder2.append(line2);
            builder2.append(System.getProperty("line.separator"));
        }
        String Errorresult = builder2.toString();

        process.waitFor();

        Map<String, String> output = new HashMap<>();
        output.put("Output", result);
        output.put("Error", Errorresult);

        return output;
    }

    @Override
    public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException
    {
        String workspaceShortened = String.valueOf(workspace);
        //workspaceShortened = workspaceShortened.substring(0, workspaceShortened.length() - 20);
        System.out.println("Checking out");

        System.out.println(workspaceShortened);

        String tfExec = getDescriptor().getTfExecutable();
        System.out.println(tfExec);

        File workspaceDir = new File(workspaceShortened);
        workspaceDir.mkdir();
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        //builder.inheritIO();
        if (isWindows) {
            builder.command(tfExec,"workspace", "/new", "/collection:"+server);
        }


        builder.directory(workspaceDir);
        Map<String,String> output = runCommand(builder);
        String errorOut = output.get("Error");
        System.out.println(output);

        if (errorOut.contains("already exists on computer")) {
            String existingWorkspace = errorOut.replaceAll(".*?workspace\\s+(.*?)\\s+already.*", "$1");
            System.out.println(existingWorkspace);

            if (isWindows) {
                builder.command(tfExec, "workspace", "/delete", "/collection:"+server, existingWorkspace);
            }
            Map<String,String> output2 = runCommand(builder);
            System.out.println(output2);

            if (isWindows) {
                builder.command(tfExec,"workspace", "/new", "/collection:"+server);
            }
            output = runCommand(builder);
            System.out.println(output);
        }

        String out = output.get("Output");
        String createdWorkspace = out.replaceAll(".*?'(.*?)'.*", "$1").stripTrailing();
        System.out.println(createdWorkspace);

        if (isWindows) {
            builder.command(tfExec,"workfold", "/map", "/collection:"+server, "/workspace:"+createdWorkspace, projectPath, workspaceShortened);
        }
        output = runCommand(builder);
        System.out.println(output);
        if (isWindows) {
            builder.command(tfExec,"get");
        }
        output = runCommand(builder);
        System.out.println(output);
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


    public String getProjectPath() {
        return projectPath;
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
