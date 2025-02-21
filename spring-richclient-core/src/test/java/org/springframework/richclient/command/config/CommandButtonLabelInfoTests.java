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
package org.springframework.richclient.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.richclient.core.LabelInfo;

/**
 * @author Peter De Bruycker
 * @author Kevin Stembridge
 */
public class CommandButtonLabelInfoTests {

	private LabelInfo labelInfo;

	private KeyStroke accelerator;

	private Map invalidLabelDescriptors;

	public static void pass() {
		// test passes
	}

	/**
	 * Creates a new {@code LabelInfoTests}.
	 */
	public CommandButtonLabelInfoTests() {
		this.invalidLabelDescriptors = new HashMap();
		this.invalidLabelDescriptors.put("@", "An @ symbol must be followed by a KeyStroke.");
		this.invalidLabelDescriptors.put("Test@Bogus", "Invalid KeyStroke format.");
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		labelInfo = new LabelInfo("Test", 'T', 3);
		accelerator = KeyStroke.getKeyStroke("ctrl T");

		// make sure the keystroke did get parsed
		assertNotNull(accelerator);
	}

	@Test
	public void testConstructorLabelInfo() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo(labelInfo, accelerator);

		assertEquals(labelInfo.getText(), info.getText());
		assertEquals(labelInfo.getMnemonic(), info.getMnemonic());
		assertEquals(labelInfo.getMnemonicIndex(), info.getMnemonicIndex());
		assertEquals(accelerator, info.getAccelerator());
	}

	@Test
	public void testConstructorLabelInfoNoAccelerator() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo(labelInfo, null);

		assertEquals(labelInfo.getText(), info.getText());
		assertEquals(labelInfo.getMnemonic(), info.getMnemonic());
		assertEquals(labelInfo.getMnemonicIndex(), info.getMnemonicIndex());
		assertNull(info.getAccelerator());
	}

	@Test
	public void testConstructorText() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo("Test");

		assertEquals("Test", info.getText());
		assertEquals(0, info.getMnemonic());
		assertEquals(-1, info.getMnemonicIndex());
		assertNull(info.getAccelerator());
	}

	@Test
	public void testConstructorNullAsLabelInfo() {
		try {
			new CommandButtonLabelInfo(null, accelerator);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	@Test
	public void testConfigureNull() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo("test");
		try {
			info.configure(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	@Test
	public void testConfigureJButton() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo(labelInfo, accelerator);

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				// try a button
				JButton button = new JButton();
				info.configure(button);

				assertEquals(info.getText(), button.getText());
				assertEquals(info.getMnemonic(), button.getMnemonic());
				assertEquals(info.getMnemonicIndex(), button.getDisplayedMnemonicIndex());
			}
		});
	}

	@Test
	public void testConfigureJMenuItem() {
		CommandButtonLabelInfo info = new CommandButtonLabelInfo(labelInfo, accelerator);

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				// try a menu item
				JMenuItem button = new JMenuItem();
				info.configure(button);

				assertEquals(info.getText(), button.getText());
				assertEquals(info.getMnemonic(), button.getMnemonic());
				assertEquals(info.getMnemonicIndex(), button.getDisplayedMnemonicIndex());
				assertEquals(accelerator, button.getAccelerator());
			}
		});
	}

	@Test
	public void testCreateButtonLabelInfoNoAccelerator() {
		CommandButtonLabelInfo info = CommandButtonLabelInfo.valueOf("S\\&ave with an \\@ &as");
		System.out.println("XXXXXXXXXXXXXXX " + info.getText());
		System.out.println("XXXXXXXXXXX 16th char = " + info.getText().charAt(16));
		assertEquals("S&ave with an @ as", info.getText());
		assertEquals(KeyEvent.VK_A, info.getMnemonic());
		assertEquals(16, info.getMnemonicIndex());
		assertNull(info.getAccelerator());
	}

	@Test
	public void testCreateButtonLabelInfo() {
		CommandButtonLabelInfo info = CommandButtonLabelInfo.valueOf("S\\@ve &as@ctrl A");

		assertEquals("S@ve as", info.getText());
		assertEquals(KeyEvent.VK_A, info.getMnemonic());
		assertEquals(5, info.getMnemonicIndex());
		assertNotNull(info.getAccelerator(), "ctrl A is invalid keystroke");
		assertEquals(KeyStroke.getKeyStroke("ctrl A"), info.getAccelerator());
	}

	@Test
	public void testCreateButtonLabelInfoInvalidAccelerator() {

		try {
			CommandButtonLabelInfo.valueOf("Save &as@Bogus keystroke");
			fail("Should have thrown an IllegalArgumentException for invalid KeyStroke format");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Confirms that exceptions are thrown for label descriptors that violate the
	 * syntax rules.
	 */
	@Test
	public void testInvalidSyntax() {

		Iterator entryIterator = this.invalidLabelDescriptors.entrySet().iterator();

		while (entryIterator.hasNext()) {

			Map.Entry entry = (Map.Entry) entryIterator.next();

			try {
				CommandButtonLabelInfo.valueOf((String) entry.getKey());
				fail("Should have thrown an IllegalArgumentException for label descriptor [" + entry.getKey()
						+ "] due to " + entry.getValue());
			} catch (IllegalArgumentException e) {
				// do nothing, test succeeded
			}

		}

	}

}