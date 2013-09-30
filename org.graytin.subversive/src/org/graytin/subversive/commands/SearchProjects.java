package org.graytin.subversive.commands;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.subversive.Messages;


public class SearchProjects extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) {
		if (MarkBranchAction.getMarkedBranchAsArray() != null) {
			CheckoutByBuildAction action = new CheckoutByBuildAction();
			action.openCheckoutSearchDialog(Build.current(), new NullProgressMonitor(), MarkBranchAction.getMarkedBranchAsArray());
		} else {
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.SearchProjects_Title,
					Messages.SearchProjects_ActionIsNotAvailable);
		}

		return null;
	}
}
