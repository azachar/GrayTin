package org.graytin.jenkins.update.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.dialog.Messages;

/**
 * Obtain an accessible sdk-bundles folder
 * 
 */
public abstract class BaseHandler extends AbstractHandler {

	protected IPreferenceStore prefStore;

	protected IFolder sdkBundlesFolder;

	@Override
	final public Object execute(ExecutionEvent event) throws ExecutionException {
		prefStore = SDKActivator.getDefault().getPreferenceStore();
		sdkBundlesFolder = SDKActivator.getSDKBundleFolder();
		if (!sdkBundlesFolder.isAccessible()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					boolean create = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), Messages.BaseHandler_SuiteName,
							NLS.bind(Messages.BaseHandler_FolderIsNotAccessibleCreateTitle, sdkBundlesFolder)); //$NON-NLS-1$
					if (create) {
						try {
							sdkBundlesFolder.create(true, true, new NullProgressMonitor());
						} catch (CoreException e) {
							System.err.print(e.getLocalizedMessage());
						}
					}
				}
			});
		}
		if (sdkBundlesFolder.isAccessible()) {
			doExecute(event);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.BaseHandler_SuiteName,
							NLS.bind(Messages.BaseHandler_FolderIsNotAccessibleSecondTimeTitle, sdkBundlesFolder));
				}
			});
		}
		return null;
	}

	protected abstract void doExecute(ExecutionEvent event);
}
