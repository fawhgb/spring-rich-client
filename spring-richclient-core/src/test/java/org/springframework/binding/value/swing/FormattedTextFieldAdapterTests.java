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
package org.springframework.binding.value.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.Test;
import org.springframework.binding.support.TestPropertyChangeListener;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * Test cases for {@link FormattedTextFieldAdapter}
 * 
 * @author Oliver Hutchison
 */
public class FormattedTextFieldAdapterTests extends SpringRichTestCase {

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
		comp.setFormatterFactory(new OnlyAlowLowerCaseFormatterFactory());
		new FormattedTextFieldAdapter(comp, valueModel, ValueCommitPolicy.AS_YOU_TYPE);
	}

	@Test
	public void testComponentChangeUpdatesValueModel() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				comp.setValue("newvalue");
				assertEquals("newvalue", valueModel.getValue());
				assertEquals(1, valueListener.eventCount());

				comp.setText("newervalue");
				assertEquals("newervalue", valueModel.getValue());
				assertEquals(3, valueListener.eventCount());
			}
		});
	}

	@Test
	public void testValueModelChangeUpdatesComponent() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				valueModel.setValue("newvalue");
				assertEquals("newvalue", comp.getValue());
				assertEquals(1, valueListener.eventCount());
			}
		});
	}

	@Test
	public void testTypingUpdatesValueModel() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				comp.typeText("a");
				assertEquals("a", valueModel.getValue());
				assertEquals(1, valueListener.eventCount());

				valueListener.reset();
				comp.typeText("bc");
				assertEquals("abc", valueModel.getValue());
				assertEquals(2, valueListener.eventCount());

				valueListener.reset();
				comp.setCaretPosition(1);
				comp.typeText("d");
				assertEquals("adbc", valueModel.getValue());
				assertEquals(1, valueListener.eventCount());

				// type an invalid char
				valueListener.reset();
				comp.setCaretPosition(1);
				comp.typeText("E");
				assertEquals("adbc", valueModel.getValue());
				assertEquals(0, valueListener.eventCount());

				valueListener.reset();
				comp.setCaretPosition(2);
				comp.typeBackSpace();
				comp.typeText("f");
				assertEquals("afdbc", valueModel.getValue());
				assertEquals(1, valueListener.eventCount());
			}
		});
	}

	private static final class OnlyAlowLowerCaseFormatterFactory extends AbstractFormatterFactory {
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf) {
			return new AbstractFormatter() {

				@Override
				public Object stringToValue(String text) throws ParseException {
					if (text != null && !text.equals(text.toLowerCase())) {
						throw new ParseException(text, 0);
					}
					return text;
				}

				@Override
				public String valueToString(Object value) throws ParseException {
					return (String) value;
				}
			};
		}
	}
}