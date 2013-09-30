package org.graytin.subversive.decorator;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.graytin.subversive.SubversiveSVNProviderImpl;
import org.graytin.subversive.commands.CompareBuilds.CompareResult;

/**
 * Decorate sdk bundle folder with the downloaded version.
 */
public class RepositoryResourceDecorator implements ILightweightLabelDecorator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, final IDecoration decoration) {
		IRepositoryResource resource = (IRepositoryResource) element;
		

		CompareResult compare = SubversiveSVNProviderImpl.getLastDiffBetweenBuilds();
		if (compare != null && compare.hasDifference() && compare.hasDetectedRemovalOfProject(resource)) {
			decoration.addPrefix(" X ");
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					decoration.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				}
			});
				decoration.addSuffix(" NA in "+compare.to.getLabel());
				decoration.addOverlay(ProjectDecorator.DEPRECATED, 1);
			return;
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