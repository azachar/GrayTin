package org.graytin.jenkins.update;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.graytin.jenkins.jenkins.JenkinsUtils;
import org.graytin.jenkins.jenkins.Job;
import org.graytin.jenkins.jenkins.ProposalUtil;
import org.graytin.jenkins.update.handlers.DownloadSDKBundles;

public class SDKBundleUpdatePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String USERNAME = "username";

	public static final String PASSWORD = "password";

	public static final String JOBNAME = "jobname";

	public static final String SVN_RELEASE_URL = "svnreleaseurl";

	public static final String SDK_BUNDLES_FOLDER = "sdkBundlesFolder";

	public static final String MAX_BUILDS_TO_PROCESS = "maxBuildToProcess";

	public static final String JENKINS = "jenkinsServerUrl";

	public static final String DO_UPDATE_TO_REVISION_AUTOMATICALLY = "doUpdateToRevisionAutomatically";

	public static final String USE_IMAGE_DECORATION = "useImageDecoration";

	public static final String USE_TEXT_DECORATION = "useTextDecorations";

	public static final String DO_SCAN_BUILDS_EVERY = "doScanEvery";

	public static final String DO_SCAN_BUILDS_PERIODICALLY = "doScan";

	public static final String DEPTH_OF_SVN_SCAN = "svnDepth";

	public static final String DO_DELETE_AUTOMATICALLY = "doDeleteAutomatically";

	public static final String BRANCH_URL = "branchUrl";

	public static final String SDK_TARGET_PLATFORM_FILE = "targetPlatformFile";

	public static final String SDK_ARTIFACT_TO_DOWNLOAD = "artifactToDownload";

	private StringFieldEditor username;

	private StringFieldEditor jobname;

	public SDKBundleUpdatePreferencePage() {
		super(GRID);
	}

	class JobPicker extends StringButtonFieldEditor implements IContentProposalProvider {
		public JobPicker(String jobname, String string, Composite fieldEditorParent) {
			super(jobname, string, fieldEditorParent);
			setChangeButtonText("reset");
		}

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			JenkinsUtils jenkinsUtils = new JenkinsUtils();
			try {
				List<Job> proposals = jenkinsUtils.getJobs();
				return ProposalUtil.filterProposals(contents, proposals);
			} catch (Exception e) {
				return new IContentProposal[] {};
			}
		}

		@Override
		protected void createControl(Composite parent) {
			super.createControl(parent);

			new ContentAssistCommandAdapter(getTextControl(), new TextContentAdapter(), this, null, null, true);
		}

		@Override
		protected String changePressed() {
			return getPreferenceStore().getDefaultString(JOBNAME);
		}

	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(JENKINS, "* Jenkins Server URL", getFieldEditorParent()));
		username = new StringFieldEditor(USERNAME, "* Jenkins Username", getFieldEditorParent());
		username.setEmptyStringAllowed(false);
		addField(username);

		addField(new PasswordFieldEditor(PASSWORD, "* Jenkins Password", getFieldEditorParent()));

		jobname = new JobPicker(JOBNAME, "* Jenkins Job Name", getFieldEditorParent());
		addField(jobname);

		addField(new StringFieldEditor(BRANCH_URL, "* Branch URL", getFieldEditorParent()));
		addField(new StringFieldEditor(SDK_BUNDLES_FOLDER, "* SDK-Bundles Folder", getFieldEditorParent()));
		addField(new StringFieldEditor(SDK_TARGET_PLATFORM_FILE, "* Target Platform Definition File", getFieldEditorParent()));
		addField(new StringFieldEditor(SDK_ARTIFACT_TO_DOWNLOAD, "* Zip artifact to download to SDK folder:", getFieldEditorParent()));
		IntegerFieldEditor numberOfBuilds = new IntegerFieldEditor(MAX_BUILDS_TO_PROCESS, "Limit number of builds to choose", getFieldEditorParent());
		numberOfBuilds.setValidRange(1, 100);
		addField(numberOfBuilds);
		addField(new StringFieldEditor(SVN_RELEASE_URL, "SVN release tag url", getFieldEditorParent()));
		addField(new BooleanFieldEditor(DO_UPDATE_TO_REVISION_AUTOMATICALLY, "Automatically update all workspace projects's to downloaded build's revision",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(DO_DELETE_AUTOMATICALLY, "Automatically delete duplicated binaries after checkout svn projects", getFieldEditorParent()));
		addField(new BooleanFieldEditor(USE_IMAGE_DECORATION, "Use image decoration (Except a target-platform project)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(USE_TEXT_DECORATION, "Use text decoration on source project (Except a target platform project)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(DO_SCAN_BUILDS_PERIODICALLY, "Obtain builds on background", getFieldEditorParent()));
		IntegerFieldEditor updateBuildsEvery = new IntegerFieldEditor(DO_SCAN_BUILDS_EVERY, "Obtain builds on background every (min)", getFieldEditorParent());
		updateBuildsEvery.setValidRange(1, 3600);
		addField(updateBuildsEvery);

		IntegerFieldEditor depth = new IntegerFieldEditor(DEPTH_OF_SVN_SCAN, "The scanning depth of nested SVN folders", getFieldEditorParent());
		depth.setValidRange(0, 99);
		addField(depth);
	}

	@Override
	protected void performApply() {
		super.performApply(); //save values first so checkConnection can use the new settings.

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IStatus checkConnection = JenkinsUtils.checkConnection();
					if (getControl().isDisposed()) {
						return;
					}
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							try {
								if (checkConnection.isOK()) {
									setMessage("The connection is ok!");
								} else {
									setMessage("");
									setErrorMessage("Unable to connect to your Jenkins server! (" + checkConnection.getMessage() + ")");
								}
							} catch (Exception e) {
								//ignore
							}

						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean performOk() {
		DownloadSDKBundles.updateProjectDecorators();
		return super.performOk();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(SDKActivator.getDefault().getPreferenceStore());
		getPreferenceStore().setDefault(SDK_BUNDLES_FOLDER, SDK_BUNDLES_FOLDER);
		setDescription("GrayTin SDK Tooling Preferences - (Press the Apply button to check the connection to your Jenkins server)");
	}

}
