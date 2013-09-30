package org.graytin.jenkins.update.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.graytin.jenkins.update.dialog.messages"; //$NON-NLS-1$

	public static String BaseHandler_FolderIsNotAccessibleSecondTimeTitle;

	public static String BaseHandler_FolderIsNotAccessibleCreateTitle;

	public static String BaseHandler_SuiteName;

	public static String BuildSelectionDialog_CheckConnection;

	public static String BuildSelectionDialog_ConnectionErrorTitle;

	public static String BuildSelectionDialog_ConnectionOK;

	public static String BuildSelectionDialog_ConnectionOKTitle;

	public static String BuildSelectionDialog_Refresh;

	public static String UpdateLaunchConfiguration_1;

	public static String UpdateLaunchConfiguration_10;

	public static String UpdateLaunchConfiguration_11;

	public static String UpdateLaunchConfiguration_13;

	public static String UpdateLaunchConfiguration_19;

	public static String UpdateLaunchConfiguration_2;

	public static String UpdateLaunchConfiguration_20;

	public static String UpdateLaunchConfiguration_21;

	public static String UpdateLaunchConfiguration_22;

	public static String UpdateLaunchConfiguration_9;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
