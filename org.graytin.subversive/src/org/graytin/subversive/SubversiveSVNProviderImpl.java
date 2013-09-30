package org.graytin.subversive;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.local.UpdateAction;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.handlers.DownloadSDKBundles;
import org.graytin.jenkins.update.svn.ISVNProvider;
import org.graytin.subversive.commands.CompareBuilds;
import org.graytin.subversive.commands.MarkBranchAction;
import org.graytin.subversive.commands.CompareBuilds.CompareResult;

/**
 * Update all workspace project shared with subversion with the provided revision
 */
public class SubversiveSVNProviderImpl implements ISVNProvider {

	protected volatile IResource[] resources;

	private static volatile CompareResult diffBetweenBuilds;

	public IStatus updateAllWorkspaceProjectsToBuildRevision(IProgressMonitor monitor, Build from, Build to) {
		try {
			doUpdateToBuildRevision(monitor, from, to);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			System.err.println(e.getLocalizedMessage());
			return Status.CANCEL_STATUS;
		}
	}

	private void doUpdateToBuildRevision(IProgressMonitor parentMonitor, Build from, final Build to) throws CoreException {
		IProgressMonitor monitor = new SubProgressMonitor(parentMonitor, 2);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(new Shell(),
							getSelectedResources(IStateFilter.SF_ONREPOSITORY));
				} catch (CoreException e) {
					System.err.println(e);
				}
			}
		});
		if (resources == null || resources.length == 0) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog
							.openWarning(
									Display.getCurrent().getActiveShell(),
									"No SVN shared project(s) found!",
									"GrayTin is unable to update workspace project to the build "
											+ to
											+ " revision because of missing projects to update. \nPlease ensure that you have at least one project shared with Subversive plugin! Subclipse is not supported yet!");
				}
			});
			return;
		}
		if (monitor.isCanceled()) {
			return;
		}

		CompositeOperation updateOperation = UpdateAction.getUpdateOperation(resources, SVNRevision.fromString(to.getRevision()));
		updateOperation.run(new SubProgressMonitor(monitor, 1));

		if (monitor.isCanceled()) {
			return;
		}
		CompareBuilds compareBuilds = new CompareBuilds();
		compareBuilds.manageFolderDiffBetweenBuilds(from, to, new SubProgressMonitor(monitor, 1), MarkBranchAction.getMarkedBranchAsArray(), true);

	}

	private IResource[] getSelectedResources(IStateFilter filter) throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		List<IProject> sdkProjects = new ArrayList<IProject>();
		for (IProject project : projects) {
			if (project.isAccessible()) {
				try {
					IRepositoryResource repositoryProject = SVNRemoteStorage.instance().asRepositoryResource(project);
					if (repositoryProject != null && repositoryProject.getUrl().startsWith(MarkBranchAction.getMarkedBranch().getUrl())) {
						sdkProjects.add(project);
					}
				} catch (Exception e) {
					//project was not shared
				}
			}
		}

		return FileUtility.getResourcesRecursive(projects, filter, IResource.DEPTH_ZERO);
	}

	@Override
	public String getProjectDifference(Build from, Build to, IProgressMonitor monitor) {
		try {
			CompareBuilds compareBuilds = new CompareBuilds();
			CompareResult compareResult = compareBuilds.getFolderDiffBetweenBuilds(from, to, monitor, MarkBranchAction.getMarkedBranchAsArray());
			if (compareResult.hasDifference()) {
				return compareResult.toString();
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to obtain the difference between builds " + from + " and " + to + " due to:" + e.getLocalizedMessage();
		}
	}

	public static CompareResult getLastDiffBetweenBuilds() {
		return diffBetweenBuilds;
	}

	public static void setLastDiffBetweenBuilds(CompareResult compareResult) {
		diffBetweenBuilds = compareResult;
		// Decorators update using current UI thread 
		DownloadSDKBundles.updateProjectDecorators();
	}

	@Override
	public long resolveRevision(IResource resource) {
		if (resource != null) {
			ILocalFolder local = (ILocalFolder) SVNRemoteStorage.instance().asLocalResource(resource);
			if (IStateFilter.SF_VERSIONED.accept(local)) {
				return local.getBaseRevision();
			}
		}
		return SVNRevision.INVALID_REVISION_NUMBER;
	}

}
