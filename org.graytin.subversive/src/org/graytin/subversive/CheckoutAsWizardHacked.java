package org.graytin.subversive;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.ObtainProjectNameOperation;
import org.eclipse.team.svn.ui.wizard.CheckoutAsWizard;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.handlers.DeleteBinaryDuplicates;

public class CheckoutAsWizardHacked extends CheckoutAsWizard {
	public CheckoutAsWizardHacked(IRepositoryResource[] resources) {
		super(resources);
	}

	/**
	 * Obtain a checkout operation from the Wizard
	 * 
	 * @param provider
	 * @param revisionToCheckoutFrom
	 * @return
	 */
	public CompositeOperation getCheckoutOut(IRepositoryResourceProvider provider, SVNRevision revisionToCheckoutFrom) {
		ObtainProjectNameOperation mainOp = new ObtainProjectNameOperation(provider);
		final CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		AbstractActionOperation checkoutProjectOperation = getCheckoutProjectOperation(resources, mainOp, Depth.INFINITY, revisionToCheckoutFrom);
		op.add(checkoutProjectOperation, new IActionOperation[] { mainOp });

		if (SDKActivator.getDefault().getPreferenceStore().getBoolean(SDKBundleUpdatePreferencePage.DO_DELETE_AUTOMATICALLY)) {
			op.add(new AbstractActionOperation(Messages.CheckoutAsWizardHacked_DeleteDuplicatedBundlesOperation, SVNUIMessages.class) {

				@Override
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							DeleteBinaryDuplicates deleteAction = new DeleteBinaryDuplicates();
							try {
								deleteAction.execute(new ExecutionEvent());
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}, new IActionOperation[] { mainOp, checkoutProjectOperation});
		}

		return op;
	}

	/**
	 * Retrieve all projects within depth of defined in the preferences.
	 */
	public SearchProjectOperation retrieveAllProjects(IRepositoryResource[] resources, SVNRevision revisionToCheckoutFrom) {
		for (int i = 0; i < resources.length; i++) {
			IRepositoryResource tmpResource = SVNUtility.copyOf(resources[i]);
			tmpResource.setSelectedRevision(revisionToCheckoutFrom);
			resources[i] = tmpResource;
		}
		return new SearchProjectOperation(resources);
	}

}
