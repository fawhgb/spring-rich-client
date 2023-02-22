/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.richclient.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.richclient.application.ApplicationPage;

/**
 * Abstract base testcase for {@link ApplicationPage} implementations.
 * 
 * @author Peter De Bruycker
 */
public abstract class AbstractApplicationPageTestCase {

	private AbstractApplicationPage applicationPage;
	private TestView testView1;
	private TestView testView2;

	@BeforeEach
	protected void setUp() throws Exception {
		setUpViews();

		applicationPage = (AbstractApplicationPage) createApplicationPage();
		assertNotNull(applicationPage, "createApplicationPage returns null");

		SimpleViewDescriptorRegistry viewDescriptorRegistry = new SimpleViewDescriptorRegistry();
		viewDescriptorRegistry.addViewDescriptor(new SimpleViewDescriptor("testView1", testView1));
		viewDescriptorRegistry.addViewDescriptor(new SimpleViewDescriptor("testView2", testView2));

		applicationPage.setViewDescriptorRegistry(viewDescriptorRegistry);

		applicationPage.setPageComponentPaneFactory(new SimplePageComponentPaneFactory());

		applicationPage.setDescriptor(new EmptyPageDescriptor());

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				// trigger control creation
				JComponent control = applicationPage.getControl();
				assertNotNull(control, "getControl cannot return null");
			}
		});
	}

	private void setUpViews() {
		testView1 = new TestView("this is test view 1");
		testView2 = new TestView("this is test view 2");
	}

	protected abstract ApplicationPage createApplicationPage();

	@Test
	public void testShowViewAndClose() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				assertNull(applicationPage.getView("testView1"));

				applicationPage.showView("testView1");

				TestView view = (TestView) applicationPage.getView("testView1");

				assertNotNull(view);
				assertEquals("testView1", view.getId());

				applicationPage.close(view);
				assertNull(applicationPage.getView("testView1"));
			}
		});
	}

	@Test
	public void testShowViewWithInput() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				Object input = "the input";

				applicationPage.showView("testView1", input);

				TestView view = applicationPage.getView("testView1");
				assertNotNull(view);

				assertTrue(view.isSetInputCalled());
				assertEquals(input, view.getInput());
			}
		});
	}

	@Test
	public void testShowView() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				assertSame(testView1, applicationPage.showView("testView1"));
				assertSame(testView1, applicationPage.getActiveComponent());

				assertSame(testView2, applicationPage.showView("testView2"));
				assertSame(testView2, applicationPage.getActiveComponent());
			}
		});
	}

	@Test
	public void testShowViewWithoutInput() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				applicationPage.showView("testView1");

				TestView view = applicationPage.getView("testView1");
				assertNotNull(view);

				assertFalse(view.isSetInputCalled());
			}
		});
	}

	@Test
	public void testGetView() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				assertNull(applicationPage.getView("testView1"));

				applicationPage.showView("testView1");

				TestView view = applicationPage.getView("testView1");

				assertNotNull(view);
				assertEquals("testView1", view.getId());

				applicationPage.close(view);
				assertNull(applicationPage.getView("testView1"));
			}
		});
	}

	private static class TestView extends AbstractView {

		private String label;
		private Object input;
		private boolean setInputCalled;

		public TestView(String label) {
			this.label = label;
		}

		@Override
		protected JComponent createControl() {
			return new JLabel(label);
		}

		@Override
		public void setInput(Object input) {
			this.input = input;
			setInputCalled = true;
		}

		public Object getInput() {
			return input;
		}

		public boolean isSetInputCalled() {
			return setInputCalled;
		}

	}
}
