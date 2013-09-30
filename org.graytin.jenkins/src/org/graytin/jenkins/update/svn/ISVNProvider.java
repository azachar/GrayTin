package org.graytin.jenkins.update.svn;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.graytin.jenkins.jenkins.Build;

/**
 * The Provider is implemented in a separate plugin that has dependency for certain team providers (like subversive, git or subclipse).
 * 
 * It is used to update all workspace project to certain revisions.
 */
public interface ISVNProvider {

	IStatus updateAllWorkspaceProjectsToBuildRevision(IProgressMonitor monitor, Build from, Build to);

	String getProjectDifference(Build from, Build to, IProgressMonitor monitor);

	long resolveRevision(IResource resource);
}
