package org.springframework.richclient.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.binding.validation.support.DefaultValidationMessage;
import org.springframework.binding.validation.support.DefaultValidationResultsModel;
import org.springframework.binding.value.support.AbstractPropertyChangePublisher;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.Messagable;

public class SimpleValidationResultsReporterTests {

	private class SimpleMessagable extends AbstractPropertyChangePublisher implements Messagable {

		private Message message;

		@Override
		public void setMessage(Message message) {
			this.message = message;
		}

		public Message getMessage() {
			return message;
		}
	}

	private class TimeSpecifiedValidationMessage extends DefaultValidationMessage {

		private final long timeStamp;

		public TimeSpecifiedValidationMessage(String property, Severity severity, String message, long timeStamp) {
			super(property, severity, message);
			this.timeStamp = timeStamp;
		}

		@Override
		public long getTimestamp() {
			return timeStamp;
		}
	}

	DefaultValidationResultsModel defaultValidationResultsModel = new DefaultValidationResultsModel();

	SimpleMessagable messagable = new SimpleMessagable();

	SimpleValidationResultsReporter simpleValidationResultsReporter = new SimpleValidationResultsReporter(
			defaultValidationResultsModel, messagable);

	/**
	 * Clear all validation results before testing.
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		defaultValidationResultsModel.clearAllValidationResults();
	}

	/**
	 * Test set error message on messagable.
	 */
	@Test
	public void testErrorMessage() {
		singleMessage(Severity.ERROR);
	}

	/**
	 * Test set warning message on messagable.
	 */
	@Test
	public void testWarningMessage() {
		singleMessage(Severity.WARNING);
	}

	/**
	 * Test set info message on messagable.
	 */
	@Test
	public void testInfoMessage() {
		singleMessage(Severity.INFO);
	}

	private void singleMessage(Severity severity) {
		ValidationMessage message = new DefaultValidationMessage("property", severity, "message");
		defaultValidationResultsModel.addMessage(message);
		assertEquals(message, messagable.getMessage());
	}

	/**
	 * Test sequence of error messages.
	 */
	@Test
	public void testErrorMessageSequence() {
		messageSequence(Severity.ERROR);
	}

	/**
	 * Test sequence of warning messages.
	 */
	@Test
	public void testWarningMessageSequence() {
		messageSequence(Severity.WARNING);
	}

	/**
	 * Test sequence of info messages.
	 */
	@Test
	public void testInfoMessageSequence() {
		messageSequence(Severity.INFO);
	}

	private void messageSequence(Severity severity) {
		ValidationMessage message1 = new TimeSpecifiedValidationMessage("property1", severity, "message1", 1);
		defaultValidationResultsModel.addMessage(message1);
		ValidationMessage message2 = new TimeSpecifiedValidationMessage("property2", severity, "message2", 2);
		defaultValidationResultsModel.addMessage(message2);
		ValidationMessage message3 = new TimeSpecifiedValidationMessage("property3", severity, "message3", 3);
		defaultValidationResultsModel.addMessage(message3);
		ValidationMessage message4 = new TimeSpecifiedValidationMessage("property4", severity, "message4", 4);
		defaultValidationResultsModel.addMessage(message4);

		assertEquals(message4, messagable.getMessage());

		defaultValidationResultsModel.removeMessage(message4);
		assertEquals(message3, messagable.getMessage());

		defaultValidationResultsModel.removeMessage(message2);
		assertEquals(message3, messagable.getMessage());

		defaultValidationResultsModel.removeMessage(message3);
		assertEquals(message1, messagable.getMessage());

		defaultValidationResultsModel.removeMessage(message1);
		assertNull(messagable.getMessage());
	}

	/**
	 * Test random severity message sequence. (first error, then warning and then
	 * info)
	 */
	@Test
	public void testRandomSeverityMessageSequence() {
		ValidationMessage error1 = new TimeSpecifiedValidationMessage("error1", Severity.ERROR, "error1", 1);
		defaultValidationResultsModel.addMessage(error1);

		ValidationMessage warning1 = new TimeSpecifiedValidationMessage("warning1", Severity.WARNING, "warning1", 2);
		defaultValidationResultsModel.addMessage(warning1);

		assertEquals(error1, messagable.getMessage());

		ValidationMessage info1 = new TimeSpecifiedValidationMessage("info1", Severity.INFO, "info1", 3);
		defaultValidationResultsModel.addMessage(info1);

		assertEquals(error1, messagable.getMessage());

		ValidationMessage error2 = new TimeSpecifiedValidationMessage("error2", Severity.ERROR, "error2", 4);
		defaultValidationResultsModel.addMessage(error2);

		assertEquals(error2, messagable.getMessage());
		defaultValidationResultsModel.removeMessage(error2);
		assertEquals(error1, messagable.getMessage());
		defaultValidationResultsModel.removeMessage(error1);
		assertEquals(warning1, messagable.getMessage());
		defaultValidationResultsModel.removeMessage(warning1);
		assertEquals(info1, messagable.getMessage());
		defaultValidationResultsModel.removeMessage(info1);
		assertNull(messagable.getMessage());
	}
}