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
package org.springframework.richclient.form;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.core.Guarded;
import org.springframework.richclient.test.SpringRichTestCase;
import org.springframework.rules.Rules;
import org.springframework.rules.support.DefaultRulesSource;

/**
 * @author Peter De Bruycker
 */
public class FormGuardTests extends SpringRichTestCase {

	private ValidatingFormModel formModel;

	private TestGuarded guarded;

	private static class TestGuarded implements Guarded {

		private boolean enabled = true; // initially enabled

		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	private static class TestBean {
		private String field;
		private String field2;

		public String getField() {
			return field;
		}

		public String getField2() {
			return field2;
		}

		public void setField(String string) {
			field = string;
		}

		public void setField2(String string) {
			field2 = string;
		}
	}

	private static class TestForm extends AbstractForm {
		TestForm() {
			this(new TestBean());
		}

		TestForm(TestBean bean) {
			this(FormModelHelper.createFormModel(bean));
		}

		TestForm(ValidatingFormModel formModel) {
			super(formModel, "testform");
		}

		@Override
		protected JComponent createFormControl() {
			JTextField textControl = (JTextField) getBindingFactory().createBinding("field").getControl();

			// provide a rootpane for the form's control.
			JRootPane rootPane = new JRootPane();
			rootPane.add(textControl);

			return textControl;
		}

		@Override
		protected String getRevertCommandFaceDescriptorId() {
			return "revert";
		}

		@Override
		protected String getCommitCommandFaceDescriptorId() {
			return "commit";
		}

	}

	@Override
	protected void doSetUp() {
		guarded = new TestGuarded();
		TestBean bean = new TestBean();
		bean.setField("ok"); // initialize rule to be valid.
		formModel = FormModelHelper.createFormModel(bean);
		formModel.setEnabled(true);
	}

	/**
	 * May be implemented in subclasses that need to register services with the
	 * global application services instance.
	 */
	@Override
	protected void registerAdditionalServices(DefaultApplicationServices applicationServices) {
		DefaultRulesSource rulesSource = new DefaultRulesSource();
		Rules rules = new Rules(TestBean.class);
		rules.add("field", rules.required());
		rulesSource.addRules(rules);

		applicationServices.setRulesSource(rulesSource);
	}

	@Test
	public void testEnabledState() {
		new FormGuard(formModel, guarded);
		assertTrue(guarded.isEnabled(), "guarded should still be enabled");

		formModel.setEnabled(false);
		assertFalse(guarded.isEnabled(), "guarded should be disabled");

		formModel.setEnabled(true);
		assertTrue(guarded.isEnabled(), "guarded should be enabled");
	}

	@Test
	public void testErrorState() {
		new FormGuard(formModel, guarded);
		assertTrue(guarded.isEnabled(), "guarded should still be enabled");

		formModel.getValueModel("field").setValue(null);
		assertTrue(formModel.getValidationResults().getHasErrors());
		assertFalse(guarded.isEnabled(), "guarded should be disabled");

		formModel.getValueModel("field").setValue("test");
		assertFalse(formModel.getValidationResults().getHasErrors());
		assertTrue(guarded.isEnabled(), "guarded should be enabled");
	}

	// Tests added based on requirements in RCP-39
	// The form's action commands should get enabled/disabled based on the form's
	// state

	@Test
	public void testNewFormCommandGuarding() {
		TestForm form = new TestForm(formModel);
		form.attachFormGuard(guarded, FormGuard.LIKE_NEWFORMOBJCOMMAND);
		Guarded nfCmd = form.getNewFormObjectCommand();

		// default enabled
		assertTrue(guarded.isEnabled(), "guarded like newForm should be initially enabled");
		assertTrue(nfCmd.isEnabled(), "newForm should be initially enabled");

		// disable form --> disabled
		form.setEnabled(false);
		assertFalse(guarded.isEnabled(), "guarded like newform should be disabled with form");
		assertFalse(nfCmd.isEnabled(), "newform should be disabled with form");

		// enable form --> enabled
		form.setEnabled(true);
		assertTrue(guarded.isEnabled(), "guarded like newForm should be initially enabled");
		assertTrue(nfCmd.isEnabled(), "newForm should be initially enabled");

		// trigger validation-error --> still enabled
		((JTextField) form.getControl()).setText(null);
		assertTrue(formModel.getValidationResults().getHasErrors());
		assertTrue(guarded.isEnabled(), "guarded like newForm should be initially enabled");
		assertTrue(nfCmd.isEnabled(), "newForm should be initially enabled");
	}

