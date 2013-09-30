package org.graytin.subversive;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.graytin.subversive.messages"; //$NON-NLS-1$

	public static String CheckoutAsWizardHacked_DeleteDuplicatedBundlesOperation;

	public static String CheckoutByBuildAction_BuildSelectionDialogTitle;

	public static String CheckoutByBuildAction_ProjectSelectionDialogTitle;
	public static String CheckoutByBuildAction_SVNIssueMessage;

	public static String CheckoutByBuildAction_SVNIssueTitle;

	public static String CompareBuilds_ActionIsNotAvailableTitle;

	public static String CompareBuilds_ActionIsNotAvailableWarning;

	public static String CompareBuilds_Added;

	public static String CompareBuilds_CompareResult;

	public static String CompareBuilds_CompareResultsDialogTitle;

	public static String CompareBuilds_DialogTitle;

	public static String CompareBuilds_ProjectSelectionDialog;

	public static String CompareBuilds_Removed;

	public static String CompareBuilds_SizeSummary;

	public static String CompareBuilds_Summary;

	public static String CompareBuilds_TaskName;
	public static String MarkBranchAction_BranchSelected;

	public static String MarkBranchAction_BranchSelectedDialogTitle;

	public static String SearchProjects_ActionIsNotAvailable;

	public static String SearchProjects_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
