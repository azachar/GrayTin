package org.graytin.subversive;

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.graytin.jenkins.jenkins.Build;


public class SVNUtil {

	public static SVNRevision getRevision(Build selectedBuild) {
		return SVNRevision.fromString(selectedBuild.getRevision());
	}

}
