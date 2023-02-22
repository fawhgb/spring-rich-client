/*
 * Copyright 2008 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

/**
 * testcase for {@link DefaultViewDescriptor}
 * 
 * @author Peter De Bruycker
 */
public class DefaultViewDescriptorTests {

	@Test
	public void testConstructor() {
		DefaultViewDescriptor descriptor = new DefaultViewDescriptor("theView", TestView.class);

		assertEquals("theView", descriptor.getId());
		assertEquals(TestView.class, descriptor.getViewClass());
	}

	@Test
	public void testViewCreation() {
		DefaultViewDescriptor descriptor = new DefaultViewDescriptor("theView", TestView.class);

		TestView view = (TestView) descriptor.createPageComponent();
		assertNotNull(view);
	}

	@Test
	public void testViewCreationWithProperties() {
		Map<String, Object> viewProperties = new HashMap<String, Object>();
		viewProperties.put("stringProperty", "test value");

		DefaultViewDescriptor descriptor = new DefaultViewDescriptor("theView", TestView.class, viewProperties);

		TestView view = (TestView) descriptor.createPageComponent();
		assertNotNull(view);

		assertEquals("test value", view.getStringProperty());
	}

	@Test
	public void testSetViewClass() throws Exception {
		DefaultViewDescriptor descriptor = new DefaultViewDescriptor();

		descriptor.setId("viewId");

		Class notAViewClass = String.class;

		try {
			descriptor.setViewClass(notAViewClass);
			fail("Must throw exception");
		} catch (IllegalArgumentException e) {
			// test passes
		}

	}

	public static class TestView extends AbstractView {

		private String stringProperty;

		@Override
		protected JComponent createControl() {
			return new JLabel("test");
		}

		public void setStringProperty(String stringProperty) {
			this.stringProperty = stringProperty;
		}

		public String getStringProperty() {
			return stringProperty;
		}

	}
}
