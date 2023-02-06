package org.springframework.richclient.components;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * A component that shows a filesystem view and in which the user can either
 * choose a file or a directory, depending on the mode set (standard is file).
 */
public class FileChooser extends JComponent {
	private static final long serialVersionUID = 1L;

	private JTextField nameField;

	private JButton openDialogButton;

	private int openDialogButtonWidth = 20;

	private FileChooserMode mode = FileChooserMode.FILE;

	public FileChooser() {
		nameField = new JTextField();
		/*
		 * nameField.addFocusListener(new FocusAdapter() { public void
		 * focusGained(FocusEvent e) { nameField.selectAll(); } });
		 */
		Handler handler = new Handler();
		openDialogButton = new JButton("...");
		openDialogButton.setName("openDialogButton");
		openDialogButton.setRolloverEnabled(false);
		openDialogButton.setFocusable(false);
		openDialogButton.addMouseListener(handler);

		add(nameField);
		add(openDialogButton);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLayout() {
		int width = getWidth();
		int height = getHeight();

		Insets insets = getInsets();
		nameField.setBounds(insets.left, insets.bottom, width - 3 - openDialogButtonWidth, height);
		openDialogButton.setBounds(width - openDialogButtonWidth + insets.left, insets.bottom, openDialogButtonWidth,
				height);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = nameField.getPreferredSize();
		dim.width += openDialogButton.getPreferredSize().width;
		Insets insets = getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;
		return dim;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(FileChooserMode mode) {
		this.mode = mode;
	}

	/**
	 * @return the mode
	 */
	public FileChooserMode getMode() {
		return mode;
	}

	/**
	 * Set the text of the file textfield
	 *
	 * @param text
	 */
	public void setText(String text) {
		nameField.setText(text);
	}

	/**
	 * Get the text of the file textfield
	 */
	public String getText() {
		return nameField.getText();
	}

	public void setEditable(boolean editable) {
		if (editable) {
			nameField.setEditable(true);
			openDialogButton.setEnabled(true);
		} else {
			nameField.setEditable(false);
			openDialogButton.setEnabled(false);
		}
	}

	/**
	 * Gets whether the control is editable or not
	 */
	public boolean isEditable() {
		return nameField.isEditable();
	}

	/**
	 * Set whether the control is enabled or not
	 *
	 * @param enabled Whether the control is enabled or not
	 */
	@Override
	public void setEnabled(boolean enabled) {
		nameField.setEnabled(enabled);
		openDialogButton.setEnabled(enabled);
	}

	/**
	 * Gets whether the control is enabled or not
	 */
	@Override
	public boolean isEnabled() {
		return nameField.isEnabled() & openDialogButton.isEnabled();
	}

	private class Handler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent ev) {
			if (isEnabled()) {
				JFileChooser chooser = new JFileChooser();
				switch (mode) {
				case FILE:
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					break;
				case FOLDER:
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					break;
				}
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.CANCEL_OPTION) {
					return;
				}
				nameField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

	public enum FileChooserMode {
		FILE, FOLDER
	}
}
