/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.richclient.progress;

import javax.swing.JProgressBar;

import org.springframework.richclient.util.SwingUtilitiesHelper;
import org.springframework.util.Assert;

/**
 * <code>ProgressMonitor</code> implementation that delegates to a
 * <code>JProgressBar</code>.
 *
 * @author Peter De Bruycker
 */
public class ProgressBarProgressMonitor implements ProgressMonitor {

	private JProgressBar progressBar;
	private boolean canceled;

	public ProgressBarProgressMonitor(JProgressBar progressBar) {
		Assert.notNull(progressBar, "ProgressBar cannot be null.");
		this.progressBar = progressBar;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void setCanceled(boolean b) {
		this.canceled = b;
	}

	@Override
	public void done() {
		// not used
	}

	@Override
	public void subTaskStarted(final String name) {
		SwingUtilitiesHelper.executeWithEDTCheck(new Runnable() {
			@Override
			public void run() {
				progressBar.setString(name);
			}
		});

	}

	@Override
	public void taskStarted(final String name, final int totalWork) {
		SwingUtilitiesHelper.executeWithEDTCheck(new Runnable() {
			@Override
			public void run() {
				progressBar.setIndeterminate(false);
				progressBar.setMinimum(0);
				progressBar.setMaximum(totalWork);
				progressBar.setString(name);
			}
		});
	}

	@Override
	public void worked(final int work) {
		SwingUtilitiesHelper.executeWithEDTCheck(new Runnable() {
			@Override
			public void run() {
				progressBar.setValue(progressBar.getValue() + work);
			}
		});

	}
}
