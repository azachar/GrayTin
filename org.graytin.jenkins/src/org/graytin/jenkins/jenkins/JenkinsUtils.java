package org.graytin.jenkins.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.HttpClientUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JenkinsUtils {
	//For testing only
	//	public  String serverUrl = "http://deadlock.netbeans.org/hudson";

	public static final String SVN_SOURCE_URL_END = "/source";

	public String serverUrl;

	private String jobname;

	private int maximumBuilds;

	public JenkinsUtils() {
		loadPreferences();
	}

	private void loadPreferences() {
		IPreferenceStore prefStore = SDKActivator.getDefault().getPreferenceStore();
		jobname = prefStore.getString(SDKBundleUpdatePreferencePage.JOBNAME);
		serverUrl = prefStore.getString(SDKBundleUpdatePreferencePage.JENKINS);
		maximumBuilds = prefStore.getInt(SDKBundleUpdatePreferencePage.MAX_BUILDS_TO_PROCESS);
	}

	private String getRevisionForBuild(String buildUrl) {
		String jsonContent = getJsonFromUrl(buildUrl, "tree=changeSet[revisions[module,revision]");

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonContent);
			JSONObject changeSet = jsonObject.getJSONObject("changeSet");

			JSONArray revs = changeSet.getJSONArray("revisions");
			for (int i = 0; i < revs.length(); i++) {
				JSONObject element = revs.getJSONObject(i);
				String module = element.getString("module");
				if (StringUtils.isNotBlank(module) && module.endsWith(SVN_SOURCE_URL_END)) {
					return String.valueOf(element.get("revision"));
				}
			}
		} catch (JSONException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		return "HEAD";
	}

	private List<ChangeSet> getChanges(String buildUrl) {
		List<ChangeSet> result = new ArrayList<ChangeSet>();

		String jsonContent = getJsonFromUrl(buildUrl, "tree=changeSet[items[msg,user]");

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonContent);
			JSONObject changeSet = jsonObject.getJSONObject("changeSet");

			JSONArray items = changeSet.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				JSONObject element = items.getJSONObject(i);
				String msg = element.getString("msg");
				String user = element.getString("user");
				if (StringUtils.isNotBlank(msg)) {
					result.add(new ChangeSet(msg, user));
				}
			}
		} catch (JSONException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		return result;
	}

	private void parseAdditionalInformation(Build build) {
		try {
			String jsonContent = getJsonFromUrl(build.getUrl(), "tree=result,building,description");
			JSONObject jsonObject = new JSONObject(jsonContent);
			try {
				build.setStatus(parseStatus(jsonObject.getString("result")));
			} catch (Exception e) {
			}

			try {
				String description = jsonObject.getString("description");
				build.setComment(description);
			} catch (Exception e) {
			}

			try {
				boolean building = jsonObject.getBoolean("building");
				if (building) {
					build.setStatus(Build.Status.RUNNING);
				}
			} catch (Exception e) {
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Build.Status parseStatus(String result) {
		String lowerCase = result.toLowerCase();
		if (lowerCase.contains("success")) {
			return Build.Status.SUCCESS;
		}
		if (lowerCase.contains("failure")) {
			return Build.Status.FAILURE;
		}
		if (lowerCase.contains("progress")) {
			return Build.Status.RUNNING;
		}
		if (lowerCase.contains("aborted")) {
			return Build.Status.ABORTED;
		}
		return Build.Status.UNKNOWN;
	}

	/**
	 * Retrieve JSON from given URL
	 * 
	 * @param url
	 * @return JSON content of given URL, empty String if it failed.
	 */
	private String getJsonFromUrl(String urlWithoutApiJSON, String params) {
		String url = urlWithoutApiJSON + "/api/json";
		return getContentFromURL(url);
	}

	private String getContentFromURL(String url) {
		System.out.println("Reading JSON from: " + url);
		try {
			return getExecutor().execute(Request.Get(url)).returnContent().asString();
		} catch (ClientProtocolException e) {
			throw new RuntimeException("Error accessing url='" + url + "', " + e.getLocalizedMessage());
		} catch (IOException e) {
			throw new RuntimeException("Error accessing url='" + url + "', " + e.getLocalizedMessage());
		} catch (HttpException e) {
			throw new RuntimeException("Error accessing url='" + url + "', " + e.getLocalizedMessage());
		}
	}

	private String getJobUrl() {
		String jenkinsUrl = serverUrl + "/job/" + getEncodedJobname();
		return jenkinsUrl;
	}

	public String getEncodedJobname() {
		return jobname.replace(" ", "%20");
	}

	/**
	 * 
	 * @return true when all 'crucial' preferences are filled in, false otherwise
	 */
	public boolean validateMandatoryFields() {
		if (StringUtils.isBlank(jobname) || StringUtils.isBlank(serverUrl)) {
			return false;
		}
		return true;
	}

	public static IStatus checkConnection() {
		JenkinsUtils jenkins = new JenkinsUtils();

		if (jenkins.validateMandatoryFields() == false) {
			return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, "Setup preferences first, required fields are missing!");
		}

		HttpResponse response = null;
		try {
			response = jenkins.getExecutor().execute(Request.Get(jenkins.serverUrl)).returnResponse();
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				System.err.println("Unable to connect to "+jenkins.serverUrl+" due to "+ response);
				return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, "Authentification failed [" + response.getStatusLine()+"]");
			}
			if (!jenkins.isAvailable(jenkins.getJobUrl())) {
				////			List<Build> builds = jenkins.getBuilds(1);
				//			if (builds.isEmpty()) {
				return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, "Authentification is OK, but the job name '" + jenkins.jobname + "' is invalid!");
			}

		} catch (Exception e) {
			return new Status(Status.ERROR, SDKActivator.PLUGIN_ID, e.toString());
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Retrieves the highest revision number of lastSuccesful build in Jenkins
	 * 
	 * @param monitor
	 * 
	 * @return revision number if found, empty String otherwise
	 * @throws JSONException
	 */
	public List<Build> getBuilds(IProgressMonitor monitor) {
		return getBuilds(monitor, maximumBuilds);
	}

	private DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yy h:mm a");

	private List<Build> getBuilds(IProgressMonitor monitor, int max) {
		List<Build> buildNumbers = new ArrayList<Build>();
		String jsonContent = getJsonFromUrl(getJobUrl(), "tree=builds[number,url]");

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonContent);
			JSONArray builds = jsonObject.getJSONArray("builds");
			monitor.beginTask("Obtaining " + builds.length() + " build(s) from " + getJobUrl(), builds.length());
			for (int i = 0; i < builds.length() && buildNumbers.size() < max && i < 3 * max; i++) {

				if (monitor.isCanceled()) {
					return buildNumbers;
				}

				JSONObject build = builds.getJSONObject(i);
				String number = build.getInt("number") + "";
				String url = build.getString("url");
				monitor.subTask("Obtaining sources revision from the build #" + build);
				String revision = getRevisionForBuild(url);

				List<ChangeSet> changes = getChanges(url);
				Build buildInstance = new Build(number, url, revision);
				buildInstance.setChanges(changes);

				monitor.subTask("Obtaining date of build #" + number);
				String date = getContentFromURL(url + "/buildTimestamp");
				DateTime dateTime = fmt.parseDateTime(date);
				buildInstance.setDate(dateTime);

				monitor.subTask("Obtaining additional info for the build #" + number);
				parseAdditionalInformation(buildInstance);
				buildInstance.setZipFileAvailableToDownload(isAvailable(buildInstance.getArchiveToDownloadURL()));

				buildNumbers.add(buildInstance);

				monitor.worked(1);
			}
			return buildNumbers;
		} catch (JSONException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		return Collections.emptyList();
	}

	private boolean isAvailable(String archiveToDownloadURL) {
		HttpResponse status = null;
		try {
			status = getExecutor().execute(Request.Head(archiveToDownloadURL)).returnResponse();
			System.out.println(archiveToDownloadURL + "=" + status.getStatusLine());
			return status.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND;
		} catch (Exception e) {
			return false;
		} finally {
			HttpClientUtils.closeQuietly(status);
		}
	}

	public Executor getExecutor() throws IOException, HttpException {
		return RemoteClient.getInstance().createAuthentificatedExecutor();
	}

	public List<Job> getJobs() {
		List<Job> result = new ArrayList<Job>();
		String jsonContent = getJsonFromUrl(serverUrl, "tree=jobs[name,url,color]");

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonContent);
			JSONArray builds = jsonObject.getJSONArray("jobs");
			for (int i = 0; i < builds.length(); i++) {
				JSONObject job = builds.getJSONObject(i);
				String name = job.getString("name");
				String url = job.getString("url");
				String color = job.getString("color");
				result.add(new Job(name, url, color));
			}
			return result;
		} catch (JSONException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		return Collections.emptyList();
	}

}
