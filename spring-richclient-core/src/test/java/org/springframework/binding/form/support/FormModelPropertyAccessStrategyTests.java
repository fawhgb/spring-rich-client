package org.springframework.binding.form.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.binding.PropertyAccessStrategy;
import org.springframework.binding.PropertyMetadataAccessStrategy;
import org.springframework.binding.support.TestBean;

public class FormModelPropertyAccessStrategyTests {

	protected AbstractFormModel getFormModel(Object formObject) {
		return new TestAbstractFormModel(formObject);
	}

	/**
	 * Test to ensure that the AccessStrategy works correctly with
	 * writeable/readable properties. *
	 */
	@Test
	public void testReadOnlyPropertyAccess() {
		AbstractFormModel model = getFormModel(new TestBean());
		PropertyAccessStrategy propertyAccessStrategy = model.getPropertyAccessStrategy();
		PropertyMetadataAccessStrategy metaDataAccessStrategy = propertyAccessStrategy.getMetadataAccessStrategy();

		assertFalse(metaDataAccessStrategy.isWriteable("readOnly"),
				"Property is readonly, isWriteable() should return false.");
		assertTrue(metaDataAccessStrategy.isReadable("readOnly"),
				"Property is readonly, isReadable() should return true.");
	}
}
