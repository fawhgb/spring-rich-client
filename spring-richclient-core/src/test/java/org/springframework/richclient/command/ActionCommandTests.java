/*
 * Copyright 2002-2007 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JButton;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.Test;

/**
 * Testcase for ActionCommand
 * 
 * @author Peter De Bruycker
 */
public class ActionCommandTests {

	@Test
	public void testOnButtonAttached() {
		final boolean[] executed = { false };

		ActionCommand command = new ActionCommand() {
			@Override
			protected void doExecuteCommand() {
				executed[0] = true;
			}
		};
		command.setActionCommand("theActionCommand");

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				JButton button = new JButton("test");

				command.onButtonAttached(button);

				assertEquals("theActionCommand", button.getActionCommand());

				button.doClick();
				assertTrue(executed[0]);
			}
		});
	}

	@Test
	public void testOnButtonAttachedWithDisplayDialog() {
		ActionCommand command = new ActionCommand() {
			@Override
			protected void doExecuteCommand() {
				// do nothing
			}
		};
		command.setDisplaysInputDialog(true);

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				JButton button = new JButton();
				button.setText(null);

				command.onButtonAttached(button);

				assertEquals(null, button.getText());
			}
		});
	}

}
