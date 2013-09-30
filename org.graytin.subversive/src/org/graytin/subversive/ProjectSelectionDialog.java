package org.graytin.subversive;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.ui.dialogs.AbstractElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.FilteredList.FilterMatcher;
import org.eclipse.ui.internal.misc.StringMatcher;

public class ProjectSelectionDialog extends AbstractElementListSelectionDialog implements IRepositoryResourceProvider {

	private Object[] fElements;

	private ILabelProvider renderer;

	/**
	 * Creates a list selection dialog.
	 * 
	 * @param parent
	 *            the parent widget.
	 * @param renderer
	 *            the label renderer.
	 */
	public ProjectSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
		this.renderer = renderer;
		setMultipleSelection(true);
		setAllowDuplicates(false);
	}

	private class MatchEveryWhereFilterMatcher implements FilterMatcher {
		private StringMatcher fMatcher;

		public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
			fMatcher = new StringMatcher('*'+pattern + '*', ignoreCase, ignoreWildCards);
		}

		public boolean match(Object element) {			
			return fMatcher.match(renderer.getText(element));
		}
	}

	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		setResult(Arrays.asList(getSelectedElements()));
	}

	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite contents = (Composite) super.createDialogArea(parent);

		createMessageArea(contents);
		createFilterText(contents);
		FilteredList createFilteredList = createFilteredList(contents);
		createFilteredList.setFilterMatcher(new MatchEveryWhereFilterMatcher());

		setListElements(fElements);

		setSelection(getInitialElementSelections().toArray());

		return contents;
	}

	/**
	 * Sets the elements of the list.
	 * 
	 * @param elements
	 *            the elements of the list.
	 */
	public void setElements(Object[] elements) {
		fElements = elements;
	}

	/**
	 * Selected resource repositories from this view
	 */
	@Override
	public IRepositoryResource[] getRepositoryResources() {
		return Arrays.asList(getResult()).toArray(new IRepositoryResource[getResult().length]);
	}
	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		
		createButton(parent, IDialogConstants.RETRY_ID,
				"Refresh", false);
		
	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		
		if (IDialogConstants.RETRY_ID == buttonId) {
			invalidateCache();
			cancelPressed();
			MessageDialog.openInformation(getShell(), "Refresh","Please select this action again to see a refreshed content!");
		}
	};
	

	private void invalidateCache() {
		SearchProjectOperation.markCacheAsInvalid();
	}
}