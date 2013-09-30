package org.graytin.jenkins.update.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;
import org.graytin.jenkins.update.dialog.BuildSelectionDialog;
import org.graytin.jenkins.update.jobs.RetrieveBuildsJob;
import org.graytin.jenkins.update.jobs.UpdateToBuildRevisionAllWorspaceProjectsJob;

public class UpdateWorkspaceToBuildRevision extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		final BuildSelectionDialog dialog = new BuildSelectionDialog(HandlerUtil.getActiveShell(event), new BuildLabelProvider(), false);

		final RetrieveBuildsJob action = new RetrieveBuildsJob(true);

		action.setUser(true);
		action.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ignore) {
				final List<Build> builds = action.getBuilds();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						dialog.setElements(builds, SDKActivator.isSDKStyle() ? BuildSelectionDialog.Strategy.current : BuildSelectionDialog.Strategy.latest);
						dialog.setTitle("Choose a build's revision you want update all of your shared resources");
						// User pressed cancel
						if (dialog.open() != Window.OK) {
							return;
						}
						updateWorkspaceProjectToRevisionOfTheBuild(Build.current(), dialog.getSelectedBuild());

					}
				});

			}
		});
		action.schedule();
		return null;
	}

	protected void updateWorkspaceProjectToRevisionOfTheBuild(final Build from, final Build to) {
		UpdateToBuildRevisionAllWorspaceProjectsJob updateJob = new UpdateToBuildRevisionAllWorspaceProjectsJob(from, to);
		updateJob.setUser(true);
		updateJob.schedule();
		updateJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ignore) {

				if (!SDKActivator.isSDKStyle()) {
					to.save();
					return;
				}

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						boolean nonSDK = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Do you work NON-SDK style?",
								"Do you want to mark build (" + to + ") as current build. This option is handy when you work NON-SDK style, otherwise say no"); //$NON-NLS-1$
						if (nonSDK) {
							to.save();
						}
					}
				});
				System.out.println("Revisions of workspace where updated to build: " + to);
			}
		});
	}
}
