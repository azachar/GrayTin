package org.graytin.jenkins.update.jobs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.ui.preferences.AddToJavaSearchJob;
import org.graytin.jenkins.update.SDKActivator;

@SuppressWarnings("restriction")
public class TargetPlatformReloader {
	public void setTargetPlatform(final IProgressMonitor monitor) throws CoreException {
		IFile targetFile = SDKActivator.getTargetPlatformFile();
		monitor.subTask("Setting up target platform (" + targetFile + ")");
		// acquire target platform service
		TargetPlatformService service = (TargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
		ITargetHandle targetHandle = service.getTarget(targetFile.getLocationURI());
		final ITargetDefinition targetDefinition = targetHandle.getTargetDefinition();

		IJobChangeListener jca = new JobChangeListenerImpl(targetDefinition);

		// When loading Target is done, rebuild java search scope index. 
		LoadTargetDefinitionJob.load(targetDefinition, jca);
		monitor.worked(1);
	}

	private final class JobChangeListenerImpl extends JobChangeAdapter {
		private final ITargetDefinition targetDefinition;

		private JobChangeListenerImpl(ITargetDefinition targetDefinition) {
			this.targetDefinition = targetDefinition;
		}

		@Override
		public void done(IJobChangeEvent event) {
			System.out.println("Done updating SDK bundles. Rebuilding Java search scope index...");
			AddToJavaSearchJob.synchWithTarget(targetDefinition);
		}
	}
}
