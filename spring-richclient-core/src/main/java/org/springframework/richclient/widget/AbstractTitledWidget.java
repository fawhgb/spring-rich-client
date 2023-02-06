package org.springframework.richclient.widget;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.richclient.dialog.TitlePane;
import org.springframework.richclient.form.ValidationResultsReporter;
import org.springframework.richclient.util.GuiStandardUtils;
import org.springframework.richclient.util.RcpSupport;

public abstract class AbstractTitledWidget extends AbstractWidget implements TitledWidget {

	private Message description = new DefaultMessage(
			RcpSupport.getMessage("titledWidget", "defaultMessage", RcpSupport.TEXT), Severity.INFO);
	private TitlePane titlePane = new TitlePane(1);

	private JComponent component;

	private String id;

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setBeanName(String beanName) {
		setId(beanName);
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
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

	@Override
	public ValidationResultsReporter newSingleLineResultsReporter(Messagable messagable) {
		return null;
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
	public void setCaption(String shortDescription) {
		// TODO needed to comply to interface DescriptionConfigurable where will this
		// end up?
	}

	/**
	 * Lazy creation of component
	 * <p/>
	 * {@inheritDoc}
	 */
	@Override
	public final JComponent getComponent() {
		if (component == null) {
			component = createComponent();
		}

		return component;
	}

	/**
	 * @return JComponent with titlePane, widgetContent and border.
	 */
	private JComponent createComponent() {
		JPanel titlePaneContainer = new JPanel(new BorderLayout());
		titlePaneContainer.add(titlePane.getControl());
		titlePaneContainer.add(new JSeparator(), BorderLayout.SOUTH);

		JPanel pageControl = new JPanel(new BorderLayout());
		pageControl.add(titlePaneContainer, BorderLayout.NORTH);
		JComponent content = createWidgetContent();
		GuiStandardUtils.attachDialogBorder(content);
		pageControl.add(content);

		return pageControl;
	}

	public abstract JComponent createWidgetContent();

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
}
