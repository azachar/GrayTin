package org.graytin.subversive;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;
import org.graytin.jenkins.update.dialog.BuildLabelProvider;

public class RepoLabelProvider extends BuildLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IRepositoryResource) {
			IRepositoryResource resource = (IRepositoryResource) element;
			return resource.getParent().getName() + " - " + resource.getName() 
//					+ "    ("+resource.getUrl()+")"
					;
		}
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof IRepositoryResource) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_FOLDER);
		}
		return super.getImage(element);
	}

}
