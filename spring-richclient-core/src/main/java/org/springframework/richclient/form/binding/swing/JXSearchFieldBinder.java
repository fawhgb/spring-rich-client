package org.springframework.richclient.form.binding.swing;

import javax.swing.text.JTextComponent;

import org.jdesktop.xswingx.JXSearchField;

/**
 * Binder that shows a JXSearchField instead of a simple JTextField
 *
 * @author Lieven Doclo
 */
public class JXSearchFieldBinder extends TextComponentBinder {
	protected JXSearchFieldBinder() {
		super();
		setSelectAllOnFocus(true);
	}

	@Override
	protected JTextComponent createTextComponent() {
		return new JXSearchField();
	}
}
