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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.junit.jupiter.api.Test;

/**
 * Provides a suite of unit tests for the {@link SwingActionAdapter} class.
 *
 * @author Kevin Stembridge
 * @since 0.3
 *
 */
public class SwingActionAdapterTests {

	/**
	 * Test method for {@link SwingActionAdapter#SwingActionAdapter(ActionCommand)}.
	 */
	@Test
	public final void testConstructor() {

		try {
			new SwingActionAdapter(null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

		// can't use EasyMock here because ActionCommand is not an interface
		MockActionCommand command = new MockActionCommand();
		command.setActionCommand("bogusActionCommand");
		command.setCaption("bogusCaption");

		SwingActionAdapter adapter = new SwingActionAdapter(command);

		assertEquals("bogusActionCommand", adapter.getValue(Action.ACTION_COMMAND_KEY));
		assertEquals("bogusCaption", adapter.getValue(Action.SHORT_DESCRIPTION));

		command.setCaption("newCaption");

		adapter.update();

		assertEquals("newCaption", adapter.getValue(Action.SHORT_DESCRIPTION));

	}

	/**
	 * Test method for
	 * {@link SwingActionAdapter#actionPerformed(java.awt.event.ActionEvent)}.
	 */
	@Test
	public final void testActionPerformed() {

		MockActionCommand command = new MockActionCommand();
		SwingActionAdapter adapter = new SwingActionAdapter(command);
		adapter.actionPerformed(new ActionEvent(command, 1, "bogus"));

		assertEquals(1, command.getActionPerformedCount());

	}

	private class MockActionCommand extends ActionCommand {

		private int actionPerformedCount;
		private PropertyChangeListener enabledListener;
		private PropertyChangeListener propertyChangeListener;

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void doExecuteCommand() {
			this.actionPerformedCount++;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addEnabledListener(PropertyChangeListener listener) {
			this.enabledListener = listener;
		}

		/**
		 * Returns the number of times the actionPerformed method has been called.
		 * 
		 * @return actionPerformedCount
		 */
		public int getActionPerformedCount() {
			return this.actionPerformedCount;
		}

	}

}