	@Test
	public void testRevertCommandGuarding() {
		TestForm form = new TestForm(formModel);
		form.attachFormGuard(guarded, FormGuard.LIKE_REVERTCOMMAND);
		Guarded rvtCmd = form.getRevertCommand();

		// initially --> disabled
		assertFalse(guarded.isEnabled(), "guarded like revert should be initially disabled");
		assertFalse(rvtCmd.isEnabled(), "revert should be initially disabled");

		// create dirt with errors --> enabled
		((JTextField) form.getControl()).setText(null);
		assertTrue(formModel.getValidationResults().getHasErrors());
		assertTrue(formModel.isDirty());
		assertTrue(guarded.isEnabled(), "guarded like revert should be enabled when there are changes");
		assertTrue(rvtCmd.isEnabled(), "revert should be enabled when there are changes");

		// relieve errors --> enabled
		((JTextField) form.getControl()).setText("nok");
		assertFalse(formModel.getValidationResults().getHasErrors());
		assertTrue(formModel.isDirty());
		assertTrue(guarded.isEnabled(), "guarded like revert should be enabled when there are changes");
		assertTrue(rvtCmd.isEnabled(), "revert should be enabled when there are changes");

		// disable form --> disabled
		form.setEnabled(false);
		assertFalse(guarded.isEnabled(), "guarded like revert should be disabled with form");
		assertFalse(rvtCmd.isEnabled(), "revert should be disabled with form");

		// enable form --> enabled
		form.setEnabled(true);
		assertTrue(guarded.isEnabled(), "guarded like revert should be enabled with form");
		assertTrue(rvtCmd.isEnabled(), "revert should be enabled with form");

		// revert --> disabled
		form.revert();
		assertFalse(guarded.isEnabled(), "guarded like revert should be disabled when no changes left");
		assertFalse(rvtCmd.isEnabled(), "revert should be disabled when no changes left");
	}

	@Test
	public void testCommitCommandGuarding() {
		TestForm form = new TestForm(formModel);
		new FormGuard(formModel, guarded, FormGuard.LIKE_COMMITCOMMAND);
		Guarded cmtCmd = form.getCommitCommand();

		// initially --> disabled
		assertFalse(guarded.isEnabled(), "guarded like commit should be initially disabled");
		assertFalse(cmtCmd.isEnabled(), "commit should be initially disabled");

		// create dirt with errors --> disabled
		((JTextField) form.getControl()).setText(null);
		assertTrue(formModel.getValidationResults().getHasErrors());
		assertTrue(formModel.isDirty());
		assertFalse(guarded.isEnabled(), "guarded like commit should be disabled when model has validation-errors");
		assertFalse(cmtCmd.isEnabled(), "commit should be disabled when model has validation-errors");

		// relieve errors --> enabled
		((JTextField) form.getControl()).setText("nok");
		assertFalse(formModel.getValidationResults().getHasErrors());
		assertTrue(formModel.isDirty());
		assertTrue(guarded.isEnabled(), "guarded like commit should be enabled if there are errors");
		assertTrue(cmtCmd.isEnabled(), "commit should be enabled if there are errors");

		// disable form --> disabled
		form.setEnabled(false);
		assertFalse(guarded.isEnabled(), "guarded like commit should be disabled with form");
		assertFalse(cmtCmd.isEnabled(), "commit should be disabled with form");

		// enable form --> enabled
		form.setEnabled(true);
		assertTrue(guarded.isEnabled(), "guarded like commit should be enabled with form");
		assertTrue(cmtCmd.isEnabled(), "commit should be enabled with form");

		// revert --> disabled
		form.revert();
		assertFalse(guarded.isEnabled(), "guarded like commit should be disabled when no changes left");
		assertFalse(cmtCmd.isEnabled(), "commit should be disabled when no changes left");
	}
}
