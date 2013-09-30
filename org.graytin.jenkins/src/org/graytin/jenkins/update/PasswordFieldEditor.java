package org.graytin.jenkins.update;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class PasswordFieldEditor extends StringFieldEditor {
	public PasswordFieldEditor(String name, String label, Composite parent) {
		super(name, label, parent);
	}

	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns);
		getTextControl().setEchoChar('*');
	}
}