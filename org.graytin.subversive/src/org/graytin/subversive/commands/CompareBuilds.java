package org.graytin.subversive.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.PlatformUI;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.NABuild;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;
import org.graytin.jenkins.update.dialog.BuildSelectionDialog;
import org.graytin.jenkins.update.jobs.RetrieveBuildsJob;
import org.graytin.subversive.CheckoutAsWizardHacked;
import org.graytin.subversive.Messages;
import org.graytin.subversive.ProjectSelectionDialog;
import org.graytin.subversive.RepoLabelProvider;
import org.graytin.subversive.SVNUtil;
import org.graytin.subversive.SearchProjectOperation;
import org.graytin.subversive.SubversiveSVNProviderImpl;

public class CompareBuilds extends AbstractHandler {

	public static class CompareResult {
		public final List<IRepositoryResource> added;

		public final List<IRepositoryResource> removed;

		public final List<String> removedURL = new ArrayList<String>();

		public List<IRepositoryResource> before;

		public List<IRepositoryResource> after;

		public final Build from;

		public final Build to;

		public CompareResult(List<IRepositoryResource> before, List<IRepositoryResource> after, Build from, Build to) {
			this.before = before;
			this.after = after;
			this.from = from;
			this.to = to;

			Set<String> beforeUrls = getAllUrls(before);

			added = new ArrayList<IRepositoryResource>();
			for (IRepositoryResource resource : after) {
				if (!beforeUrls.contains(resource.getUrl())) {
					added.add(resource);
				}
			}

			Set<String> afterUrls = getAllUrls(after);

			removed = new ArrayList<IRepositoryResource>();
			for (IRepositoryResource resource : before) {
				String url = resource.getUrl();
				if (!afterUrls.contains(url)) {
					removed.add(resource);
					//remove additional slasshess
					String fixedUrl = resource.getRepositoryLocation().getRepositoryRootUrl()
							+ url.substring(resource.getRepositoryLocation().getRepositoryRootUrl().length()).replace("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
					removedURL.add(fixedUrl);

				}
			}

		}

		private Set<String> getAllUrls(List<IRepositoryResource> before) {
			Set<String> ulr = new HashSet<String>(before.size());
			for (IRepositoryResource resource : before) {
				ulr.add(resource.getUrl());
			}
			return ulr;
		}

		@Override
		public String toString() {
			return NLS.bind(Messages.CompareBuilds_CompareResult, from, to) + "\n" + getChangesAsString(); //$NON-NLS-2$ //$NON-NLS-3$
		}

		private String getChangesAsString() {
			String result = ""; //$NON-NLS-1$
			if (!added.isEmpty()) {
				result = Messages.CompareBuilds_Added + getString(added);
			}

			if (!removed.isEmpty()) {
				if (!result.isEmpty()) {
					result += "\n\n";
				}
				result += Messages.CompareBuilds_Removed + getString(removed);
			}
			return result;
		}

		public String getSizeDiff() {
			return NLS.bind(Messages.CompareBuilds_SizeSummary, new Object[] { after.size(), before.size(), after.size() - before.size() });
		}

		private String getString(List<IRepositoryResource> repositoryResourcesAsList) {
			StringBuilder result = new StringBuilder();
			int i = 0;
			for (IRepositoryResource iRepositoryResource : repositoryResourcesAsList) {
				result.append(" "); //$NON-NLS-1$
				result.append(StringUtils.abbreviateMiddle(iRepositoryResource.getParent().getName() + "\\" + iRepositoryResource.getName(), "~", 70)); //$NON-NLS-1$
				result.append("\n"); //$NON-NLS-1$
				i++;
				if (i > 7) {
					result.append("and "); //$NON-NLS-1$
					result.append(repositoryResourcesAsList.size() - i);
					result.append(" others"); //$NON-NLS-1$
					break;
				}
			}

			return result.toString();
		}

		public boolean hasDifference() {
			return !added.isEmpty() || !removed.isEmpty();
		}

		public boolean hasDetectedRemovalOfProject(IProject project) {
			IRepositoryResource repositoryResource = SVNRemoteStorage.instance().asRepositoryResource(project);
			return hasDetectedRemovalOfProject(repositoryResource);
		}

		public boolean hasDetectedRemovalOfProject(IRepositoryResource repositoryResource) {
			//euals on resource repository directly doesn't work
			return removedURL.contains(repositoryResource.getUrl());
		}
	}

