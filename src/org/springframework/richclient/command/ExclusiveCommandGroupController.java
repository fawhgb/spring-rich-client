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
package org.springframework.richclient.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExclusiveCommandGroupController {

	private List commands = new ArrayList();

	private boolean allowsEmptySelection;

	public ExclusiveCommandGroupController() {
	}

	public boolean getAllowsEmptySelection() {
		return allowsEmptySelection;
	}

	public void setAllowsEmptySelection(boolean allowsEmptySelection) {
		this.allowsEmptySelection = allowsEmptySelection;
	}

	public void add(ToggleCommand command) {
		if (!commands.contains(command)) {
			commands.add(command);
			command.setExclusiveController(this);
		}
	}

	public void remove(ToggleCommand command) {
		if (commands.remove(command)) {
			command.setExclusiveController(null);
		}
	}

	public void handleSelectionRequest(ToggleCommand delegatingCommand, boolean requestsSelection) {
		if (requestsSelection) {
			ToggleCommand previousSelectedCommand = null;

			for (Iterator iterator = commands.iterator(); iterator.hasNext();) {
				ToggleCommand command = (ToggleCommand)iterator.next();
				if (command.isSelected()) {
					previousSelectedCommand = command;
					break;
				}
			}

			if (previousSelectedCommand == null) {
				delegatingCommand.requestSetSelection(true);
			}
			else {
				previousSelectedCommand.requestSetSelection(false);

				delegatingCommand.requestSetSelection(!previousSelectedCommand.isSelected());

				if (!delegatingCommand.isSelected() && previousSelectedCommand != null) {
					previousSelectedCommand.requestSetSelection(true);
				}
			}
		}
		else {
			// its a deselection
			if (allowsEmptySelection) {
				delegatingCommand.requestSetSelection(false);
			}
		}
	}

}