package org.springframework.richclient.widget;

import java.util.Collections;
import java.util.List;

import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.command.AbstractCommand;

/**
 * Default behavior implementation of AbstractWidget
 */
public abstract class AbstractWidget extends ApplicationServicesAccessor implements Widget {
	protected boolean showing = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAboutToShow() {
		showing = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAboutToHide() {
		showing = false;
	}

	@Override
	public boolean isShowing() {
		return showing;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Default: Widget can be closed.
	 */
	@Override
	public boolean canClose() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends AbstractCommand> getCommands() {
		return Collections.emptyList();
	}
}