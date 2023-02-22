package org.springframework.richclient.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.Collator;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.richclient.form.binding.swing.ComboBoxBinder;

public class DefaultFormUIProviderTests {
	private SimplePanel panel;
	private Map context;

	@Test
	public void testGetComponent() {
		DefaultFormUIProvider formUIProvider = new DefaultFormUIProvider(panel);

		assertSame(panel, formUIProvider.getControl());
		assertEquals(panel.getStringField(), formUIProvider.getComponent("stringProperty"));
		assertEquals(panel.getComboBox(), formUIProvider.getComponent("comboProperty"));
		assertEquals(panel.getCheckBox(), formUIProvider.getComponent("booleanProperty"));

		// find nested components
		assertEquals(panel.getNestedField(), formUIProvider.getComponent("nestedField"));
	}

	@Test
	public void testBind() {
		DefaultFormUIProvider formUIProvider = new DefaultFormUIProvider(panel);

		TestableBindingFactory bindingFactory = new TestableBindingFactory();

		String[] properties = { "stringProperty", "comboProperty", "booleanProperty" };
		formUIProvider.setProperties(properties);

		formUIProvider.setContext("comboProperty", context);

		formUIProvider.bind(bindingFactory, null);
		assertEquals(3, bindingFactory.getBindControlCount());

		// string property
		assertEquals("stringProperty", bindingFactory.getPropertyPaths().get(0));
		assertEquals(panel.getStringField(), bindingFactory.getControls().get(0));
		assertEquals(Collections.EMPTY_MAP, bindingFactory.getContexts().get(0));

		// combo property
		assertEquals("comboProperty", bindingFactory.getPropertyPaths().get(1));
		assertEquals(panel.getComboBox(), bindingFactory.getControls().get(1));
		assertEquals(context, bindingFactory.getContexts().get(1));

		// boolean property
		assertEquals("booleanProperty", bindingFactory.getPropertyPaths().get(2));
		assertEquals(panel.getCheckBox(), bindingFactory.getControls().get(2));
		assertEquals(Collections.EMPTY_MAP, bindingFactory.getContexts().get(2));
	}

	@BeforeEach
	protected void setUp() throws Exception {
		panel = new SimplePanel();

		context = Collections.singletonMap(ComboBoxBinder.COMPARATOR_KEY, Collator.getInstance());
	}

	@Test
	public void testSetAndGetContext() {
		DefaultFormUIProvider formUIProvider = new DefaultFormUIProvider(panel);

		formUIProvider.setContext("comboProperty", context);

		assertEquals(context, formUIProvider.getContext("comboProperty"));

		assertNotNull(formUIProvider.getContext("stringProperty"), "if no context provided, must return empty map");
		assertTrue(formUIProvider.getContext("stringProperty").isEmpty(),
				"if no context provided, must return empty map");
	}
}
