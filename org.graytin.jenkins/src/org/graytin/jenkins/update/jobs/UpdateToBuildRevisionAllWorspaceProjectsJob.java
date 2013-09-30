package org.graytin.jenkins.update.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.svn.ISVNProvider;

/**
 * Update all workspace project with the revision provided in the build.
 * 
 * If there are no team provider does nothing!
 */
public class UpdateToBuildRevisionAllWorspaceProjectsJob extends Job {

	private final Build to;
	private final Build from;

	public UpdateToBuildRevisionAllWorspaceProjectsJob(Build from, Build to) {
		super("Updating workspace to the revision #" + to.getRevision());
		this.from = from;
		this.to = to;
	}

	public ISVNProvider getSvnProvider() {
		return SDKActivator.getDefault().getSvnProvider();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (getSvnProvider() != null) {
			return getSvnProvider().updateAllWorkspaceProjectsToBuildRevision(monitor, from, to);
		}
		return Status.CANCEL_STATUS;
	}

}
