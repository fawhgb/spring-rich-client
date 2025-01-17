package org.springframework.richclient.taskpane;

import javax.swing.AbstractButton;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;

import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.config.CommandFaceDescriptor;
import org.springframework.richclient.command.config.DefaultCommandButtonConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class JTaskPaneCommandButtonConfigurer extends DefaultCommandButtonConfigurer {
	@Override
	public void configure(AbstractButton button, AbstractCommand command, CommandFaceDescriptor faceDescriptor) {

		Assert.notNull(button, "The button to configure cannot be null.");
		Assert.notNull(faceDescriptor, "The command face descriptor cannot be null.");

		if (StringUtils.hasText(faceDescriptor.getDescription())) {
			button.setText(faceDescriptor.getDescription());
		} else {
			button.setText(faceDescriptor.getText());
		}

		button.setToolTipText(faceDescriptor.getCaption());

		if (faceDescriptor.getLargeIcon() != null) {
			faceDescriptor.configureIconInfo(button, true);
		} else {
			faceDescriptor.configureIcon(button);
		}
		try {
			button.setUI((ButtonUI) Class.forName((String) UIManager.get("LinkButtonUI")).newInstance());
		} catch (Exception e) {

		}
	}
}
