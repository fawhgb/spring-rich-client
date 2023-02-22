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
package org.springframework.richclient.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.richclient.application.ViewDescriptor;

/**
 * Provides a suite of unit tests for the
 * {@link BeanFactoryViewDescriptorRegistry} class.
 *
 * @author Kevin Stembridge
 * @since 0.3
 *
 */
public class BeanFactoryViewDescriptorRegistryTests {

	/**
	 * Test method for
	 * {@link BeanFactoryViewDescriptorRegistry#getViewDescriptor(java.lang.String)}.
	 */
	@Test
	public final void testGetViewDescriptor() {

		BeanFactoryViewDescriptorRegistry registry = new BeanFactoryViewDescriptorRegistry();
		StaticApplicationContext appCtx = new StaticApplicationContext();
		registry.setApplicationContext(appCtx);

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue("viewClass", NullView.class);
		appCtx.registerSingleton("view1", DefaultViewDescriptor.class, mpv);
		appCtx.registerSingleton("view2", DefaultViewDescriptor.class, mpv);
		appCtx.registerSingleton("bogusView", String.class);

		assertNotNull(registry.getViewDescriptor("view1"));
		assertNotNull(registry.getViewDescriptor("view2"));

		assertNull(registry.getViewDescriptor("bogus"), "Should return null when viewName not found");

		try {
			registry.getViewDescriptor("bogusView");
			fail("Should have thrown BeanNotOfRequiredTypeException");
		} catch (BeanNotOfRequiredTypeException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Performs the following assertions on the
	 * {@link BeanFactoryViewDescriptorRegistry#getViewDescriptors()} method:
	 * 
	 * <ul>
	 * <li>The method does not return null if there are no view descriptors in the
	 * underlying registry</li>
	 * <li>The correct number of descriptors are returned.</li>
	 * </ul>
	 */
	@Test
	public void testGetViewDescriptors() {

		BeanFactoryViewDescriptorRegistry registry = new BeanFactoryViewDescriptorRegistry();
		StaticApplicationContext appCtx = new StaticApplicationContext();
		registry.setApplicationContext(appCtx);

		ViewDescriptor[] viewDescriptors = registry.getViewDescriptors();

		assertNotNull(viewDescriptors, "View descriptor array should never be null");
		assertEquals(0, viewDescriptors.length, "Should be no view descriptors in the array");

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue("viewClass", NullView.class);
		appCtx.registerSingleton("view1", DefaultViewDescriptor.class, mpv);
		appCtx.registerSingleton("view2", DefaultViewDescriptor.class, mpv);

		viewDescriptors = registry.getViewDescriptors();
		assertEquals(2, viewDescriptors.length, "Should be 2 view descriptors in the array");

	}

	/**
	 * Confirms that an IllegalArgumentException is thrown if a null viewName is
	 * passed to the
	 * {@link BeanFactoryViewDescriptorRegistry#getViewDescriptor(String)} method.
	 */
	@Test
	public void testForNullViewId() {

		try {
			new BeanFactoryViewDescriptorRegistry().getViewDescriptor(null);
			fail("Should have thrown an IllegalArgumentException for null view ID");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	private class NullView extends AbstractView {

		@Override
		protected JComponent createControl() {
			return new JPanel();
		}
	}

}
