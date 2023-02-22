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
package org.springframework.richclient.dialog.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Guarded;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.AbstractDialogPage;
import org.springframework.richclient.dialog.DefaultMessageAreaModel;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.TitlePane;

/**
 * Tests for {@link DialogPageUtils}.
 * 
 * @author Larry Streepy
 */
public class DialogPageUtilsTests {

	@BeforeEach
	public void setUp() {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"org/springframework/richclient/dialog/support/generic-application-ctx.xml");
		Application.load(null);
		Application app = new Application(new DefaultApplicationLifecycleAdvisor());
		app.setApplicationContext(applicationContext);
	}

	@Test
	public void testMessageMonitor() {
		DialogPage page = new TestDialogPage();
		TestMessagable monitor = new TestMessagable();
		DialogPageUtils.addMessageMonitor(page, monitor);

		monitor.resetMessageCount();
		page.setMessage(new DefaultMessage("a message"));
		assertEquals(monitor.getMessage().getMessage(), "a message", "Message text not equal");

		page.setMessage(new DefaultMessage("another message"));
		assertEquals(monitor.getMessage().getMessage(), "another message", "Message text not equal");

		assertEquals(2, monitor.getMessageCount(), "Message count incorrect");
	}

	@Test
	public void testPageCompleteAdapter() {
		TestDialogPage page = new TestDialogPage();
		Guarded guarded = new TestGuarded();

		DialogPageUtils.adaptPageCompletetoGuarded(page, guarded);

		page.setPageComplete(false);
		assertFalse(guarded.isEnabled(), "guarded should be disabled");

		page.setPageComplete(true); // Change it
		assertTrue(guarded.isEnabled(), "guarded should be enabled");
	}

	@Test
	public void testStandardViewAdaptsOkCommand() {
		TestDialogPage page = new TestDialogPage();

		ActionCommand okCommand = new ActionCommand("okCommand") {
			@Override
			protected void doExecuteCommand() {
			}
		};
		ActionCommand cancelCommand = new ActionCommand("cancelCommand") {
			@Override
			protected void doExecuteCommand() {
			}
		};

		DialogPageUtils.createStandardView(page, okCommand, cancelCommand);

		page.setPageComplete(false);
		assertFalse(okCommand.isEnabled(), "okCommand should be disabled");

		page.setPageComplete(true);
		assertTrue(okCommand.isEnabled(), "okCommand should be enabled");
	}

	@Test
	public void testCreateTitlePane() {
		TestDialogPage page = new TestDialogPage();

		TitlePane titlePane = DialogPageUtils.createTitlePane(page);

		page.setMessage(new DefaultMessage("test message"));
		assertEquals(titlePane.getMessage().getMessage(), "test message", "Message text not equal");
	}

	/**
	 * Class to provide a concrete DialogPage for testing.
	 */
	private class TestDialogPage extends AbstractDialogPage {

		public TestDialogPage() {
			super("MyPageId");
		}

		@Override
		protected JComponent createControl() {
			return new JLabel();
		}
	}

	/**
	 * Class to inspect Messagable operations.
	 */
	private class TestMessagable extends DefaultMessageAreaModel {

		private int msgCount = 0;

		@Override
		public void setMessage(Message message) {
			msgCount += 1;
			super.setMessage(message);
		}

		public int getMessageCount() {
			return msgCount;
		}

		public void resetMessageCount() {
			msgCount = 0;
		}
	}

	/**
	 * Class to inspect Guarded operations.
	 */
	private class TestGuarded implements Guarded {
		private boolean enabled = false;

		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
