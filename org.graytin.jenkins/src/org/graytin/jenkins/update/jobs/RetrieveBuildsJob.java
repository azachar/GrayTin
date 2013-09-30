package org.graytin.jenkins.update.jobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.JenkinsUtils;
import org.graytin.jenkins.jenkins.NABuild;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public final class RetrieveBuildsJob extends org.eclipse.core.runtime.jobs.Job {

	private boolean includeNABuild;

	private static volatile LoadingCache<String, List<Build>> cache;

	private static volatile String invalidateKey;

	public RetrieveBuildsJob(boolean includeNABuild) {
		super("Retrieving build's information");
		this.includeNABuild = includeNABuild;
	}

	@Override
	public IStatus run(final IProgressMonitor monitor) {
		String jobName = null;
		try {
			jobName = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.JOBNAME);
		} catch (Exception e1) {
			return Status.CANCEL_STATUS;
		}

		setName("Obtaining a list of builds from '" + jobName + "'");

		try {
			if (cache == null) {
				cache = CacheBuilder.newBuilder()

				.maximumSize(50)

				.expireAfterAccess(15, TimeUnit.MINUTES)

				.build(new CacheLoader<String, List<Build>>() {
					@Override
					public List<Build> load(String key) throws Exception {
						return downloadBuilds(monitor);
					}
				});
			}
			if (invalidateKey != null) {
				cache.invalidate(invalidateKey);
				invalidateKey = null;
			}
			String key = jobName;
			cache.get(key);
		} catch (ExecutionException e) {
			return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, e.getLocalizedMessage());
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	public List<Build> downloadBuilds(IProgressMonitor monitor) {
		return new JenkinsUtils().getBuilds(monitor);
	}

	public List<Build> getBuilds() {
		String key = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.JOBNAME);

		List<Build> result = new ArrayList<Build>();
		try {
			if (cache != null) {
				List<Build> list = cache.get(key);
				if (list != null) {
					result.addAll(list);
				} else {
					//when it returns null try to obtain it again
					cache.invalidate(key);
				}
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (includeNABuild) {
			result.add(new NABuild());
		}
		return result;
	}

	public static void markCacheAsInvalid(String key) {
		invalidateKey = key;
	}
}