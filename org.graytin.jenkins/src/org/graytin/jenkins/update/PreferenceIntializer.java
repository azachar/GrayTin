package org.graytin.jenkins.update;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceIntializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = SDKActivator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.SDK_BUNDLES_FOLDER, "target-platform/sdk-bundles");
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.JENKINS,"http://your.jenkins.com");
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.JOBNAME, "<your-job-name>");
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.MAX_BUILDS_TO_PROCESS,  10);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.DO_UPDATE_TO_REVISION_AUTOMATICALLY,  true);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.DO_DELETE_AUTOMATICALLY,  false);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.USE_IMAGE_DECORATION,  true);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.USE_TEXT_DECORATION,  true);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_PERIODICALLY,  true);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_EVERY,  15);
		preferenceStore.setDefault(SDKBundleUpdatePreferencePage.DEPTH_OF_SVN_SCAN,  1);
	}

}
