package org.graytin.jenkins.update.dialog;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.graytin.jenkins.jenkins.Build;
import org.graytin.jenkins.update.SDKActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildLabelProvider extends LabelProvider {
	ImageRegistry registry = new ImageRegistry();

	private static final Logger logger = LoggerFactory.getLogger(BuildLabelProvider.class);

	@Override
	public String getText(Object element) {
		if (element instanceof Build) {
			Build build = (Build) element;
			return build.getFullDescription();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Build) {
			Build build = (Build) element;

			String file = build.getIconType();
			return getImageFor(file.toLowerCase());
		}
		if (element instanceof String) {
			return getImageFor((String) element);
		}
		return super.getImage(element);
	}

	protected Image getImageFor(String filename) {
		String file = "/icons/" + filename;
		Image image = registry.get(file);
		if (image != null) {
			return image;
		}
		ImageDescriptor imageDescriptor = createImageDescriptor(file);
		if (imageDescriptor != null) {
			image = imageDescriptor.createImage();

			if (image != null) {
				registry.put(file, image);
				return image;
			}
		}
		logger.error("Unable to find an image for the file {} ", file);
		return null;
	}

	public ImageDescriptor createImageDescriptor(String file) {
		ImageDescriptor imageDescriptor = SDKActivator.getImageDescriptor(file + ".png");
		if (imageDescriptor == null || imageDescriptor.equals(ImageDescriptor.getMissingImageDescriptor())) {
			imageDescriptor = SDKActivator.getImageDescriptor(file + ".gif");
		}
		return imageDescriptor;
	}

	/*
	 * (non-Javadoc) Method declared on LabelProvider.
	 */
	public void dispose() {
		registry.dispose();
	}
}