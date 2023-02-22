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
package org.springframework.richclient.command.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.AbstractCommandRegistryTests;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandNotOfRequiredTypeException;
import org.springframework.richclient.command.CommandRegistry;
import org.springframework.richclient.command.CommandRegistryListener;

/**
 * Provides unit tests for the {@link DefaultCommandRegistry} class.
 * 
 * @author Peter De Bruycker
 * @author Kevin Stembridge
 */
public class DefaultCommandRegistryTests extends AbstractCommandRegistryTests {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CommandRegistry getCommandRegistry() {
		return new DefaultCommandRegistry();
	}

	@Test
	public void testConstructor() {
		DefaultCommandRegistry registry = new DefaultCommandRegistry();
		assertNull(registry.getParent(), "parent must be null");

		TestCommandRegistry parent = new TestCommandRegistry();
		registry = new DefaultCommandRegistry(parent);
		assertEquals(parent, registry.getParent(), "parent not set");
		assertEquals(1, parent.addedListeners.size(), "registry not added to parent");
		assertTrue(parent.addedListeners.contains(registry), "registry not added to parent");
	}

	@Test
	public void testSetParent() {
		TestCommandRegistry parent = new TestCommandRegistry();
		TestCommandRegistry parent2 = new TestCommandRegistry();

		DefaultCommandRegistry registry = new DefaultCommandRegistry(parent);

		registry.setParent(parent2);

		assertEquals(parent2, registry.getParent(), "parent not set");
		assertEquals(1, parent.removedListeners.size(), "registry not removed from parent");
		assertTrue(parent.removedListeners.contains(registry), "registry not removed from parent");
		assertEquals(1, parent2.addedListeners.size(), "registry not added to parent");
		assertTrue(parent2.addedListeners.contains(registry), "registry not added to parent");

		// set same parent, nothing should happen
		registry.setParent(parent2);
		assertEquals(1, parent2.addedListeners.size(), "registry added twice to same parent");
		assertTrue(parent2.removedListeners.isEmpty(), "registry removed from same parent");

		parent2.reset();

		// set parent to null
		registry.setParent(null);
		assertNull(registry.getParent(), "parent not set to null");
		assertEquals(1, parent2.removedListeners.size(), "registry not removed from parent");
	}

	private static class TestCommandRegistry implements CommandRegistry {
		private List addedListeners = new ArrayList();

		private List removedListeners = new ArrayList();

		@Override
		public ActionCommand getActionCommand(String commandId) {
			return null;
		}

		@Override
		public CommandGroup getCommandGroup(String groupId) {
			return null;
		}

		@Override
		public boolean containsActionCommand(String commandId) {
			return false;
		}

		@Override
		public boolean containsCommandGroup(String groupId) {
			return false;
		}

		@Override
		public void registerCommand(AbstractCommand command) {
		}

		@Override
		public void setTargetableActionCommandExecutor(String targetableCommandId,
				ActionCommandExecutor commandExecutor) {
		}

		@Override
		public void addCommandRegistryListener(CommandRegistryListener l) {
			addedListeners.add(l);
		}

		@Override
		public void removeCommandRegistryListener(CommandRegistryListener l) {
			removedListeners.add(l);
		}

		public void reset() {
			addedListeners.clear();
			removedListeners.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean containsCommand(String commandId) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getCommand(String commandId) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getCommand(String commandId, Class requiredType) throws CommandNotOfRequiredTypeException {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class getType(String commandId) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isTypeMatch(String commandId, Class targetType) {
			return false;
		}

	}

}
