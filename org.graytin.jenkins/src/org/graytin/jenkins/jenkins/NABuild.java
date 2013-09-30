package org.graytin.jenkins.jenkins;

/**
 * To be able to reach also the latest source code we need to have an access to head revision using this class
 * 
 * @author azachar
 * 
 */
public class NABuild extends Build {

	public NABuild() {
		super("NA", "NA", "HEAD");
		setStatus(Status.UNKNOWN);
	}

	@Override
	public String toString() {
		return getLabel();
	}
	
	public Long getRevisionAsNumber() {
		return Long.MAX_VALUE;
	}

	@Override
	public String getLabel() {
		return "N/A -  Represents the head revision";
	}
}
