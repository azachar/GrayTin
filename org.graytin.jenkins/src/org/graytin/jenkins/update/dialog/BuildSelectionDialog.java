package org.graytin.jenkins.update.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.JenkinsUtils;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.jobs.RetrieveBuildsJob;

public class BuildSelectionDialog extends ElementListSelectionDialog {

	public enum Strategy {
		latest, current
	};

	public BuildSelectionDialog(Shell parent, ILabelProvider renderer, boolean allowMultipleSelection) {
		super(parent, new DecoratingLabelProvider(renderer, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));

		setMultipleSelection(allowMultipleSelection);
		setAllowDuplicates(false);
	}

	public void setElements(List<Build> elements, Strategy strategy) {
		if (elements.isEmpty()) {
			setElements(new Object[] { Build.current() });
			return;
		}

		setElements(elements.toArray());
		switch (strategy) {
			case current:
				Build current = Build.current();

				if (!elements.contains(current)) {
					List<Build> addedCurrentBuild = new ArrayList<Build>(elements);
					addedCurrentBuild.add(Build.current());
					setElements(addedCurrentBuild.toArray());
				}
				setInitialElementSelections(Collections.singletonList(current));
				break;

			case latest:
				setInitialElementSelections(elements.subList(0, 1));
				break;
		}

		setSelectionResult(getInitialElementSelections().toArray());

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		createButton(parent, IDialogConstants.RETRY_ID, Messages.BuildSelectionDialog_Refresh, false);

		createButton(parent, IDialogConstants.DETAILS_ID, Messages.BuildSelectionDialog_CheckConnection, false);

	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);

		if (IDialogConstants.RETRY_ID == buttonId) {
			refreshBuildPressed();
		} else if (IDialogConstants.DETAILS_ID == buttonId) {
			checkConnectionPressed();
		}
	};

	private void checkConnectionPressed() {
		IStatus connection = JenkinsUtils.checkConnection();
		if (connection.isOK()) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.BuildSelectionDialog_ConnectionOKTitle, Messages.BuildSelectionDialog_ConnectionOK);
		} else {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.BuildSelectionDialog_ConnectionErrorTitle, connection.getMessage());
		}
	}

	private void refreshBuildPressed() {
		invalidateCache();
		cancelPressed();
		MessageDialog.openInformation(getShell(), "Refresh","Please select this action again to see a refreshed content!");
	}

	private void invalidateCache() {
		String key = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.JOBNAME);
		RetrieveBuildsJob.markCacheAsInvalid(key);
	}

	public Build getSelectedBuild() {
		return (Build) getFirstResult();
	}

	public Build getSecondBuild() {
		return (Build) getResult()[1];
	}

}