package org.springframework.richclient.widget;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.TitlePane;
import org.springframework.richclient.util.GuiStandardUtils;
import org.springframework.richclient.util.RcpSupport;

public abstract class AbstractTitledWidgetForm extends AbstractWidgetForm implements TitledWidget {
	private Message description = new DefaultMessage(
			RcpSupport.getMessage("titledWidget", "defaultMessage", RcpSupport.TEXT), Severity.INFO);

	private TitlePane titlePane = new TitlePane(2);

	public AbstractTitledWidgetForm(FormModel model) {
		super(model);
	}

	public AbstractTitledWidgetForm(FormModel model, String formId) {
		super(model, formId);
	}

	@Override
	public void setTitle(String title) {
		this.titlePane.setTitle(title);
	}

	@Override
	public void setImage(Image image) {
		this.titlePane.setImage(image);
	}

	@Override
	public void setMessage(Message message) {
		if (message != null) {
			titlePane.setMessage(message);
		} else {
			titlePane.setMessage(getDescription());
		}
	}

	protected Message getDescription() {
		return description;
	}

	@Override
	public void setDescription(String longDescription) {
		this.description = new DefaultMessage(longDescription);
		setMessage(this.description);
	}

	@Override
	public JComponent getComponent() {
		JPanel titlePaneContainer = new JPanel(new BorderLayout());
		titlePaneContainer.add(titlePane.getControl());
		titlePaneContainer.add(new JSeparator(), BorderLayout.SOUTH);

		JPanel pageControl = new JPanel(new BorderLayout());
		pageControl.add(titlePaneContainer, BorderLayout.NORTH);
		JComponent content = createFormControl();
		GuiStandardUtils.attachDialogBorder(content);
		pageControl.add(content);

		setMessage(getDescription());

		return pageControl;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.titlePane.addPropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String txt, PropertyChangeListener listener) {
		this.titlePane.addPropertyChangeListener(txt, listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.titlePane.removePropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(String txt, PropertyChangeListener listener) {
		this.titlePane.removePropertyChangeListener(txt, listener);
	}

	@Override
	public boolean canClose() {
		return true;
	}

	@Override
	public java.util.List<? extends AbstractCommand> getCommands() {
		return Collections.emptyList();
	}

	@Override
	public void onAboutToHide() {
	}

	@Override
	public void onAboutToShow() {
	}

	@Override
	public void setCaption(String shortDescription) {
		setTitle(shortDescription);
	}

	@Override
	public void setBeanName(String name) {
		setId(name);
	}
}
