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
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * testcase for {@link SimpleViewDescriptor}
 * 
 * @author Peter De Bruycker
 */
public class SimpleViewDescriptorTests {

	private AbstractView view;

	@Test
	public void testConstructor() {
		SimpleViewDescriptor descriptor = new SimpleViewDescriptor("id", view);
		assertEquals("id", descriptor.getId());
		assertEquals(descriptor, view.getDescriptor());
	}

	@BeforeEach
	protected void setUp() throws Exception {
		view = new AbstractView() {

			@Override
			protected JComponent createControl() {
				return new JLabel("hello");
			}
		};

	}

	@Test
	public void testCreatePageComponent() {
		SimpleViewDescriptor descriptor = new SimpleViewDescriptor("id", view);

		assertSame(view, descriptor.createPageComponent());
		// must always return the same
		assertSame(view, descriptor.createPageComponent());
	}

}
