package org.jenkinsci.plugins.workflow.multibranch.extended;

import hudson.Extension;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory;
import org.jenkinsci.plugins.workflow.multibranch.extended.scm.ExtendedSCMBinder;
import org.jenkinsci.plugins.workflow.multibranch.extended.scm.LocalFileSCMSourceCriteria;
import org.jenkinsci.plugins.workflow.multibranch.extended.scm.SCMFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Collection;

/**
 * This class extends @{@link WorkflowBranchProjectFactory} to inject defined Jenkins file and repository in
 * Remote Jenkins File Plugin
 * @author Aytunc BEKEN, aytuncbeken.ab@gmail.com
 */
public class RemoteJenkinsFileWorkflowBranchProjectFactory extends WorkflowBranchProjectFactory {


    private static final String defaultJenkinsFile = "Jenkinsfile";
    private String localFile;
    private String remoteJenkinsFile;
    private SCM remoteJenkinsFileSCM;
    private boolean matchBranches;
    private String scmSourceBranchName;


    /**
     * Jenkins @{@link DataBoundSetter}
     *
     * @param remoteJenkinsFile path of the Jenkinsfile
     */
    @DataBoundSetter
    public void setRemoteJenkinsFile(String remoteJenkinsFile) {
        if (StringUtils.isEmpty(remoteJenkinsFile)) {
            this.remoteJenkinsFile = RemoteJenkinsFileWorkflowBranchProjectFactory.defaultJenkinsFile;
        } else {
            this.remoteJenkinsFile = remoteJenkinsFile;
        }
    }

    /**
     * Jenkins @{@link DataBoundSetter}
     *
     * @param remoteJenkinsFileSCM @{@link SCM} definition for the Jenkinsfile
     */
    @DataBoundSetter
    public void setRemoteJenkinsFileSCM(SCM remoteJenkinsFileSCM) {
        this.remoteJenkinsFileSCM = remoteJenkinsFileSCM;
    }


    /**
     * Jenkins @{@link DataBoundConstructor}
     *
     * @param localFile            path of an arbitrary local file which must be present for the project to be recognised
     * @param remoteJenkinsFile    path of the Jenkinsfile
     * @param remoteJenkinsFileSCM @{@link SCM} definition for the Jenkinsfile
     */
    @DataBoundConstructor
    public RemoteJenkinsFileWorkflowBranchProjectFactory(String remoteJenkinsFile, String localFile, SCM remoteJenkinsFileSCM, boolean matchBranches) {
        this.localFile = localFile;
        this.remoteJenkinsFile = remoteJenkinsFile;
        this.remoteJenkinsFileSCM = remoteJenkinsFileSCM;
        this.matchBranches = matchBranches;
    }

    /**
     * Extends @{@link WorkflowBranchProjectFactory}
     *
     * @return @{@link FlowDefinition}
     */
    @Override
    protected FlowDefinition createDefinition() {
        return new ExtendedSCMBinder(this.remoteJenkinsFile, this.remoteJenkinsFileSCM, this.scmSourceBranchName, this.matchBranches);
    }

    /**
     * Extends @{@link WorkflowBranchProjectFactory}
     *
     * @param source @{@link SCMSource}
     * @return @{@link SCMSourceCriteria}
     */
    @Override
    protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return (probe, taskListener) -> {
            // Don't match if remote SCM of remoteFileName is not configured
            if (this.remoteJenkinsFileSCM == null || StringUtils.isEmpty(this.remoteJenkinsFile)) {
                return false;
            }
            this.setScmSourceBranchName(probe.name());
            return LocalFileSCMSourceCriteria.matches(this.localFile, probe, taskListener);
        };
    }

    /**
     * Descriptor Implementation for @{@link org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowMultiBranchProjectFactory}
     */
    @Extension
    public static class DescriptorImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {
        @Override
        public String getDisplayName() {
            return "by " + org.jenkinsci.plugins.workflow.multibranch.extended.Messages.ProjectRecognizer_DisplayName();
        }

        public Collection<? extends SCMDescriptor<?>> getApplicableDescriptors() {
            return SCMFilter.filter();
        }
    }

    /**
     * Default getter method
     * @return @this.remoteJenkinsFile
     */
    public String getRemoteJenkinsFile() {
        return remoteJenkinsFile;
    }

    /**
     * Default getter method
     * @return @this.remoteJenkinsFile
     */
    public SCM getRemoteJenkinsFileSCM() {
        return remoteJenkinsFileSCM;
    }

    /**
     * Default getter method
     * @return @this.localFile
     */
    public String getLocalFile() {
        return localFile;
    }


    /**
     *Jenkins @{@link DataBoundSetter}
     * @param matchBranches True to enable match branches feature
     */
    @DataBoundSetter
    public void setMatchBranches(boolean matchBranches) {
        this.matchBranches = matchBranches;
    }

    /**
     * Default getter method
     * @return @this.matchBranches
     */
    public boolean getMatchBranches() {
        return matchBranches;
    }

    /**
     * Set  @this.scmSourceBranchName to be used in new scm definition with new branch name
     * @param scmSourceBranchName Current branch name which MultiBranch pipeline working on.
     */
    public void setScmSourceBranchName(String scmSourceBranchName) {
        this.scmSourceBranchName = scmSourceBranchName;
    }

    /**
     * Default getter method
     * @return @this.scmSourceBranchName
     */
    public String getScmSourceBranchName() {
        return scmSourceBranchName;
    }
}