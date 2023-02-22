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
package org.springframework.richclient.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JTextField;

import org.junit.jupiter.api.Test;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.test.SpringRichTestCase;
import org.springframework.rules.Rules;
import org.springframework.rules.support.DefaultRulesSource;

/**
 * @author Peter De Bruycker
 */
public class InputApplicationDialogTests extends SpringRichTestCase {
	private static final String BUSINESS_FIELD = "name";

	private BusinessObject businessObject;

	@Override
	protected void doSetUp() {
		// create business object
		businessObject = new BusinessObject();
	}

	@Test
	public void testInitialEnabledState() {
		InputApplicationDialog createDialog = createDialog(businessObject, BUSINESS_FIELD);
		assertEnabledStateReflectsFormModelState(createDialog);

		businessObject.setName("test");
		assertEnabledStateReflectsFormModelState(createDialog);
	}

	private void assertEnabledStateReflectsFormModelState(InputApplicationDialog dialog) {
		assertEquals(dialog.getFormModel().getValidationResults().getHasErrors(), !dialog.isEnabled());
	}

	private InputApplicationDialog createDialog(BusinessObject businessObject, String field) {
		InputApplicationDialog dialog = new InputApplicationDialog(businessObject, BUSINESS_FIELD);
		dialog.createDialog();

		return dialog;
	}

	@Test
	public void testConstructor() {
		InputApplicationDialog dialog = new InputApplicationDialog(businessObject, BUSINESS_FIELD);

		assertNotNull(dialog.getFormModel(), "No FormModel created");
		assertEquals(businessObject, dialog.getFormModel().getFormObject(), "BusinessObject not set on FormModel");

		assertNotNull(dialog.getInputField(), "No inputField created");
		assertTrue(dialog.getInputField() instanceof JTextField, "Default inputField not a JTextField");
	}

	@Test
	public void testEnabledByUserAction() {
		InputApplicationDialog dialog = createDialog(businessObject, BUSINESS_FIELD);

		assertFalse(dialog.isEnabled());

		dialog.getFormModel().getValueModel(BUSINESS_FIELD).setValue("test");
		assertEnabledStateReflectsFormModelState(dialog);

		dialog.getFormModel().getValueModel(BUSINESS_FIELD).setValue("");
		assertEnabledStateReflectsFormModelState(dialog);
	}

	/**
	 * May be implemented in subclasses that need to register services with the
	 * global application services instance.
	 */
	@Override
	protected void registerAdditionalServices(DefaultApplicationServices applicationServices) {
		applicationServices.setRulesSource(new BusinessRulesSource());
	}

	private class BusinessRulesSource extends DefaultRulesSource {
		public BusinessRulesSource() {
			Rules rules = new Rules(BusinessObject.class);
			rules.add(BUSINESS_FIELD, rules.required());
			addRules(rules);
		}
	}

	public class BusinessObject {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}