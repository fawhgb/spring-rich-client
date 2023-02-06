package org.springframework.richclient.exceptionhandling;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;

/**
 * Displays the validation errors to the user.
 *
 * @author Geoffrey De Smet
 * @since 0.3
 */
public class HibernateValidatorDialogExceptionHandler extends AbstractDialogExceptionHandler {

	private static final String CAPTION_KEY = "hibernateValidatorDialogExceptionHandler.caption";
	private static final String EXPLANATION_KEY = "hibernateValidatorDialogExceptionHandler.explanation";

	@Override
	public String resolveExceptionCaption(Throwable throwable) {
		return messageSourceAccessor.getMessage(CAPTION_KEY, CAPTION_KEY);
	}

	@Override
	public Object createExceptionContent(Throwable throwable) {
		if (!(throwable instanceof InvalidStateException)) {
			String ILLEGAL_THROWABLE_ARGUMENT = "Could not handle exception: throwable is not an InvalidStateException:\n"
					+ throwable.getClass().getName();
			logger.error(ILLEGAL_THROWABLE_ARGUMENT);
			return ILLEGAL_THROWABLE_ARGUMENT;
		}
		InvalidStateException invalidStateException = (InvalidStateException) throwable;
		String explanation = messageSourceAccessor.getMessage(EXPLANATION_KEY, EXPLANATION_KEY);
		JPanel panel = new JPanel(new BorderLayout());
		JLabel explanationLabel = new JLabel(explanation);
		panel.add(explanationLabel, BorderLayout.NORTH);
		List<String> invalidValueMessageList = new ArrayList<String>();
		for (InvalidValue invalidValue : invalidStateException.getInvalidValues()) {
			StringBuffer messageBuffer = new StringBuffer();
			String propertyName = invalidValue.getPropertyName();
			messageBuffer.append(messageSourceAccessor.getMessage(propertyName + ".label", propertyName));
			messageBuffer.append(" ");
			messageBuffer.append(invalidValue.getMessage());
			invalidValueMessageList.add(messageBuffer.toString());
		}
		JList invalidValuesJList = new JList(invalidValueMessageList.toArray());
		JScrollPane invalidValuesScrollPane = new JScrollPane(invalidValuesJList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(invalidValuesScrollPane, BorderLayout.CENTER);
		return panel;
	}

}
