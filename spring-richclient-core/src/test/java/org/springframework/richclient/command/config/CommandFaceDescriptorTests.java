/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.richclient.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.core.DescribedElement;
import org.springframework.richclient.image.EmptyIcon;

/**
 * @author Peter De Bruycker
 */
public class CommandFaceDescriptorTests {

	private CommandButtonLabelInfo buttonLabelInfo;

	private CommandFaceDescriptor descriptor;

	private TestPropertyChangeListener propertyChangeListener;

	@Test
	public void testDefaultConstructor() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor();

		assertEquals(CommandButtonLabelInfo.BLANK_BUTTON_LABEL, descriptor.getLabelInfo());
		assertTrue(descriptor.isBlank());
		assertEquals(CommandButtonLabelInfo.BLANK_BUTTON_LABEL.getText(), descriptor.getText());
		assertNull(descriptor.getDescription());
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getIconInfo());
		assertNull(descriptor.getCaption());
	}

	@Test
	public void testConstructorWithNullButtonLabelInfo() {
		try {
			new CommandFaceDescriptor((CommandButtonLabelInfo) null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	@Test
	public void testConstructorWithEncodedLabel() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor("&Test@ctrl T");
		assertEquals("Test", descriptor.getText());
		assertFalse(CommandButtonLabelInfo.BLANK_BUTTON_LABEL.equals(descriptor.getLabelInfo()));
		assertFalse(descriptor.isBlank());
		assertNull(descriptor.getDescription());
		assertNull(descriptor.getCaption());
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getIconInfo());
	}

	@Test
	public void testConstructorWithEmptyEncodedLabel() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor("");
		assertEquals(CommandButtonLabelInfo.BLANK_BUTTON_LABEL, descriptor.getLabelInfo());
	}

	@Test
	public void testConstructorWithEncodedLabelAndIcon() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor("&Test@ctrl T", EmptyIcon.SMALL, "caption");

		assertEquals("Test", descriptor.getText());
		assertFalse(CommandButtonLabelInfo.BLANK_BUTTON_LABEL.equals(descriptor.getLabelInfo()));
		assertFalse(descriptor.isBlank());
		assertNull(descriptor.getDescription());
		assertEquals("caption", descriptor.getCaption());
		assertFalse(CommandButtonIconInfo.BLANK_ICON_INFO.equals(descriptor.getIconInfo()));
		assertEquals(EmptyIcon.SMALL, descriptor.getIconInfo().getIcon());
	}

	@Test
	public void testSetDescription() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor("&Test@ctrl T", EmptyIcon.SMALL, "caption");
		descriptor.setDescription("Long description");

		assertEquals("Long description", descriptor.getDescription());
	}

	@Test
	public void testSetCaption() {
		descriptor.setCaption("new caption");
		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals("caption", propertyChangeListener.oldValue);
		assertEquals("new caption", propertyChangeListener.newValue);
		assertEquals(DescribedElement.CAPTION_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();

		// caption not changed
		descriptor.setCaption("new caption");
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testSetIconNull() {
		descriptor.setIcon(null);
		assertNull(descriptor.getIconInfo().getIcon());

		descriptor.setIconInfo(CommandButtonIconInfo.BLANK_ICON_INFO);
		descriptor.setIcon(null);
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getIconInfo());
	}

	@Test
	public void testSetIcon() {
		Icon oldIcon = descriptor.getIcon();
		descriptor.setIcon(EmptyIcon.LARGE);
		assertEquals(EmptyIcon.LARGE, descriptor.getIcon());

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldIcon, propertyChangeListener.oldValue);
		assertEquals(descriptor.getIcon(), propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.ICON_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setIcon(EmptyIcon.LARGE);
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testSetLargeIconNull() {
		descriptor.setLargeIcon(null);
		assertNull(descriptor.getLargeIconInfo().getIcon());

		descriptor.setLargeIconInfo(CommandButtonIconInfo.BLANK_ICON_INFO);
		descriptor.setLargeIcon(null);
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getLargeIconInfo());
	}

	@Test
	public void testSetLargeIcon() {
		Icon oldIcon = descriptor.getLargeIcon();
		descriptor.setLargeIcon(EmptyIcon.LARGE);
		assertEquals(EmptyIcon.LARGE, descriptor.getLargeIcon());

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldIcon, propertyChangeListener.oldValue);
		assertEquals(descriptor.getLargeIcon(), propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.LARGE_ICON_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setLargeIcon(EmptyIcon.LARGE);
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testSetNullIconInfo() {
		descriptor.setIconInfo(null);
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getIconInfo());
	}

	@Test
	public void testSetNullLabelInfo() {
		descriptor.setLabelInfo(null);
		assertEquals(CommandButtonLabelInfo.BLANK_BUTTON_LABEL, descriptor.getLabelInfo());
	}

	@Test
	public void testConfigureWithConfigurer() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				JButton button = new JButton();
				TestCommandButtonConfigurer configurer = new TestCommandButtonConfigurer();
				descriptor.configure(button, null, configurer);
				assertEquals(button, configurer.button);
				assertEquals(descriptor, configurer.face);
			}
		});
	}

	@Test
	public void testConfigureWithNullConfigurerAndNullButton() {
		try {
			GuiActionRunner.execute(new GuiTask() {
				@Override
				protected void executeInEDT() throws Throwable {
					descriptor.configure(new JButton(), null, null);
					fail("Should throw IllegalArgumentException");
				}
			});
		} catch (IllegalArgumentException e) {
			pass();
		}
		try {
			descriptor.configure(null, null, new TestCommandButtonConfigurer());
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	private static class TestCommandButtonConfigurer extends DefaultCommandButtonConfigurer
			implements CommandButtonConfigurer {
		private CommandFaceDescriptor face;

		private AbstractButton button;

		@Override
		public void configure(AbstractButton button, AbstractCommand command, CommandFaceDescriptor faceDescriptor) {
			super.configure(button, command, faceDescriptor);
			this.face = faceDescriptor;
			this.button = button;
		}
	}

	@Test
	public void testSetLabelInfoAsText() {
		CommandButtonLabelInfo oldLabelInfo = descriptor.getLabelInfo();
		descriptor.setButtonLabelInfo("&Other Test@ctrl O");
		CommandButtonLabelInfo newLabelInfo = descriptor.getLabelInfo();
		assertEquals(CommandButtonLabelInfo.valueOf("&Other Test@ctrl O"), newLabelInfo);

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldLabelInfo, propertyChangeListener.oldValue);
		assertEquals(newLabelInfo, propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.LABEL_INFO_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setButtonLabelInfo("&Other Test@ctrl O");
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testSetLabelInfo() {
		CommandButtonLabelInfo oldLabelInfo = descriptor.getLabelInfo();
		CommandButtonLabelInfo newLabelInfo = CommandButtonLabelInfo.valueOf("&Other Test@ctrl O");
		descriptor.setLabelInfo(newLabelInfo);
		assertEquals(newLabelInfo, descriptor.getLabelInfo());

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldLabelInfo, propertyChangeListener.oldValue);
		assertEquals(newLabelInfo, propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.LABEL_INFO_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setButtonLabelInfo("&Other Test@ctrl O");
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testConfigureNullAction() {
		try {
			descriptor.configure(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	@Test
	public void testConfigure() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		};

		descriptor.configure(action);
		assertEquals(descriptor.getLabelInfo().getText(), action.getValue(Action.NAME), "name");
		assertEquals(new Integer(descriptor.getLabelInfo().getMnemonic()), action.getValue(Action.MNEMONIC_KEY),
				"mnemonic");
		assertEquals(descriptor.getLabelInfo().getAccelerator(), action.getValue(Action.ACCELERATOR_KEY),
				"accelerator");
		assertEquals(descriptor.getIconInfo().getIcon(), action.getValue(Action.SMALL_ICON), "icon");
		assertEquals(descriptor.getCaption(), action.getValue(Action.SHORT_DESCRIPTION), "caption");
		assertEquals(descriptor.getDescription(), action.getValue(Action.LONG_DESCRIPTION), "description");
	}

	@Test
	public void testSetIconInfo() {
		CommandButtonIconInfo oldIconInfo = descriptor.getIconInfo();
		CommandButtonIconInfo newIconInfo = new CommandButtonIconInfo(EmptyIcon.LARGE);
		descriptor.setIconInfo(newIconInfo);
		assertEquals(newIconInfo, descriptor.getIconInfo());

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldIconInfo, propertyChangeListener.oldValue);
		assertEquals(newIconInfo, propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.ICON_INFO_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setIconInfo(newIconInfo);
		assertFalse(propertyChangeListener.changed);
	}

	@Test
	public void testSetLargeIconInfo() {
		CommandButtonIconInfo oldIconInfo = descriptor.getLargeIconInfo();
		CommandButtonIconInfo newIconInfo = new CommandButtonIconInfo(EmptyIcon.LARGE);
		descriptor.setLargeIconInfo(newIconInfo);
		assertEquals(newIconInfo, descriptor.getLargeIconInfo());

		assertTrue(propertyChangeListener.changed);
		assertEquals(descriptor, propertyChangeListener.source);
		assertEquals(oldIconInfo, propertyChangeListener.oldValue);
		assertEquals(newIconInfo, propertyChangeListener.newValue);
		assertEquals(CommandFaceDescriptor.LARGE_ICON_INFO_PROPERTY, propertyChangeListener.propertyName);

		propertyChangeListener.reset();
		// no change
		descriptor.setLargeIconInfo(newIconInfo);
		assertFalse(propertyChangeListener.changed);
	}

	private static class TestPropertyChangeListener implements PropertyChangeListener {

		private boolean changed = false;

		private String propertyName;

		private Object newValue;

		private Object oldValue;

		private Object source;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
		 * PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			changed = true;
			propertyName = e.getPropertyName();
			newValue = e.getNewValue();
			oldValue = e.getOldValue();
			source = e.getSource();
		}

		/**
		 *  
		 */
		public void reset() {
			changed = false;
			propertyName = null;
			newValue = null;
			oldValue = null;
			source = null;
		}

	}

	private static void pass() {
		// test passes
	}

	@Test
	public void testConstructorWithButtonLabelInfo() {
		CommandFaceDescriptor descriptor = new CommandFaceDescriptor(buttonLabelInfo);

		assertEquals(buttonLabelInfo, descriptor.getLabelInfo());
		assertFalse(descriptor.isBlank());
		assertEquals("Test", descriptor.getText());
		assertNull(descriptor.getDescription());
		assertEquals(CommandButtonIconInfo.BLANK_ICON_INFO, descriptor.getIconInfo());
		assertNull(descriptor.getCaption());
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		buttonLabelInfo = CommandButtonLabelInfo.valueOf("&Test@ctrl T");
		descriptor = new CommandFaceDescriptor("&Test@ctrl T", EmptyIcon.SMALL, "caption");
		descriptor.setDescription("long description");
		assertNotNull(descriptor.getLabelInfo().getAccelerator());
		propertyChangeListener = new TestPropertyChangeListener();
		descriptor.addPropertyChangeListener(propertyChangeListener);
	}

}
