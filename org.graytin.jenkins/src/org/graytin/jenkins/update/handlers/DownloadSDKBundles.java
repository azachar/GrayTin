package org.graytin.jenkins.update.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;
import org.graytin.jenkins.update.dialog.BuildSelectionDialog;
import org.graytin.jenkins.update.jobs.DownloadSdkBundlesJob;
import org.graytin.jenkins.update.jobs.RetrieveBuildsJob;
import org.graytin.jenkins.update.jobs.UpdateToBuildRevisionAllWorspaceProjectsJob;

public class DownloadSDKBundles extends BaseHandler {

	protected void doExecute(final ExecutionEvent event) {
		final BuildSelectionDialog dialog = new BuildSelectionDialog(HandlerUtil.getActiveShell(event), new BuildLabelProvider(), false);

		final RetrieveBuildsJob action = new RetrieveBuildsJob(false);

		action.setUser(true);
		action.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ignore) {
				final List<Build> builds = action.getBuilds();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						dialog.setElements(builds, BuildSelectionDialog.Strategy.latest);
						dialog.setTitle("Choose a build you want to download your bundles to the SDK-BUNDLES folder..");
						// User pressed cancel
						if (dialog.open() != Window.OK) {
							return;
						}
						downloadAndUpdate(event, Build.current(), dialog.getSelectedBuild());

					}
				});

			}
		});
		action.schedule();
	}

	protected void downloadAndUpdate(final ExecutionEvent event, final Build from, final Build to) {
		System.out.println("Updating to the build: " + to);

		Job sdkUpdateJob = new DownloadSdkBundlesJob(sdkBundlesFolder, to);
		sdkUpdateJob.setUser(true);
		sdkUpdateJob.schedule();
		sdkUpdateJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				updateProjectDecorators();
				if (event.getResult().isOK()) {

					if (prefStore.getBoolean(SDKBundleUpdatePreferencePage.DO_UPDATE_TO_REVISION_AUTOMATICALLY)) {

						UpdateToBuildRevisionAllWorspaceProjectsJob svnUpdateToBuild = new UpdateToBuildRevisionAllWorspaceProjectsJob(from, to);
						svnUpdateToBuild.setUser(true);
						svnUpdateToBuild.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent ignore) {
								updateProjectDecorators();
							}

						});
						svnUpdateToBuild.schedule();
					}
				}
			}

		});

	}

	public static void updateProjectDecorators() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update("org.graytin.subversive.ProjectDecorator");
				PlatformUI.getWorkbench().getDecoratorManager().update("org.graytin.subversive.BuildDecorator");
				PlatformUI.getWorkbench().getDecoratorManager().update("org.graytin.subversive.ResourceRepositoryDecorator");
				PlatformUI.getWorkbench().getDecoratorManager().update("org.graytin.subversive.BranchDecorator");
			}
		});
	}
}
