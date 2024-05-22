package io.jenkins.plugins.multibranch;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue;
import jenkins.advancedqueue.priority.strategy.AbstractDynamicPriorityStrategy;
import jenkins.branch.Branch;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

public class BranchPriorityStrategy extends AbstractDynamicPriorityStrategy {
    @Extension
    static public class BranchPriorityStrategyDescriptor extends AbstractDynamicPriorityStrategyDescriptor {

        public BranchPriorityStrategyDescriptor() {
            super("Set Priority from branch name");
        }
    };

    private String branchName;
    private Boolean pullRequestMatchOriginName;
    private int priority;

    @DataBoundConstructor
    public BranchPriorityStrategy(String branchName, Boolean pullRequestMatchOriginName, int priority) {
        this.branchName = branchName;
        this.pullRequestMatchOriginName = pullRequestMatchOriginName;
        this.priority = priority;
    }

    @Override
    public boolean isApplicable(Queue.Item item) {
        return true;
    }

    @Override
    public int getPriority(Queue.Item item) {
        if(item.task instanceof Job<?, ?>) {
            Job<?, ?> job = (Job<?, ?>) item.task;
            BranchJobProperty priorityProperty = job.getProperty(BranchJobProperty.class);
            if (priorityProperty != null) {
                Branch branch = priorityProperty.getBranch();
                String originName = branch.getName();
                if (pullRequestMatchOriginName && branch.getHead() instanceof ChangeRequestSCMHead2) {
                    originName = ((ChangeRequestSCMHead2) branch.getHead()).getOriginName();
                }

                if (originName.matches(branchName)) {
                    return priority;
                }
            }
        }
        return 3;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean getPullRequestMatchOriginName() {
        return pullRequestMatchOriginName;
    }

    public int getPriority() {
        return priority;
    }
}
