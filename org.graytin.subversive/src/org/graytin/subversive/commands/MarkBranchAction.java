package org.graytin.subversive.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractRepositoryModifyWorkspaceAction;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.handlers.DownloadSDKBundles;
import org.graytin.subversive.Messages;

/**
 * UI Checkout As action using a build revision
 * 
 * @author Andrej Zachar
 */
public class MarkBranchAction extends AbstractRepositoryModifyWorkspaceAction {

	public void runImpl(IAction ignore) {
		IRepositoryResource[] selectedResources = retrieveSVNProjects();
		markBranch(selectedResources);

		String url = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.BRANCH_URL);

		MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.MarkBranchAction_BranchSelectedDialogTitle,
				NLS.bind(Messages.MarkBranchAction_BranchSelected, url)); //$NON-NLS-2$
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

	@Override
	public boolean isEnabled() {
		return true;
	}

	public static IRepositoryResource[] getMarkedBranchAsArray() {
			IRepositoryResource markedBranch = getMarkedBranch();
			if (markedBranch != null) {
				return new IRepositoryResource[] { markedBranch };
			}
			return null;
	}

	public static IRepositoryResource getMarkedBranch() {
		String url = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.BRANCH_URL);
		if (url != null) {
			IRepositoryResource asRepositoryResource = SVNUtility.asRepositoryResource(url, true);
			if (asRepositoryResource != null) {
				return asRepositoryResource;
			}

		}
		return null;
	}

	public static void markBranch(IRepositoryResource[] lastSelectedResources) {
		String url = lastSelectedResources[0].getUrl();
		SDKActivator.getDefault().getPreferenceStore().setValue(SDKBundleUpdatePreferencePage.BRANCH_URL, url);
		DownloadSDKBundles.updateProjectDecorators();
	}
}
