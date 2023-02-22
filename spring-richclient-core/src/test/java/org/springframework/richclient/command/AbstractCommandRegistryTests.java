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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

/**
 * This is an abstract test case for implementations of the
 * {@link CommandRegistry} interface. Subclasses only need to override the
 * {@link #getCommandRegistry()} method to return the concrete implementation to
 * be tested.
 *
 * @author Kevin Stembridge
 * @since 0.3
 *
 */
public abstract class AbstractCommandRegistryTests {

	/**
	 * Creates a new uninitialized {@code AbstractCommandRegistryTests}.
	 */
	protected AbstractCommandRegistryTests() {
		super();
	}

	/**
	 * Subclasses must override this method to provide the concrete implementation
	 * of the registry to be tested. A new, empy registry must be provided. This
	 * method may be called often, so subclasses should take care to not repeat any
	 * unnecessary initialization.
	 *
	 * @return The registry implementation to be tested, never null.
	 */
	protected abstract CommandRegistry getCommandRegistry();

	/**
	 * Tests the {@link CommandRegistry#registerCommand(AbstractCommand)} method.
	 */
	@Test
	public void testRegisterCommand() {

		CommandRegistry registry = getCommandRegistry();
		CommandRegistryListener listener = (CommandRegistryListener) EasyMock
				.createStrictMock(CommandRegistryListener.class);
		registry.addCommandRegistryListener(listener);

		EasyMock.replay(listener);

		try {
			registry.registerCommand(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		EasyMock.verify(listener);
		EasyMock.reset(listener);
		EasyMock.replay(listener);

		try {
			registry.registerCommand(new TestCommand());
			fail("Should throw IllegalArgumentException because commandId has not been set");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		EasyMock.verify(listener);
		EasyMock.reset(listener);

		TestCommand command1 = new TestCommand("command");
		CommandRegistryEvent event = new CommandRegistryEvent(registry, command1);

		listener.commandRegistered(matchEvent(event));

		EasyMock.replay(listener);
		registry.registerCommand(command1);
		EasyMock.verify(listener);

		assertTrue(registry.containsCommand(command1.getId()), "command1 not registered");
		assertEquals(command1, registry.getCommand(command1.getId()), "command1 not registered");

		TestCommand command2 = new TestCommand(command1.getId());
		event = new CommandRegistryEvent(registry, command2);

		EasyMock.reset(listener);

		listener.commandRegistered(matchEvent(event));
		EasyMock.replay(listener);
		registry.registerCommand(command2);
		EasyMock.verify(listener);

		assertTrue(registry.containsCommand(command2.getId()));
		assertEquals(command2, registry.getCommand(command2.getId()), "command1 not overridden");

	}

	/**
	 * Confirms that if a CommandGroup is being registered, it will obtain a
	 * reference to the registry that it is being added to.
	 */
	@Test
	public void testRegisterCommandGroup() {

		CommandRegistry registry = getCommandRegistry();

		CommandGroup commandGroup = new CommandGroup("testCommandGroup");
		registry.registerCommand(commandGroup);

		assertTrue(registry.containsCommand("testCommandGroup"), "commandgroup not registered");
		assertEquals(commandGroup, registry.getCommand("testCommandGroup"), "commandgroup not registered");

		assertEquals(registry, commandGroup.getCommandRegistry());

	}

	/**
	 * Tests the {@link CommandRegistry#isTypeMatch(String, Class)} method.
	 */
	@Test
	public final void testIsTypeMatch() {

		CommandRegistry registry = getCommandRegistry();

		try {
			registry.isTypeMatch(null, Object.class);
			fail("Should have thrown IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		try {
			registry.isTypeMatch("bogusCommandId", null);
			fail("Should have thrown IllegalArgumentException for null targetType");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		// Add a command to the registry which is a subclass of AbstractCommand
		TestCommand testCommand = new TestCommand("testCommand");
		registry.registerCommand(testCommand);

		assertTrue(registry.isTypeMatch(testCommand.getId(), TestCommand.class), "Assert isTypeMatch");
		assertTrue(registry.isTypeMatch(testCommand.getId(), AbstractCommand.class), "Assert isTypeMatch");
		assertFalse(registry.isTypeMatch(testCommand.getId(), String.class), "Assert isTypeMatch");

	}

	/**
	 * Tests the {@link CommandRegistry#containsCommand(String)} method.
	 */
	@Test
	public final void testContainsCommand() {

		CommandRegistry registry = getCommandRegistry();

		try {
			registry.containsCommand(null);
			fail("Should have thrown an IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		assertFalse(registry.containsCommand("bogusCommandId"), "Assert registry does not contain a command");

		TestCommand testCommand = new TestCommand("testCommand");
		registry.registerCommand(testCommand);

		assertTrue(registry.containsCommand(testCommand.getId()), "Assert registry contains command");

	}

	/**
	 * Tests the {@link CommandRegistry#getCommand(String)} method.
	 */
	@Test
	public final void testGetCommandById() {

		CommandRegistry registry = getCommandRegistry();

		try {
			registry.getCommand(null);
			fail("Should have thrown an IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		assertNull(registry.getCommand("bogusCommandId"), "getCommand should return null");

		TestCommand testCommand = new TestCommand("testCommand");
		registry.registerCommand(testCommand);

		assertEquals(testCommand, registry.getCommand(testCommand.getId()));

	}

	/**
	 * Tests the {@link CommandRegistry#getCommand(String, Class)} method.
	 */
	@Test
	public final void testGetCommandByIdAndRequiredType() {

		CommandRegistry registry = getCommandRegistry();

		try {
			registry.getCommand(null, Object.class);
			fail("Should have thrown an IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		assertNull(registry.getCommand("bogusCommandId", null));

		assertNull(registry.getCommand("bogusCommandId", TestCommand.class), "getCommand should return null");

		TestCommand testCommand = new TestCommand("testCommand");
		TestCommand2 testCommand2 = new TestCommand2("testCommand2");
		registry.registerCommand(testCommand);
		registry.registerCommand(testCommand2);

		assertEquals(testCommand, registry.getCommand(testCommand.getId(), TestCommand.class));
		assertEquals(testCommand, registry.getCommand(testCommand.getId(), AbstractCommand.class));

		try {
			registry.getCommand(testCommand.getId(), TestCommand2.class);
			fail("Should have thrown CommandNotOfRequiredTypeException");
		} catch (CommandNotOfRequiredTypeException e) {
			assertEquals(testCommand.getId(), e.getCommandId());
			assertEquals(TestCommand2.class, e.getRequiredType());
			assertEquals(TestCommand.class, e.getActualType());
		}

	}

	/**
	 * Tests the {@link CommandRegistry#getType(String)} method.
	 */
	@Test
	public final void testGetType() {

		CommandRegistry registry = getCommandRegistry();

		try {
			registry.getType(null);
			fail("Should have thrown an IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		assertNull(registry.getType("bogusCommandId"), "getType should return null");

		TestCommand testCommand = new TestCommand("testCommand");
		TestCommand2 testCommand2 = new TestCommand2("testCommand2");
		registry.registerCommand(testCommand);
		registry.registerCommand(testCommand2);

		assertEquals(testCommand.getClass(), registry.getType(testCommand.getId()));
		assertEquals(testCommand2.getClass(), registry.getType(testCommand2.getId()));

	}

	/**
	 * Tests the
	 * {@link CommandRegistry#setTargetableActionCommandExecutor(String, ActionCommandExecutor)}
	 * method.
	 */
	@Test
	public void testSetTargetableActionCommandExecutor() {

		CommandRegistry registry = getCommandRegistry();
		TestTargetableActionCommand targetableCommand = new TestTargetableActionCommand();
		targetableCommand.setId("bogusId");
		registry.registerCommand(targetableCommand);
		ActionCommandExecutor executor = (ActionCommandExecutor) EasyMock.createMock(ActionCommandExecutor.class);

		try {
			registry.setTargetableActionCommandExecutor(null, null);
			fail("Should have thrown an IllegalArgumentException for null commandId");
		} catch (IllegalArgumentException e) {
			// test passes
		}

		registry.setTargetableActionCommandExecutor(targetableCommand.getId(), executor);

		assertEquals(executor, targetableCommand.getCommandExecutor());

		registry.setTargetableActionCommandExecutor(targetableCommand.getId(), null);

		assertEquals(null, targetableCommand.getCommandExecutor());

	}

	private static class TestCommand extends AbstractCommand {

		private String id;

		/**
		 * Creates a new uninitialized {@code TestCommand}.
		 *
		 */
		public TestCommand() {
			// do nothing
		}

		/**
		 * Creates a new uninitialized {@code TestCommand}.
		 *
		 * @param id
		 */
		public TestCommand(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return this.id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void execute() {
			// do nothing
		}

	}

	private static class TestCommand2 extends AbstractCommand {

		private String id;

		/**
		 * Creates a new uninitialized {@code TestCommand2}.
		 *
		 */
		public TestCommand2() {
			// do nothing
		}

		/**
		 * Creates a new uninitialized {@code TestCommand2}.
		 *
		 * @param id
		 */
		public TestCommand2(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return this.id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void execute() {
			// do nothing
		}

	}

	private static CommandRegistryEvent matchEvent(CommandRegistryEvent event) {
		EasyMock.reportMatcher(new CommandRegistryEventMatcher(event));
		return event;
	}

	private static class CommandRegistryEventMatcher implements IArgumentMatcher {

		private CommandRegistryEvent expectedEvent;

		/**
		 * Creates a new {@code CommandRegistryEventMatcher}.
		 *
		 * @param expectedEvent
		 */
		public CommandRegistryEventMatcher(CommandRegistryEvent expectedEvent) {
			this.expectedEvent = expectedEvent;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void appendTo(StringBuffer buffer) {
			buffer.append("(");
			buffer.append(expectedEvent.getClass().getName());
			buffer.append(" with message \"");
			buffer.append(expectedEvent.getSource());
			buffer.append("\"\")");

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean matches(Object argument) {

			if (!(argument instanceof CommandRegistryEvent)) {
				return false;
			}

			CommandRegistryEvent other = (CommandRegistryEvent) argument;

			if (!ObjectUtils.nullSafeEquals(expectedEvent.getSource(), other.getSource())) {
				return false;
			}

			if (!ObjectUtils.nullSafeEquals(expectedEvent.getCommand(), other.getCommand())) {
				return false;
			}

			return true;

		}

	}

	private static class TestTargetableActionCommand extends TargetableActionCommand {

		private ActionCommandExecutor executor;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setCommandExecutor(ActionCommandExecutor commandExecutor) {
			this.executor = commandExecutor;
		}

		public ActionCommandExecutor getCommandExecutor() {
			return this.executor;
		}

	}

}
