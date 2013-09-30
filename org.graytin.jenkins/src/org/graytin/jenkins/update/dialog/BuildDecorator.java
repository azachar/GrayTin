package org.graytin.jenkins.update.dialog;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.SDKActivator;

/**
 * Decorate sdk bundle folder with the downloaded version.
 */
public class BuildDecorator implements ILightweightLabelDecorator {

	protected static final ImageDescriptor OK = SDKActivator.getImageDescriptor("icons/icon_accept.png");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, final IDecoration decoration) {
//		Build build = (Build) element;
//		if (build.equals(Build.current())) {
////			Display.getDefault().syncExec(new Runnable() {
////				@Override
////				public void run() {
////				}
////			});
//
//			decoration.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
//			decoration.addOverlay(OK, 1);
//		}
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