	@Override
	public Object execute(final ExecutionEvent event) {
		final BuildSelectionDialog dialog = new BuildSelectionDialog(Display.getCurrent().getActiveShell(), new BuildLabelProvider(), true);
		final RetrieveBuildsJob action = new RetrieveBuildsJob(true);

		action.setUser(true);
		action.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ignore) {
				final List<Build> builds = action.getBuilds();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						dialog.setElements(builds, BuildSelectionDialog.Strategy.current);
						dialog.setTitle(Messages.CompareBuilds_DialogTitle);
						// User pressed cancel
						if (dialog.open() != Window.OK) {
							return;
						}
						try {
							PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {

								@Override
								public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
									compareSelectedBuilds(dialog.getSelectedBuild(), dialog.getSecondBuild(), monitor);
								}
							});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				});

			}
		});
		action.schedule();

		return null;
	}

	private void compareSelectedBuilds(Build from, Build to, IProgressMonitor monitor) {
		if (MarkBranchAction.getMarkedBranchAsArray() != null && !from.equals(to) && !((to instanceof NABuild) || (from instanceof NABuild))) {
			manageFolderDiffBetweenBuilds(from, to, monitor, MarkBranchAction.getMarkedBranchAsArray(), false);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.CompareBuilds_ActionIsNotAvailableTitle,
							Messages.CompareBuilds_ActionIsNotAvailableWarning);
				}
			});
		}

	}

	public CompareResult getFolderDiffBetweenBuilds(final Build firstBuild, final Build secondBuild, IProgressMonitor monitor,
			final IRepositoryResource[] selectedResources) {
		monitor.setTaskName(NLS.bind(Messages.CompareBuilds_TaskName, firstBuild.getNumber(), secondBuild.getNumber()));
		final List<IRepositoryResource> before = retrieveProjects(firstBuild, monitor, selectedResources);
		final List<IRepositoryResource> after = retrieveProjects(secondBuild, monitor, selectedResources);
		CompareResult compareResult = new CompareResult(before, after, firstBuild, secondBuild);

		SubversiveSVNProviderImpl.setLastDiffBetweenBuilds(compareResult);
		return compareResult;

	}

	public void manageFolderDiffBetweenBuilds(final Build firstBuild, final Build secondBuild, IProgressMonitor monitor,
			final IRepositoryResource[] selectedResources, boolean headless) {

		final CompareResult compareResult = getFolderDiffBetweenBuilds(firstBuild, secondBuild, monitor, selectedResources);

		if (headless && !compareResult.hasDifference()) {
			return;
		}
		checkoutNewAddedProjectIfNeeded(secondBuild, selectedResources, compareResult);
	}

	private void checkoutNewAddedProjectIfNeeded(final Build secondBuild, final IRepositoryResource[] selectedResources, final CompareResult compareResult) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.CompareBuilds_CompareResultsDialogTitle, compareResult.toString()
						+ Messages.CompareBuilds_Summary + compareResult.getSizeDiff());

				final ProjectSelectionDialog dialog = new ProjectSelectionDialog(Display.getDefault().getActiveShell(), new RepoLabelProvider());
				if (!compareResult.added.isEmpty()) {
					dialog.setElements(compareResult.added.toArray());
					dialog.setTitle(NLS.bind(Messages.CompareBuilds_ProjectSelectionDialog, secondBuild)); //$NON-NLS-2$
					// User pressed cancel
					if (dialog.open() != Window.OK) {
						return;
					}

					UIMonitorUtility.doTaskScheduledActive(new CheckoutAsWizardHacked(selectedResources).getCheckoutOut(dialog,
							SVNUtil.getRevision(secondBuild)));
				}
			}
		});
	}

	private List<IRepositoryResource> retrieveProjects(final Build build, IProgressMonitor monitor, IRepositoryResource[] selectedResources) {
		final CheckoutAsWizardHacked hack = new CheckoutAsWizardHacked(selectedResources);
		SearchProjectOperation searchProjectOperation = hack.retrieveAllProjects(selectedResources, SVNUtil.getRevision(build));
		searchProjectOperation.run(new SubProgressMonitor(monitor, 1));
		return searchProjectOperation.getRepositoryResourcesAsList();
	}

}
