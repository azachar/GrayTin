package org.graytin.jenkins.update.jobs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
//
// Enable this in order to make a build for Eclipse Indigo
//
//
// Enable this in order to make a build for Eclipse Juno and Kepler
//
// import org.eclipse.pde.core.target.ITargetDefinition;
// import org.eclipse.pde.core.target.ITargetHandle;
// import org.eclipse.pde.core.target.ITargetPlatformService;
// import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
// import org.eclipse.pde.internal.core.PDECore;
// import org.eclipse.pde.internal.core.target.TargetPlatformService;
// import org.eclipse.pde.internal.ui.preferences.AddToJavaSearchJob;

public class TargetPlatformReloader {
	public void setTargetPlatform(final IProgressMonitor monitor) throws CoreException {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Missing fragment that does reload functionality!",
						"Please reload target platform manualy!");
			}
		});
	}
}
