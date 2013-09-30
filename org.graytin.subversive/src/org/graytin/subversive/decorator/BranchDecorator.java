package org.graytin.subversive.decorator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.graytin.subversive.Activator;
import org.graytin.subversive.commands.MarkBranchAction;

/**
 * Decorate sdk bundle folder with the downloaded version.
 */
public class BranchDecorator implements ILightweightLabelDecorator {

	protected static final ImageDescriptor FLAG = Activator.getImageDescriptor("icons/flag_green.gif");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof RepositoryFolder) {
			RepositoryFolder resource = (RepositoryFolder) element;

			IRepositoryResource[] lastSelectedResources = MarkBranchAction.getMarkedBranchAsArray();
			for (IRepositoryResource iRepositoryResource : lastSelectedResources) {
				if (iRepositoryResource.getUrl().equals(resource.getRepositoryResource().getUrl())) {
					decoration.addOverlay(FLAG);
					break;
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