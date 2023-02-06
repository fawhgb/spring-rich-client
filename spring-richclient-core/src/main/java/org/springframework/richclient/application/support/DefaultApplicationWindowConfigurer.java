/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.application.support;

import java.awt.Dimension;
import java.awt.Image;

import org.springframework.core.style.ToStringCreator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class DefaultApplicationWindowConfigurer implements ApplicationWindowConfigurer {

	private String title = "New Application Window";

	private Image image;

	private boolean showMenuBar = true;

	private boolean showToolBar = true;

	private boolean showStatusBar = true;

	private Dimension initialSize = new Dimension(800, 600);

	private ApplicationWindow window;

	public DefaultApplicationWindowConfigurer(ApplicationWindow window) {
		Assert.notNull(window, "Application window is required");
		this.window = window;
	}

	@Override
	public ApplicationWindow getWindow() {
		return window;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public Dimension getInitialSize() {
		return initialSize;
	}

	@Override
	public boolean getShowMenuBar() {
		return showMenuBar;
	}

	@Override
	public boolean getShowToolBar() {
		return showToolBar;
	}

	@Override
	public boolean getShowStatusBar() {
		return showStatusBar;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	public void setInitialSize(Dimension initialSize) {
		if (initialSize != null) {
			this.initialSize = initialSize;
		}
	}

	@Override
	public void setShowMenuBar(boolean showMenuBar) {
		this.showMenuBar = showMenuBar;
	}

	@Override
	public void setShowToolBar(boolean showToolBar) {
		this.showToolBar = showToolBar;
	}

	@Override
	public void setShowStatusBar(boolean showStatusBar) {
		this.showStatusBar = showStatusBar;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("title", title).append("image", image)
				.append("showMenuBar", showMenuBar).append("showToolBar", showToolBar)
				.append("showStatusBar", showStatusBar).append("initialSize", initialSize).append("window", window)
				.toString();
	}

}