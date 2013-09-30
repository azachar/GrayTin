package org.graytin.subversive.decorator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.jenkins.NABuild;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;
import org.graytin.subversive.Activator;
import org.graytin.subversive.SubversiveSVNProviderImpl;
import org.graytin.subversive.commands.MarkBranchAction;
import org.graytin.subversive.commands.CompareBuilds.CompareResult;

/**
 * Decorate sdk bundle folder with the downloaded version.
 */
public class ProjectDecorator implements ILightweightLabelDecorator {

	protected static final ImageDescriptor OK = Activator.getImageDescriptor("icons/icon_accept.png");

	protected static final ImageDescriptor BROKEN = Activator.getImageDescriptor("icons/action_stop.png");

	protected static final ImageDescriptor MODIFIED = Activator.getImageDescriptor("icons/flag_green.png");

	protected static final ImageDescriptor DEPRECATED = Activator.getImageDescriptor("icons/deprecated.png");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, final IDecoration decoration) {
		IFolder sdkBundlesFolder = SDKActivator.getSDKBundleFolder();

		IProject project = (IProject) element;
		if (sdkBundlesFolder.getProject().equals(project)) {
			return;
		}
		IPreferenceStore preferenceStore = SDKActivator.getDefault().getPreferenceStore();
		boolean useTextDecorators = preferenceStore.getBoolean(SDKBundleUpdatePreferencePage.USE_TEXT_DECORATION);
		boolean useImageDecorators = preferenceStore.getBoolean(SDKBundleUpdatePreferencePage.USE_IMAGE_DECORATION);

		Build build = Build.current();
		if (!useTextDecorators && !useImageDecorators || build instanceof NABuild) {
			return;
		}

		ILocalFolder local = (ILocalFolder) SVNRemoteStorage.instance().asLocalResource(project);
		if (IStateFilter.SF_VERSIONED.accept(local)) {

			IRepositoryResource markedBranch = MarkBranchAction.getMarkedBranch();
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(project);

			boolean sameRepo = remote.getRoot().getUrl().equals(markedBranch.getRoot().getUrl());
			if (!sameRepo) {
				if (useTextDecorators) {
					decoration.addPrefix("⦸ ");
				}
				return;
			}

			String url = remote.getUrl()+"/"; //ensure comparison with slash at the end
			boolean sameBranch = remote != null && url.startsWith(markedBranch.getUrl());
			if (!sameBranch) {
				if (useImageDecorators) {
					decoration.addOverlay(BROKEN, 1);
					decoration.addPrefix("⩚  ");
				}
				if (useTextDecorators) {
					decoration.addSuffix(" Mismatch branch '" + markedBranch.getName() + "' | ");
					
				}
			}

			CompareResult compare = SubversiveSVNProviderImpl.getLastDiffBetweenBuilds();
			if (compare != null && compare.hasDifference() && compare.hasDetectedRemovalOfProject(project)) {
				decoration.addPrefix("✘ ");
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						decoration.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					}
				});
				if (useTextDecorators) {
					decoration.addSuffix(" NA in " + compare.to.getLabel());
				}
				if (useImageDecorators) {
					decoration.addOverlay(DEPRECATED, 1);
				}
				return;
			}

			if (!sameBranch) {
				return;
			}

			long revision = local.getBaseRevision();
			if (build.getNumber().length() > 0) {
				Long buildRevision = build.getRevisionAsNumber();

				if (revision == buildRevision) {
					if (IStateFilter.SF_MODIFIED.accept(local)) {
						if (useTextDecorators) {
							decoration.addPrefix("* ");
						}
						if (useImageDecorators) {
							decoration.addOverlay(MODIFIED, 1);
						}
					} else {
						if (useTextDecorators) {
							decoration.addPrefix("≋ ");
						}
						if (useImageDecorators) {
							decoration.addOverlay(OK, 1);
						}
					}
				} else {
					if (useTextDecorators) {
						String result = "";
						if (revision < buildRevision) {
							result = "➘";
							if (useImageDecorators) {
								decoration.addOverlay(BROKEN, 1);
							}
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