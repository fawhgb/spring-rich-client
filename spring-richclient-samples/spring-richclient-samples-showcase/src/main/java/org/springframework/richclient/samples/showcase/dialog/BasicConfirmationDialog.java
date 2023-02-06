package org.springframework.richclient.samples.showcase.dialog;

import org.springframework.richclient.dialog.ConfirmationDialog;

/**
 * Shows the usage of the confirmation dialog.
 *
 * @author Jan Hoskens
 *
 */
public class BasicConfirmationDialog extends ConfirmationDialog {

	@Override
	protected void onConfirm() {
		logger.info("BasicConfirmationDialog was confirmed");
	}
}