/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.richclient.factory;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * @author Keith Donald
 */
public class DefaultMenuFactory implements MenuFactory {

	@Override
	public JMenu createMenu() {
		return new JMenu();
	}

	@Override
	public JMenuItem createMenuItem() {
		return new JMenuItem();
	}

	@Override
	public JCheckBoxMenuItem createCheckBoxMenuItem() {
		return new JCheckBoxMenuItem();
	}

	@Override
	public JRadioButtonMenuItem createRadioButtonMenuItem() {
		return new JRadioButtonMenuItem();
	}

	@Override
	public JPopupMenu createPopupMenu() {
		return new JPopupMenu();
	}

	@Override
	public JMenuBar createMenuBar() {
		return new JMenuBar();
	}

}