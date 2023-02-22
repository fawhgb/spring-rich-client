/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.value.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.binding.support.BeanPropertyAccessStrategy;
import org.springframework.binding.support.TestBean;
import org.springframework.binding.support.TestPropertyChangeListener;
import org.springframework.binding.value.CommitTrigger;
import org.springframework.binding.value.ValueChangeDetector;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * Tests class {@link BufferedValueModel}.
 * 
 * @author Jeanette Winzenburg
 * @author Karsten Lentzsch
 * @author Oliver Hutchison
 */
public final class BufferedValueModelTests extends SpringRichTestCase {

	private static final Object INITIAL_VALUE = "initial value";
	private static final Object RESET_VALUE = "reset value";

	private ValueModel wrapped;
	private CommitTrigger commitTrigger;

	@Override
	protected void doSetUp() throws Exception {
		wrapped = new ValueHolder(INITIAL_VALUE);
		commitTrigger = new CommitTrigger();
	}

	@Test
	public void testGetWrappedValueModel() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();

		assertSame(wrapped, buffer.getWrappedValueModel());
		assertSame(wrapped, buffer.getInnerMostWrappedValueModel());

		ValueModel nestedValueModel = new AbstractValueModelWrapper(wrapped) {
		};
		buffer = new BufferedValueModel(nestedValueModel);
		assertSame(nestedValueModel, buffer.getWrappedValueModel());
		assertSame(wrapped, buffer.getInnerMostWrappedValueModel());
	}

	@Test
	public void testReturnsWrappedValueIfNoValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		assertEquals(buffer.getValue(), wrapped.getValue(),
				"Buffer value equals the wrapped value before any changes.");

		wrapped.setValue("change1");
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value changes.");

		wrapped.setValue(null);
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value changes.");

		wrapped.setValue("change2");
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value changes.");
	}

	/**
	 * Tests that the BufferedValueModel returns the buffered values once a value
	 * has been assigned.
	 */
	@Test
	public void testReturnsBufferedValueIfValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();

		Object newValue1 = wrapped.getValue();
		buffer.setValue(newValue1);
		assertSame(buffer.getValue(), newValue1, "Buffer value == new value once a value has been assigned.");

		Object newValue2 = "change1";
		buffer.setValue(newValue2);
		assertSame(buffer.getValue(), newValue2, "Buffer value == new value once a value has been assigned.");

		Object newValue3 = null;
		buffer.setValue(newValue3);
		assertSame(buffer.getValue(), newValue3, "Buffer value == new value once a value has been assigned.");

		Object newValue4 = "change2";
		buffer.setValue(newValue4);
		assertSame(buffer.getValue(), newValue4, "Buffer value == new value once a value has been assigned.");
	}

	@Test
	public void testDetectedWrappedValueChangeIfValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();

		Object newValue1 = "change1";
		buffer.setValue(newValue1);
		wrapped.setValue("change3");
		assertSame(buffer.getValue(), "change3", "Buffer value == new value once a value has been assigned.");
		wrapped.setValue(newValue1);
		assertSame(buffer.getValue(), newValue1, "Buffer value == new value once a value has been assigned.");
		wrapped.setValue(null);
		assertSame(buffer.getValue(), null, "Buffer value == new value once a value has been assigned.");
	}

	/**
	 * Tests that the BufferedValueModel returns the wrapped's values after a
	 * commit.
	 */
	@Test
	public void testReturnsWrappedValueAfterCommit() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("change1"); // shall buffer now
		commit();
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value after a commit.");

		wrapped.setValue("change2");
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value after a commit.");

		wrapped.setValue(buffer.getValue());
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value equals the wrapped value after a commit.");
	}

	/**
	 * Tests that the BufferedValueModel returns the wrapped's values after a flush.
	 */
	@Test
	public void testReturnsWrappedValueAfterFlush() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("change1"); // shall buffer now
		revert();
		assertEquals(wrapped.getValue(), buffer.getValue(), "Buffer value equals the wrapped value after a flush.");

		wrapped.setValue("change2");
		assertEquals(wrapped.getValue(), buffer.getValue(), "Buffer value equals the wrapped value after a flush.");
	}

	// Testing Proper Value Commit and Flush **********************************

	/**
	 * Tests the core of the buffering feature: buffer modifications do not affect
	 * the wrapped before a commit.
	 */
	@Test
	public void testWrappedValuesUnchangedBeforeCommit() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		Object oldWrappedValue = wrapped.getValue();
		buffer.setValue("changedBuffer1");
		assertEquals(wrapped.getValue(), oldWrappedValue,
				"Buffer changes do not change the wrapped value before a commit.");
		buffer.setValue(null);
		assertEquals(wrapped.getValue(), oldWrappedValue,
				"Buffer changes do not change the wrapped value before a commit.");
		buffer.setValue(oldWrappedValue);
		assertEquals(wrapped.getValue(), oldWrappedValue,
				"Buffer changes do not change the wrapped value before a commit.");
		buffer.setValue("changedBuffer2");
		assertEquals(wrapped.getValue(), oldWrappedValue,
				"Buffer changes do not change the wrapped value before a commit.");
	}

	/**
	 * Tests the core of a commit: buffer changes are written through on commit and
	 * change the wrapped value.
	 */
	@Test
	public void testCommitChangesWrappedValue() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		Object oldWrappedValue = wrapped.getValue();
		Object newValue1 = "change1";
		buffer.setValue(newValue1);
		assertEquals(wrapped.getValue(), oldWrappedValue, "Wrapped value is unchanged before the first commit.");
		commit();
		assertEquals(wrapped.getValue(), newValue1, "Wrapped value is the new value after the first commit.");

		// Set the buffer to the current wrapped value to check whether
		// the starts buffering, even if there's no value difference.
		Object newValue2 = wrapped.getValue();
		buffer.setValue(newValue2);
		commit();
		assertEquals(wrapped.getValue(), newValue2, "Wrapped value is the new value after the second commit.");
	}

	/**
	 * Tests the core of a flush action: buffer changes are overridden by wrapped
	 * changes after a flush.
	 */
	@Test
	public void testFlushResetsTheBufferedValue() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		Object newValue1 = "new value1";
		buffer.setValue(newValue1);
		assertSame(buffer.getValue(), newValue1, "Buffer value reflects changes before the first flush.");
		revert();
		assertEquals(buffer.getValue(), wrapped.getValue(), "Buffer value is the wrapped value after the first flush.");

		// Set the buffer to the current wrapped value to check whether
		// the starts buffering, even if there's no value difference.
		Object newValue2 = wrapped.getValue();
		buffer.setValue(newValue2);
		assertSame(buffer.getValue(), newValue2, "Buffer value reflects changes before the flush.");
		revert();
		assertEquals(buffer.getValue(), wrapped.getValue(),
				"Buffer value is the wrapped value after the second flush.");
	}

	// Tests a Proper Buffering State *****************************************

	/**
	 * Tests that a buffer isn't buffering as long as no value has been assigned.
	 */
	@Test
	public void testIsNotBufferingIfNoValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		assertFalse(buffer.isBuffering(), "Initially the buffer does not buffer.");

		Object newValue = "change1";
		wrapped.setValue(newValue);
		assertFalse(buffer.isBuffering(), "Wrapped changes do not affect the buffering state.");

		wrapped.setValue(null);
		assertFalse(buffer.isBuffering(), "Wrapped change to null does not affect the buffering state.");
	}

	/**
	 * Tests that the buffer is buffering once a value has been assigned.
	 */
	@Test
	public void testIsBufferingIfValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("change1");
		assertTrue(buffer.isBuffering(), "Setting a value (even the wrapped's value) turns on buffering.");

		buffer.setValue("change2");
		assertTrue(buffer.isBuffering(), "Changing the value doesn't affect the buffering state.");

		buffer.setValue(wrapped.getValue());
		assertTrue(!buffer.isBuffering(), "Resetting the value to the wrapped's value should affect buffering.");
	}

	/**
	 * Tests that the buffer is not buffering after a commit.
	 */
	@Test
	public void testIsNotBufferingAfterCommit() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("change1");
		commit();
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a commit.");

		Object newValue = "change1";
		wrapped.setValue(newValue);
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a commit and wrapped change1.");

		wrapped.setValue(null);
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a commit and wrapped change2.");
	}

	/**
	 * Tests that the buffer is not buffering after a flush.
	 */
	@Test
	public void testIsNotBufferingAfterFlush() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("change1");
		revert();
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a flush.");

		Object newValue = "change1";
		wrapped.setValue(newValue);
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a flush and wrapped change1.");

		wrapped.setValue(null);
		assertFalse(buffer.isBuffering(), "The buffer does not buffer after a flush and wrapped change2.");
	}

	/**
	 * Tests that changing the buffering state fires changes of the <i>buffering</i>
	 * property.
	 */
	@Test
	public void testFiresBufferingChanges() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();

		TestPropertyChangeListener pcl = new TestPropertyChangeListener(BufferedValueModel.BUFFERING_PROPERTY);
		buffer.addPropertyChangeListener(BufferedValueModel.BUFFERING_PROPERTY, pcl);

		assertEquals(0, pcl.eventCount(), "Initial state.");
		buffer.getValue();
		assertEquals(0, pcl.eventCount(), "Reading initial value.");
		buffer.setCommitTrigger(null);
		buffer.setCommitTrigger(commitTrigger);
		assertEquals(0, pcl.eventCount(), "After commit trigger change.");

		buffer.setValue("now buffering");
		assertEquals(1, pcl.eventCount(), "After setting the first value.");
		buffer.setValue("still buffering");
		assertEquals(1, pcl.eventCount(), "After setting the second value.");
		buffer.getValue();
		assertEquals(1, pcl.eventCount(), "Reading buffered value.");

		wrapped.setValue(buffer.getValue());
		assertEquals(2, pcl.eventCount(), "Changing wrapped to same as buffer.");

		commit();
		assertEquals(2, pcl.eventCount(), "After committing.");
		buffer.getValue();
		assertEquals(2, pcl.eventCount(), "Reading unbuffered value.");

		buffer.setValue("buffering again");
		assertEquals(3, pcl.eventCount(), "After second buffering switch.");
		revert();
		assertEquals(4, pcl.eventCount(), "After flushing.");
		buffer.getValue();
		assertEquals(4, pcl.eventCount(), "Reading unbuffered value.");

		buffer.setValue("before real commit");
		assertEquals(5, pcl.eventCount(), "With new change to be committed");
		commit();
		assertEquals(6, pcl.eventCount(), "With new change committed");
	}

	@Test
	public void testSetValueSendsProperValueChangeEvents() {
		Object obj1 = new Integer(1);
		Object obj2a = new Integer(2);
		Object obj2b = new Integer(2);
		testSetValueSendsProperEvents(null, obj1, true);
		testSetValueSendsProperEvents(obj1, null, true);
		testSetValueSendsProperEvents(obj1, obj1, false);
		testSetValueSendsProperEvents(obj1, obj2a, true);
		testSetValueSendsProperEvents(obj2a, obj2b, false);
		testSetValueSendsProperEvents(null, null, false);
	}

	@Test
	public void testValueChangeSendsProperValueChangeEvents() {
		Object obj1 = new Integer(1);
		Object obj2a = new Integer(2);
		Object obj2b = new Integer(2);
		testValueChangeSendsProperEvents(null, obj1, true);
		testValueChangeSendsProperEvents(obj1, null, true);
		testValueChangeSendsProperEvents(obj1, obj1, false);
		testValueChangeSendsProperEvents(obj1, obj2a, true);
		testValueChangeSendsProperEvents(obj2a, obj2b, false);
		testValueChangeSendsProperEvents(null, null, false);

		// Now replace the default value change detector with one that
		// only uses true equivalence.
		ValueChangeDetector oldVCD = (ValueChangeDetector) ApplicationServicesLocator.services()
				.getService(ValueChangeDetector.class);
		getApplicationServices().setValueChangeDetector(new StrictEquivalenceValueChangeDetector());
		testValueChangeSendsProperEvents(null, obj1, true);
		testValueChangeSendsProperEvents(obj1, null, true);
		testValueChangeSendsProperEvents(obj1, obj1, false);
		testValueChangeSendsProperEvents(obj1, obj2a, true);
		testValueChangeSendsProperEvents(obj2a, obj2b, true);
		testValueChangeSendsProperEvents(null, null, false);

		getApplicationServices().setValueChangeDetector(oldVCD);
	}

	// Commit Trigger Tests *************************************************

	/**
	 * Checks that #setCommitTrigger changes the commit trigger.
	 */
	@Test
	public void testCommitTriggerChange() {
		CommitTrigger trigger1 = new CommitTrigger();
		CommitTrigger trigger2 = new CommitTrigger();

		BufferedValueModel buffer = new BufferedValueModel(wrapped, trigger1);
		assertSame(buffer.getCommitTrigger(), trigger1, "Commit trigger has been changed.");

		buffer.setCommitTrigger(trigger2);
		assertSame(buffer.getCommitTrigger(), trigger2, "Commit trigger has been changed.");

		buffer.setCommitTrigger(null);
		assertSame(buffer.getCommitTrigger(), null, "Commit trigger has been changed.");
	}

	/**
	 * Checks and verifies that commit and flush events are driven by the current
	 * commit trigger.
	 */
	@Test
	public void testListensToCurrentCommitTrigger() {
		CommitTrigger trigger1 = new CommitTrigger();
		CommitTrigger trigger2 = new CommitTrigger();

		BufferedValueModel buffer = new BufferedValueModel(wrapped, trigger1);
		buffer.setValue("change1");
		Object wrappedValue = wrapped.getValue();
		Object bufferedValue = buffer.getValue();
		trigger2.commit();
		assertEquals(wrapped.getValue(), wrappedValue,
				"Changing the unrelated trigger2 to commit has no effect on the wrapped.");
		assertSame(buffer.getValue(), bufferedValue,
				"Changing the unrelated trigger2 to commit has no effect on the buffer.");

		trigger2.revert();
		assertEquals(wrapped.getValue(), wrappedValue,
				"Changing the unrelated trigger2 to revert has no effect on the wrapped.");
		assertSame(buffer.getValue(), bufferedValue,
				"Changing the unrelated trigger2 to revert has no effect on the buffer.");

		// Change the commit trigger to trigger2.
		buffer.setCommitTrigger(trigger2);
		assertSame(buffer.getCommitTrigger(), trigger2, "Commit trigger has been changed.");

		trigger1.commit();
		assertEquals(wrapped.getValue(), wrappedValue,
				"Changing the unrelated trigger1 to commit has no effect on the wrapped.");
		assertSame(buffer.getValue(), bufferedValue,
				"Changing the unrelated trigger1 to commit has no effect on the buffer.");

		trigger1.revert();
		assertEquals(wrapped.getValue(), wrappedValue,
				"Changing the unrelated trigger1 to revert has no effect on the wrapped.");
		assertSame(buffer.getValue(), bufferedValue,
				"Changing the unrelated trigger1 to revert has no effect on the buffer.");

		// Commit using trigger2.
		trigger2.commit();
		assertEquals(buffer.getValue(), wrapped.getValue(),
				"Changing the current trigger2 to commit commits the buffered value.");

		buffer.setValue("change2");
		wrappedValue = wrapped.getValue();
		trigger2.revert();
		assertEquals(buffer.getValue(), wrapped.getValue(),
				"Changing the current trigger2 to revert flushes the buffered value.");
		assertEquals(buffer.getValue(), wrappedValue,
				"Changing the current trigger2 to revert flushes the buffered value.");
	}

	// Tests Proper Update Notifications **************************************

	/**
	 * Checks that wrapped changes fire value changes if no value has been assigned.
	 */
	@Test
	public void testPropagatesWrappedChangesIfNoValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		buffer.addValueChangeListener(pcl);

		wrapped.setValue("change1");
		assertEquals(1, pcl.eventCount(), "Value change.");

		wrapped.setValue(null);
		assertEquals(2, pcl.eventCount(), "Value change.");

		wrapped.setValue("change2");
		assertEquals(3, pcl.eventCount(), "Value change.");

		wrapped.setValue(buffer.getValue());
		assertEquals(3, pcl.eventCount(), "No value change.");
	}

	/**
	 * Tests that wrapped changes are propagated once a value has been assigned,
	 * i.e. the buffer is buffering.
	 */
	@Test
	public void testIgnoresWrappedChangesIfValueAssigned() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		buffer.addValueChangeListener(pcl);

		buffer.setValue("new buffer");
		wrapped.setValue("change1");
		assertEquals(2, pcl.eventCount(), "Value change.");

		buffer.setValue("new buffer");
		wrapped.setValue(null);
		assertEquals(4, pcl.eventCount(), "Value change.");

		buffer.setValue("new buffer");
		wrapped.setValue("change2");
		assertEquals(6, pcl.eventCount(), "Value change.");

		buffer.setValue("new buffer");
		wrapped.setValue(buffer.getValue()); // won't fire event
		assertEquals(7, pcl.eventCount(), "No value change.");
	}

	/**
	 * Checks and verifies that a commit fires no value change.
	 */
	@Test
	public void testCommitFiresNoChangeOnSameOldAndNewValues() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		buffer.setValue("value1");
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		buffer.addValueChangeListener(pcl);

		assertEquals(0, pcl.eventCount(), "No initial change.");
		commit();
		assertEquals(0, pcl.eventCount(), "First commit: no change.");

		buffer.setValue("value2");
		assertEquals(1, pcl.eventCount(), "Setting a value: a change.");
		commit();
		assertEquals(1, pcl.eventCount(), "Second commit: no change.");
	}

	@Test
	public void testCommitFiresChangeOnDifferentOldAndNewValues() {
		BufferedValueModel buffer = createDefaultBufferedValueModel(new ToUpperCaseStringHolder());
		buffer.setValue("initialValue");
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		buffer.addValueChangeListener(pcl);
		buffer.setValue("value1");
		assertEquals(1, pcl.eventCount(), "One event fired");
		assertEquals("value1", pcl.lastEvent().getNewValue(), "First value set.");
		commit();
		assertEquals(2, pcl.eventCount(), "Commit fires if the wrapped modifies the value.");
		assertEquals("value1", pcl.lastEvent().getOldValue(), "Old value is the buffered value.");
		assertEquals("VALUE1", pcl.lastEvent().getNewValue(), "New value is the modified value.");
	}

	/**
	 * Tests that a flush event fires a value change if and only if the flushed
	 * value does not equal the buffered value.
	 */
	@Test
	public void testFlushFiresTrueValueChanges() {
		BufferedValueModel buffer = createDefaultBufferedValueModel();
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);

		wrapped.setValue("new wrapped");
		buffer.setValue("new buffer");
		buffer.addValueChangeListener(pcl);
		revert();
		assertEquals(1, pcl.eventCount(), "First flush changes value.");

		buffer.setValue(wrapped.getValue());
		assertEquals(1, pcl.eventCount(), "Resetting value: no change.");
		revert();
		assertEquals(1, pcl.eventCount(), "Second flush: no change.");

		buffer.setValue("new buffer2");
		assertEquals(2, pcl.eventCount(), "Second value change.");
		wrapped.setValue("new wrapped2");
		assertEquals(3, pcl.eventCount(), "Setting new wrapped value: no change.");
		buffer.setValue(wrapped.getValue());
		assertEquals(3, pcl.eventCount(), "Third value change.");
		revert();
		assertEquals(3, pcl.eventCount(), "Third flush: no change.");
	}

	// Misc Tests *************************************************************

	/**
	 * Tests read actions on a read-only model.
	 */
	@Test
	public void testReadOnly() {
		TestBean bean = new TestBean();
		ValueModel readOnlyModel = new BeanPropertyAccessStrategy(bean).getPropertyValueModel("readOnly");
		BufferedValueModel buffer = new BufferedValueModel(readOnlyModel, commitTrigger);

		assertSame(buffer.getValue(), readOnlyModel.getValue(), "Can read values from a read-only model.");

		Object newValue1 = "new value";
		buffer.setValue(newValue1);
		assertSame(buffer.getValue(), newValue1, "Can read values from a read-only model when buffering.");

		revert();
		assertSame(buffer.getValue(), bean.getReadOnly(), "Can read values from a read-only model after a flush.");

		buffer.setValue("new value2");
		try {
			commit();
			fail("Cannot commit to a read-only model.");
		} catch (Exception e) {
			// The expected behavior
		}
	}

	// Test Implementations ***************************************************

	@Test
	private void testSetValueSendsProperEvents(Object oldValue, Object newValue, boolean eventExpected) {
		BufferedValueModel valueModel = new BufferedValueModel(new ValueHolder(oldValue), new CommitTrigger());
		testSendsProperEvents(valueModel, oldValue, newValue, eventExpected);
	}

	@Test
	private void testValueChangeSendsProperEvents(Object oldValue, Object newValue, boolean eventExpected) {
		BufferedValueModel defaultModel = createDefaultBufferedValueModel();
		defaultModel.setValue(oldValue);
		testSendsProperEvents(defaultModel, oldValue, newValue, eventExpected);
	}

	@Test
	private void testSendsProperEvents(BufferedValueModel valueModel, Object oldValue, Object newValue,
			boolean eventExpected) {
		TestPropertyChangeListener pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
		valueModel.addValueChangeListener(pcl);
		int expectedEventCount = eventExpected ? 1 : 0;

		valueModel.setValue(newValue);
		assertEquals(expectedEventCount, pcl.eventCount(),
				"Expected event count after ( " + oldValue + " -> " + newValue + ").");
		if (eventExpected) {
			assertEquals(oldValue, pcl.lastEvent().getOldValue(), "Event's old value.");
			assertEquals(newValue, pcl.lastEvent().getNewValue(), "Event's new value.");
		}
	}

	// Helper Code ************************************************************

	private void commit() {
		commitTrigger.commit();
	}

	private void revert() {
		commitTrigger.revert();
	}

	private BufferedValueModel createDefaultBufferedValueModel() {
		wrapped.setValue(RESET_VALUE);
		return new BufferedValueModel(wrapped, commitTrigger);
	}

	private BufferedValueModel createDefaultBufferedValueModel(ValueModel wrapped) {
		wrapped.setValue(RESET_VALUE);
		return new BufferedValueModel(wrapped, commitTrigger);
	}

	// A String typed ValueModel that modifies set values to uppercase.
	private static class ToUpperCaseStringHolder extends AbstractValueModel {

		private String text;

		@Override
		public Object getValue() {
			return text;
		}

		@Override
		public void setValue(Object newValue) {
			String newText = ((String) newValue).toUpperCase();
			Object oldText = text;
			text = newText;
			fireValueChange(oldText, newText);
		}

	}

	/**
	 * This class is used to test alternate value change detection methods.
	 */
	private static class StrictEquivalenceValueChangeDetector implements ValueChangeDetector {
		@Override
		public boolean hasValueChanged(Object oldValue, Object newValue) {
			return oldValue != newValue;
		}
	}

}
