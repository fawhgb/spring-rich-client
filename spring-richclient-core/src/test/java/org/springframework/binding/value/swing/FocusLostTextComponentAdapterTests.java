/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache LicenseVersion 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writingsoftware
 * distributed under the License is distributed on an "AS IS" BASISWITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KINDeither express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.value.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.Test;
import org.springframework.binding.support.TestPropertyChangeListener;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * Test cases for {@link FocusLostTextComponentAdapter}
 * 
 * @author Oliver Hutchison
 */
public class FocusLostTextComponentAdapterTests extends SpringRichTestCase {

	private ValueModel valueModel;

	private TestPropertyChangeListener valueListener;

	private TestableJTextComponent comp;

	@Override
	public void doSetUp() throws Exception {
		valueModel = new ValueHolder("");
		valueListener = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		valueModel.addValueChangeListener(valueListener);
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				comp = new TestableJTextComponent();
			}
		});
		new FocusLostTextComponentAdapter(comp, valueModel);
	}

	@Test
	public void testComponentChangeDoesNotUpdateValueModel() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				comp.setText("newValue");
				assertTrue(!valueModel.getValue().equals("newValue"));
				assertEquals(0, valueListener.eventCount());
			}
		});
	}

	@Test
	public void testValueModelChangeUpdatesComponent() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				valueModel.setValue("newValue");
				assertEquals("newValue", comp.getText());
				assertEquals(1, valueListener.eventCount());
			}
		});
	}

	@Test
	public void testFocusChangeUpdatesValueModel() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				comp.typeText("a");
				assertEquals("", valueModel.getValue());
				assertEquals(0, valueListener.eventCount());

				comp.gainFocus();
				comp.typeText("b");
				assertEquals("", valueModel.getValue());
				assertEquals(0, valueListener.eventCount());

				comp.loseFocus();
				assertEquals("ab", valueModel.getValue());
				assertEquals(1, valueListener.eventCount());
			}
		});
	}
}