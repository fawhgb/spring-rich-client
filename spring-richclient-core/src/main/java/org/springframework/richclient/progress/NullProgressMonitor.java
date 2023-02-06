/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.richclient.progress;

/**
 * <code>ProgressMonitor</code> implementation that does nothing.
 *
 * @author Peter De Bruycker
 */
public class NullProgressMonitor implements ProgressMonitor {

	private boolean canceled;

	@Override
	public void done() {
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void setCanceled(boolean b) {
		canceled = b;
	}

	@Override
	public void subTaskStarted(String name) {
	}

	@Override
	public void taskStarted(String name, int totalWork) {
	}

	@Override
	public void worked(int work) {
	}
}
