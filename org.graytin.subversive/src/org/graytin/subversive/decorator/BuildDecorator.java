package org.graytin.subversive.decorator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.NABuild;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.subversive.Activator;

/**
 * Decorate sdk bundle folder with the downloaded version.
 */
public class BuildDecorator implements ILightweightLabelDecorator {

	protected static final ImageDescriptor BROKEN = Activator.getImageDescriptor("icons/action_stop.png");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		IFolder sdkBundlesFolder = SDKActivator.getSDKBundleFolder();

		IProject project = (IProject) element;
		if (sdkBundlesFolder.getProject().equals(project)) {
			Build currentBuild = Build.current();
			if (currentBuild instanceof NABuild) {
				return;
			}
			if (currentBuild.getNumber().isEmpty()) {
				decoration.addSuffix(" (Unknown build)");
			} else {
				if (SDKActivator.isSDKStyle()) {
					decoration.addSuffix(" (SDK: " +"Build #" + currentBuild.getLabel() + ")");
				} else{
					decoration.addSuffix(" (NON-SDK: Build #" + currentBuild.getLabel() + ")");
				}
			}

			ILocalFolder local = (ILocalFolder) SVNRemoteStorage.instance().asLocalResource(project);
			if (IStateFilter.SF_VERSIONED.accept(local)) {
				long revision = local.getBaseRevision();
				if (currentBuild.getNumber().length() > 0) {
					Long buildRevision = currentBuild.getRevisionAsNumber();

					if (revision != buildRevision) {
						String result = "";
						if (revision < buildRevision) {
							result = "➘";
							decoration.addOverlay(BROKEN, 1);
						} else {
							result = "➚";
						}
						decoration.addPrefix(result + " ");
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}