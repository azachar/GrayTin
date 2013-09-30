package org.graytin.jenkins.jenkins;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.handlers.DownloadSDKBundles;
import org.graytin.jenkins.update.svn.ISVNProvider;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Represents Jenkins build including number and revision.
 * 
 */
public class Build implements IContentProposal, Comparable<Build> {

	public enum Status {
		SUCCESS, ABORTED, FAILURE, RUNNING, UNKNOWN, DOWNLOADING
	};

	DateTimeFormatterBuilder timeFormatter = new DateTimeFormatterBuilder()

	.appendDayOfWeekShortText()

	.appendLiteral(' ')

	.appendHourOfDay(1)

	.appendLiteral(':')

	.appendMinuteOfHour(2);

	PeriodFormatterBuilder periodFormatter = new PeriodFormatterBuilder().printZeroNever()

	.appendWeeks()

	.appendSuffix(" week", " weeks")

	.appendSeparator(", ")

	.appendDays()

	.appendSuffix(" d", " d")

	.appendSeparator(", ")

	.appendHours()

	.appendSuffix(" h", " h")

	.appendSeparator(", ")

	.appendMinutes()

	.appendSuffix(" m", " m")

	.appendSuffix(" ago")

	;

	private final String number;

	private final String url;

	private final String revision;

	private DateTime date;

	private String comment;

	private boolean isZipFileAvailableToDownload;

	private Status status = Status.UNKNOWN;

	private List<ChangeSet> changes = Collections.emptyList();

	public Build(String number, String url, String revision) {
		this.number = number;
		this.url = url;
		this.revision = revision;
	}

	public DateTime getDate() {
		return date;
	}

	public String getNumber() {
		return number;
	}

	public String getUrl() {
		return url;
	}

	public String getRevision() {
		return revision;
	}

	public String getFullDescription() {
		return number + " ( #" + revision + " ) " + (isZipFileAvailableToDownload ? "" : "No SDK-BUNDLES to download!") + " " + getComment() + " | "
				+ getDateAsString() + " | " + getChanges();
	}

	public String getDateAsString() {
		if (date != null) {
			return getDateAsSimple() + " - " + getDateAsComparism();
		}
		return "?";
	}

	public String getDateAsSimple() {
		if (date != null) {
			return date.toString(timeFormatter.toFormatter());
		}
		return "?";
	}

	public String getDateAsComparism() {
		if (date != null) {
			return new Period(date, new DateTime(), PeriodType.yearMonthDayTime()).toString(periodFormatter.toFormatter());
		}
		return "?";

	}

	@Override
	public String toString() {
		return getNumber();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Build other = (Build) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	@Override
	public String getContent() {
		return number;
	}

	@Override
	public int getCursorPosition() {
		return 0;
	}

	@Override
	public String getLabel() {
		return number + " - revision: " + revision;
	}

	@Override
	public String getDescription() {
		return url;
	}

	public String getIconType() {
		if (getStatus() == null) {
			return Status.UNKNOWN.toString();
		}

		if (Status.SUCCESS.equals(getStatus()) && !isZipFileAvailableToDownload) {
			return Status.UNKNOWN.toString();
		}

		return getStatus().toString();
	}

	public String getArchiveToDownloadURL() {
		return getUrl() + SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.SDK_ARTIFACT_TO_DOWNLOAD);
	}

	/**
	 * Return current downloaded build
	 */
	public static synchronized Build current() {
		// debug data
		//				return new Build("OLD", "", "90000");
		IPreferenceStore prefStore = SDKActivator.getDefault().getPreferenceStore();

		String build = prefStore.getString("buildNumber");
		String revision = prefStore.getString("buildRevision");
		long date = prefStore.getLong("buildDate");

//		if (StringUtils.isBlank(revision)) {
//			//try to guess at least revision from the check outed target platform
//			ISVNProvider svnProvider = SDKActivator.getDefault().getSvnProvider();
//
//			if (svnProvider != null) {
//				long resolveRevision = svnProvider.resolveRevision(SDKActivator.getSDKBundleFolder());
//				if (resolveRevision > 0) {
//					revision = resolveRevision + "";
//				} else {
//					revision = "HEAD";
//				}
//				
//			} else {
//				revision = "HEAD";
//			}
//
//		}
//		if (StringUtils.isBlank(build)) {
//			build = "?";
//		}
		if (StringUtils.isBlank(revision) || StringUtils.isBlank(build)) {
			return new NABuild();
		}
		Build b = new Build(build, "", revision);
		if (date > 0) {
			b.setDate(new DateTime(date));
		}
		return b;
	}

	/**
	 * Save current downloaded build
	 */
	public void save() {
		save(this);
		DownloadSDKBundles.updateProjectDecorators();
	}

	public synchronized static void save(Build build) {
		//debug data
		//				if (build.getNumber().equals("OLD")){
		//					return;
		//				}
		IPreferenceStore prefStore = SDKActivator.getDefault().getPreferenceStore();
		prefStore.setValue("buildNumber", build.getNumber());
		prefStore.setValue("buildRevision", build.getRevision());
		if (build.getDate() != null) {
			prefStore.setValue("buildDate", build.getDate().getMillis());
		}
		prefStore.needsSaving();
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public String getComment() {
		return comment == null ? "" : comment;
	}

	public void setComment(String info) {
		this.comment = info;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isZipFileAvailableToDownload() {
		return isZipFileAvailableToDownload;
	}

	public void setZipFileAvailableToDownload(boolean zipToDownloadAvailable) {
		this.isZipFileAvailableToDownload = zipToDownloadAvailable;
	}

	public Long getRevisionAsNumber() {
		try {
			Long rev = Long.parseLong(getRevision());
			return rev;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1L;
	}

	@Override
	public int compareTo(Build other) {
		return getRevisionAsNumber().compareTo(other.getRevisionAsNumber());
	}

	public List<ChangeSet> getChanges() {
		return changes;
	}

	public void setChanges(List<ChangeSet> changes) {
		this.changes = changes;
	}

}
