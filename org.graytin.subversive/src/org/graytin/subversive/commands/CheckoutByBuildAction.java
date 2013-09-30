package org.graytin.subversive.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryModifyWorkspaceAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.PlatformUI;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;
import org.graytin.jenkins.update.dialog.BuildSelectionDialog;
import org.graytin.jenkins.update.jobs.RetrieveBuildsJob;
import org.graytin.subversive.CheckoutAsWizardHacked;
import org.graytin.subversive.Messages;
import org.graytin.subversive.ProjectSelectionDialog;
import org.graytin.subversive.RepoLabelProvider;
import org.graytin.subversive.SVNUtil;
import org.graytin.subversive.SearchProjectOperation;

/**
 * UI Checkout As action using a build revision
 * 
 * @author Andrej Zachar
 */
public class CheckoutByBuildAction extends AbstractRepositoryModifyWorkspaceAction {

	public void runImpl(IAction ignore) {

		final BuildSelectionDialog dialog = new BuildSelectionDialog(Display.getCurrent().getActiveShell(), new BuildLabelProvider(), false);
		final RetrieveBuildsJob action = new RetrieveBuildsJob(true);

		action.setUser(true);
		action.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ignore) {
				final List<Build> builds = action.getBuilds();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						dialog.setElements(builds, BuildSelectionDialog.Strategy.current);
						dialog.setTitle(Messages.CheckoutByBuildAction_BuildSelectionDialogTitle);
						// User pressed cancel
						if (dialog.open() != Window.OK) {
							return;
						}
						buildSelected(dialog.getSelectedBuild());

					}

				});

			}
		});
		action.schedule();

	}

	public void buildSelected(final Build selectedBuild) {
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IRepositoryResource[] selectedResources = retrieveSVNProjects();
						openCheckoutSearchDialog(selectedBuild, monitor, selectedResources);

					} finally {
						monitor.done();
					}
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

	private IRepositoryResource[] retrieveSVNProjects() {
		IRepositoryResource[] selectedResources = getSelectedRepositoryResources();
		IRepositoryLocation[] locations = getSelectedRepositoryLocations();
		if (selectedResources.length == 0) {
			selectedResources = new IRepositoryResource[locations.length];
			for (int i = 0; i < locations.length; i++) {
				selectedResources[i] = locations[i].getRoot();
			}
		}
		return selectedResources;
	}

	public void openCheckoutSearchDialog(final Build selectedBuild, IProgressMonitor monitor, final IRepositoryResource[] selectedResources) {
		final CheckoutAsWizardHacked hack = new CheckoutAsWizardHacked(selectedResources);
		final SVNRevision revision = SVNUtil.getRevision(selectedBuild);

		SearchProjectOperation mainOp = hack.retrieveAllProjects(selectedResources, revision);
		mainOp.run(monitor);
		final IRepositoryResource[] allProjects = mainOp.getRepositoryResources();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (allProjects == null || allProjects.length == 0) {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.CheckoutByBuildAction_SVNIssueTitle,
							NLS.bind(Messages.CheckoutByBuildAction_SVNIssueMessage, selectedResources)); //$NON-NLS-1$
					return;
				}
				final ProjectSelectionDialog dialog = new ProjectSelectionDialog(getShell(), new RepoLabelProvider());

				dialog.setElements(allProjects);
				dialog.setTitle(Messages.CheckoutByBuildAction_ProjectSelectionDialogTitle + selectedBuild.getLabel());
				// User pressed cancel
				if (dialog.open() != Window.OK) {
					return;
				}

				UIMonitorUtility.doTaskScheduledActive(hack.getCheckoutOut(dialog, revision));
			}
		});
	}

	public boolean isEnabled() {
		return true;
	}

}
