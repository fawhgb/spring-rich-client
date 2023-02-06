package org.springframework.richclient.form.binding.swing;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.binding.support.CustomBinding;

@SuppressWarnings("unchecked")
public class SpinnerBinding extends CustomBinding implements ChangeListener {
	private final JSpinner spinner;

	public SpinnerBinding(FormModel formModel, String propertyPath, Class numberClass, JSpinner spinner) {
		super(formModel, propertyPath, numberClass);
		this.spinner = spinner;
	}

	@Override
	protected JComponent doBindControl() {
		spinner.setValue(getValue());
		spinner.getModel().addChangeListener(this);
		return this.spinner;
	}

	@Override
	protected void readOnlyChanged() {
		spinner.setEnabled(!isReadOnly() && isEnabled());
	}

	@Override
	protected void enabledChanged() {
		readOnlyChanged();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		controlValueChanged(this.spinner.getValue());
	}

	@Override
	protected void valueModelChanged(Object newValue) {
		this.spinner.setValue(newValue);
	}

}
