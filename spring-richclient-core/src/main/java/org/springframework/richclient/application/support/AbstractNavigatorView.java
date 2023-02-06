package org.springframework.richclient.application.support;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupJComponentBuilder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Abstract class for views that use some sort of navigation component for the
 * overall application.
 */
public abstract class AbstractNavigatorView extends AbstractView {
	private CommandGroup currentNavigation;

	protected AbstractNavigatorView(CommandGroup currentNavigation) {
		this.currentNavigation = currentNavigation;
	}

	public abstract CommandGroupJComponentBuilder getNavigationBuilder();

	@Override
	protected JComponent createControl() {
		JPanel navigationView = new JPanel(new FormLayout("fill:pref:grow", "fill:pref:grow"));
		navigationView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		CellConstraints cc = new CellConstraints();
		navigationView.add(getNavigationBuilder().buildComponent(this.currentNavigation), cc.xy(1, 1));
		return navigationView;
	}
}
