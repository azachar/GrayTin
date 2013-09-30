package org.graytin.jenkins.jenkins;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;

public class RemoteClient {

	private static RemoteClient instance;

	public static synchronized RemoteClient getInstance() {
		if (instance == null) {
			instance = new RemoteClient();
		}
		return instance;
	}

	private RemoteClient() {
	}

	public Executor createAuthentificatedExecutor() throws IOException, HttpException {
		IPreferenceStore prefStore = SDKActivator.getDefault().getPreferenceStore();

		String username = prefStore.getString(SDKBundleUpdatePreferencePage.USERNAME);
		String password = prefStore.getString(SDKBundleUpdatePreferencePage.PASSWORD);
		String serverUrl = prefStore.getString(SDKBundleUpdatePreferencePage.JENKINS);

		return Executor.newInstance()

		.auth(new HttpHost(serverUrl), username, password)

		.authPreemptive(new HttpHost(serverUrl));
	}
}
