package org.graytin.jenkins.update.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.update.SDKActivator;

/**
 * Deletes bundles which are checked out as project. Useful when the bundles are up-to-date, but just checkout some more bundles from svn. This job is lightning
 * fast compared to "Update SDK bundles"
 */
public class DeleteBinaryDuplicates extends BaseHandler {

	private static final String JOB_NAME = "Deleting binary duplicates of source projects";

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	protected void doExecute(final ExecutionEvent executionEvent) {
		Job sdkUpdateJob = new DeleteSDKBundlesJob(JOB_NAME);
		sdkUpdateJob.setUser(true);
		sdkUpdateJob.schedule();
		sdkUpdateJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				final IStatus resultStatus = event.getJob().getResult();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (resultStatus.isOK()) {
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Delete binary duplicates in SDK-BUNDLES folder",
									resultStatus.getMessage());
						} else {
							MessageDialog.openError(Display.getCurrent().getActiveShell(), "Delete binary duplicates in SDK-BUNDLES folder",
									resultStatus.getMessage());
						}
					}
				});

			}
		});

	}

	class DeleteSDKBundlesJob extends Job {

		public DeleteSDKBundlesJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			System.out.println(JOB_NAME);
			try {
				int deletedBundles = 0;
				StringBuilder builder = new StringBuilder();
				try {
					IResource[] bundles = sdkBundlesFolder.members();
					monitor.beginTask("Process bundles ", bundles.length);
					for (IResource resource : bundles) {
						String bundleFileName = resource.getName();
						if (hasMatchingProjectInWorkspace(bundleFileName)) {
							System.out.println("Deleting: " + bundleFileName);
							resource.delete(true, null);
							deletedBundles++;
							builder.append(",");
							builder.append(bundleFileName);
						}
						monitor.worked(1);
					}
					System.out.format("Deleted %d bundles.", deletedBundles);
				} catch (CoreException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, "Error reading or deleting SDK bundles");
				}

				// Refresh workspace?
				monitor.worked(1);
				String deletedBundlesNames = "";
				if (deletedBundles > 0) {
					deletedBundlesNames = "\n" + builder.substring(1);
				}
				return new Status(Status.OK, SDKActivator.PLUGIN_ID, "Deleted " + deletedBundles + " binary bundles " + deletedBundlesNames + ".");
			} finally {
				monitor.done();
			}
		}

	}

	public boolean hasMatchingProjectInWorkspace(String bundleFileName) {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isAccessible() && bundleFileName.startsWith(project.getName() + "_")) {
				return true;
			}
		}
		return false;
	}

}
