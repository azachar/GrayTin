package org.graytin.jenkins.jenkins;

/**
 * When we download a build, this instance if used in UI
 * 
 * @author azachar
 * 
 */
public class InProgressBuild extends Build {

	public InProgressBuild(Build buildToDownload) {
		super(buildToDownload.getNumber() + " in progress..", buildToDownload.getUrl(), buildToDownload.getRevision());
		setStatus(Status.DOWNLOADING);
	}
}
