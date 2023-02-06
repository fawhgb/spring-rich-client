package org.springframework.richclient.components;

import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.springframework.richclient.form.HasValidationComponent;

public class PanelWithValidationComponent extends JPanel implements HasValidationComponent {
	private static final long serialVersionUID = 1L;

	public PanelWithValidationComponent() {
		super();
	}

	public PanelWithValidationComponent(LayoutManager layoutManager) {
		super(layoutManager);
	}

	public PanelWithValidationComponent(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public PanelWithValidationComponent(LayoutManager layoutManager, boolean isDoubleBuffered) {
		super(layoutManager, isDoubleBuffered);
	}

	/**
	 * Geef de component waarop de validatiekleur en het icoontje terecht komen.
	 */
	@Override
	public JComponent getValidationComponent() {
		return null;
	}

}
