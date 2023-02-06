package org.springframework.richclient.form.binding.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.components.TimeTextField;
import org.springframework.richclient.form.binding.support.CustomBinding;

@SuppressWarnings("unchecked")
public final class TimeBinding extends CustomBinding implements PropertyChangeListener {

	private final TimeTextField field;

	public TimeBinding(FormModel model, String path, Class requiredSourceClass, TimeTextField field) {
		super(model, path, requiredSourceClass);
		this.field = field;
	}

	@Override
	protected void valueModelChanged(Object newValue) {
		field.setValue(newValue);
		readOnlyChanged();
	}

	@Override
	protected JComponent doBindControl() {
		field.setValue(getValue());
		field.addPropertyChangeListener("value", this);
		return field;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		controlValueChanged(field.getValue());
	}

	@Override
	protected void readOnlyChanged() {
		field.setEditable(isEnabled() && !isReadOnly());
	}

	@Override
	protected void enabledChanged() {
		field.setEnabled(isEnabled());
		readOnlyChanged();
	}
}
