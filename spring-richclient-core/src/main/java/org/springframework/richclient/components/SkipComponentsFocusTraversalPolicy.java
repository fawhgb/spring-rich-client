package org.springframework.richclient.components;

import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.text.JTextComponent;

/**
 * Traversal policy designed to skip certain components
 */
public class SkipComponentsFocusTraversalPolicy extends LayoutFocusTraversalPolicy {
	private static final long serialVersionUID = 1L;

	List<JComponent> componentsToSkip;

	public static final SkipComponentsFocusTraversalPolicy skipJTextComponentTraversalPolicy = new SkipComponentsFocusTraversalPolicy();

	public SkipComponentsFocusTraversalPolicy() {
	}

	public SkipComponentsFocusTraversalPolicy(List<JComponent> componentsToSkip) {
		this.componentsToSkip = componentsToSkip;
	}

	@Override
	protected boolean accept(Component aComponent) {
		if (!super.accept(aComponent)
				|| (aComponent instanceof JTextComponent && !((JTextComponent) aComponent).isEditable())) {
			return false;
		}

		if (componentsToSkip != null) {
			for (JComponent component : componentsToSkip) {
				if (component == aComponent || component.isAncestorOf(aComponent)) {
					return false;
				}
			}
		}
		return true;
	}
}