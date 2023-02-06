package org.springframework.richclient.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.text.JTextComponent;

/**
 * A simple FocusListener that selects all the contents of a JTextComponent upon
 * entering the component.
 *
 * @author Jan Hoskens
 */
public class SelectAllFocusListener implements FocusListener {

	private final JTextComponent textComponent;

	/**
	 * Create a SelectAllFocusListener to select all text upon entering the given
	 * JTextComponent.
	 *
	 * @param textComponent the JTextComponent that needs the select all upon
	 *                      entering.
	 */
	public SelectAllFocusListener(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}

	/**
	 * Select all text upon gaining focus.
	 */
	@Override
	public void focusGained(FocusEvent e) {
		textComponent.selectAll();
	}

	/**
	 * Remove selection when focus is lost.
	 */
	@Override
	public void focusLost(FocusEvent e) {
		textComponent.select(0, 0);
	}
}
