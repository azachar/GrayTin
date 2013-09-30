package org.graytin.jenkins.update.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.ChangeSet;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;
import org.graytin.jenkins.update.handlers.DownloadSDKBundles;

public class CheckJenkinsPeriodicallyJob extends Job {

	private final class BuildNotification extends AbstractUiNotification {
		private final List<Build> listOfNewBuilds;

		private final BuildLabelProvider labelProvider = new BuildLabelProvider();

		private final String difference;

		private BuildNotification(List<Build> listOfNewBuilds, String difference) {
			super("org.graytin.jenkins.newBuilds");
			this.listOfNewBuilds = listOfNewBuilds;
			this.difference = difference;
		}

		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}

		@Override
		public Image getNotificationImage() {
			return labelProvider.getImage(listOfNewBuilds.get(0));
		}

		@Override
		public Image getNotificationKindImage() {
			return labelProvider.getImage("jenkins");
		}

		@Override
		public void open() {
			DownloadSDKBundles d = new DownloadSDKBundles();
			try {
				d.execute(new ExecutionEvent());
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		@Override
		public Date getDate() {
			return listOfNewBuilds.get(0).getDate().toDate();
		}

		@Override
		public String getLabel() {
			return listOfNewBuilds.size() + " new build(s) available.\nThe latest is " + listOfNewBuilds.get(0);
		}

		final int LIMIT_TEXT = 60;

		final int LIMIT_BUILDS = 3;

		@Override
		public String getDescription() {

			StringBuilder b = new StringBuilder();
			int i = 0;
			for (Build build : listOfNewBuilds) {
				if (i > LIMIT_BUILDS) {
					break;
				}
				if (build.isZipFileAvailableToDownload()) {
					b.append(build.getDateAsSimple());
					b.append(" - ");
					b.append(build.getNumber());
					b.append(" - ");
					b.append(build.getStatus());
					b.append("  ");
					b.append(StringUtils.abbreviate(build.getComment(), LIMIT_TEXT));
					b.append("  ");
					b.append("\n  ");

					
					//TODO better change report
					List<ChangeSet> changes = build.getChanges();

					Set<String> setOfUsers = new HashSet<String>();
					for (ChangeSet change : changes) {
						setOfUsers.add(change.getUser());
					}
					boolean isSecondOrMore = false;
					for (String user : setOfUsers) {
						if (isSecondOrMore) {
							b.append(", ");
							isSecondOrMore = true;
						}
						b.append(user);
					}
					b.append("\n  Changes:\n");
					for (ChangeSet change : changes) {
						b.append("   ");
						b.append(StringUtils.abbreviate(change.getMessage(), LIMIT_TEXT));
					}
					b.append("\n\n");
					i++;
				}
			}
			if (listOfNewBuilds.size()>LIMIT_BUILDS){
				b.append("and ");
				b.append(listOfNewBuilds.size()-LIMIT_BUILDS);
				b.append(" other new build(s).");
			}
			return b.toString() + "\n" + difference;
		}
	}

	final RetrieveBuildsJob retrieve = new RetrieveBuildsJob(false);

	private static List<Build> previousBuilds = null;

	public CheckJenkinsPeriodicallyJob() {
		super("Periodical check of builds");
		setSystem(false);
	}

	private void invalidateCache() {
		String key = SDKActivator.getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.JOBNAME);
		RetrieveBuildsJob.markCacheAsInvalid(key);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		invalidateCache();
		monitor.beginTask("", 2);

		IStatus status = retrieve.run(new SubProgressMonitor(monitor, 1));
		final List<Build> builds = new ArrayList<Build>(retrieve.getBuilds());
		if (previousBuilds == null) {
			//first time we do not report builds only differences
			previousBuilds = new ArrayList<Build>(builds);
			final Build currentBuild = Build.current();
			for (Build build : builds) {
				//show only newer builds than the current one
				if (build.compareTo(currentBuild) > 0) {
					previousBuilds.remove(build);
				}
			}
		}
		final List<Build> newAdded = new ArrayList<Build>(builds);
		newAdded.removeAll(previousBuilds);
		previousBuilds = new ArrayList<Build>(builds);

		if (!newAdded.isEmpty() && !monitor.isCanceled()) {
			final String difference = SDKActivator.getDefault().getSvnProvider()
					.getProjectDifference(Build.current(), newAdded.get(0), new SubProgressMonitor(monitor, 1));

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					BuildNotification notification = new BuildNotification(newAdded, difference);
					NotificationsUi.getService().notify(Collections.singletonList(notification));
				}
			});
		}

		if (SDKActivator.getDefault().getPreferenceStore().getBoolean(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_PERIODICALLY)) {
			int minutes = SDKActivator.getDefault().getPreferenceStore().getInt(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_EVERY);
			if (minutes > 0) {
				if (Thread.interrupted()) {
					return Status.CANCEL_STATUS;
				}
				schedule(minutes * 60 * 1000);
			}
		}
		return status;
	}

	public void restart() {
		previousBuilds = null;
		schedule();
	}
}