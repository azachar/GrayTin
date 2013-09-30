package org.graytin.jenkins.update.jobs;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.InProgressBuild;
import org.graytin.jenkins.jenkins.JenkinsUtils;
import org.graytin.jenkins.jenkins.NABuild;
import org.graytin.jenkins.update.SDKActivator;

/**
 * Download bundles, clean up sdk bundles folder, extract downloaded bundles for projects that are not availabel in the workpace, sets target platform.
 */
public class DownloadSdkBundlesJob extends Job {
	final private JenkinsUtils jenkinsUtils = new JenkinsUtils();;

	final private IFolder sdkBundlesFolder;

	final private Build build;

	public DownloadSdkBundlesJob(final IFolder targetFolder, final Build selectedBuild) {
		super("Update SDK bundles to build " + selectedBuild);
		this.sdkBundlesFolder = targetFolder;
		this.build = selectedBuild;
		assert build != null;
		assert sdkBundlesFolder != null;
	}

	boolean hasAccesibleMatchingProjectInWorkspace(String bundleFileName) {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isAccessible() && bundleFileName.startsWith(project.getName() + "_")) {
				return true;
			}
		}
		return false;
	}

	static boolean doDownload = true;

	@SuppressWarnings("deprecation")
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask("Updating SDK bundles in " + sdkBundlesFolder.getName(), 8);
		try {

			IFolder downloadFolder = SDKActivator.getSDKBundleFolder().getFolder(".downloads");
			if (!downloadFolder.isAccessible()) {
				downloadFolder.create(true, true, new SubProgressMonitor(monitor, 1));
			}
			downloadFolder.refreshLocal(IResource.DEPTH_ONE, monitor);

			//TODO remove hack using static method
			doDownload = true;
			final IFile downloadFile = downloadFolder.getFile("plugins-" + build.getNumber() + "_" + build.getRevision() + ".zip");
			if (downloadFile.isAccessible()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						DownloadSdkBundlesJob.doDownload = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Download again?",
								NLS.bind("The file {0} is already downloaded, do you want to download again?", downloadFile.getName())); //$NON-NLS-1$
					}
				});
			}

			if (doDownload) {
				monitor.subTask("Downloading.. please wait!");
				jenkinsUtils.getExecutor().execute(Request.Get(build.getArchiveToDownloadURL())).saveContent(downloadFile.getFullPath().toFile());
				monitor.worked(1);
			}

			if (!downloadFile.isAccessible()) {
				return Status.CANCEL_STATUS;
			}
			Build.save(new InProgressBuild(build));

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			cleanUpFolder(monitor);

			extract(monitor, new ZipInputStream(downloadFile.getContents()));

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			new TargetPlatformReloader().setTargetPlatform(monitor);

			build.save();
			monitor.worked(1);
			return new Status(Status.OK, SDKActivator.PLUGIN_ID, "Ok");

		} catch (Exception e) {
			e.printStackTrace();
			Build.save(new NABuild());
			return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, e.getLocalizedMessage());
		} finally {
			monitor.done();
		}
	}

	private void extract(final IProgressMonitor monitor, ZipInputStream zip) throws IOException {
		monitor.subTask("Extracting SDK bundles");
		try {
			ZipEntry entry = null;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					if (monitor.isCanceled()) {
						return;
					}
					String strippedName = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
					// remove those bundles that are present as project in the workspace
					if (hasAccesibleMatchingProjectInWorkspace(strippedName)) {
						deleteFile(sdkBundlesFolder.getFile(strippedName), monitor);
					} else {
						// unzip to the selected folder
						monitor.subTask("Extracting " + entry);
						int count;
						int BUFFER = 2048;
						byte data[] = new byte[BUFFER];
						// write the files to the disk
						IFile file = sdkBundlesFolder.getFile(strippedName);

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						OutputStream dest = new BufferedOutputStream(bos, BUFFER);
						while ((count = zip.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();

						ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
						deleteFile(file, monitor);
						try {
							file.create(bin, true, new NullProgressMonitor());
						} catch (CoreException e) {
							System.err.format("Error creating file %s; %s\n", file.getName(), e.getMessage());
						}
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(zip);
		}
		monitor.worked(1);
	}

	private void cleanUpFolder(final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Deleting existing files.");
		// delete all files in the target folder

		sdkBundlesFolder.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) {
				if (resource instanceof IFile) {
					deleteFile((IFile) resource, monitor);
				}
				return true;
			}
		}, IResource.DEPTH_ONE, false);
		monitor.worked(1);
	}

	private void deleteFile(IFile file, final IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}
		if (file.exists()) {
			try {
				file.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				System.err.format("Could not delete %s; %s\n", file.getName(), e.getMessage());
			}
		}
	}

}
