package org.graytin.jenkins.update.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

/**
 * compare for arbitrary IResources.
 */
public class StringCompare extends CompareEditorInput {

	private final String left;

	private final String right;

	private Object fRoot;

	public StringCompare(CompareConfiguration configuration, String left, String right) {
		super(configuration);
		this.left = left;
		this.right = right;

	}


	@Override
	protected Object prepareInput(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
		try {
			// fix for PR 1GFMLFB: ITPUI:WIN2000 - files that are out of sync with the file system appear as empty							
			pm.beginTask(Utilities.getString("ResourceCompare.taskName"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

			String leftLabel = "Before ";
			String rightLabel = "After ";

			String format = Utilities.getString("ResourceCompare.twoWay.title"); //$NON-NLS-1$
			String title = MessageFormat.format(format, new String[] { leftLabel, rightLabel });
			setTitle(title);

			Differencer d = new Differencer();

			fRoot = d.findDifferences(false, pm, null, null, new CompareItem(left), new CompareItem(right));
			return fRoot;
		} finally {
			pm.done();
		}
	}

	class CompareItem implements IStreamContentAccessor, ITypedElement, IModificationDate {

		private String result;

		CompareItem(String result) {
			this.result = result;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(result.getBytes());
		}

		public String getType() {
			return ITypedElement.TEXT_TYPE;
		}

		@Override
		public long getModificationDate() {
			return new Date().getTime();
		}

		@Override
		public String getName() {
			return result.toString();
		}

		@Override
		public Image getImage() {
			return null;
		}
	}

}
