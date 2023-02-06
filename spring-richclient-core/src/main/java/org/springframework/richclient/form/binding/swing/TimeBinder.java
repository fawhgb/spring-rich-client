package org.springframework.richclient.form.binding.swing;

import java.util.Date;
import java.util.Map;

import javax.swing.JComponent;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.components.TimeTextField;
import org.springframework.richclient.form.binding.Binding;
import org.springframework.richclient.form.binding.support.AbstractBinder;

@SuppressWarnings("unchecked")
public class TimeBinder extends AbstractBinder {

	public TimeBinder() {
		super(Date.class);
	}

	@Override
	protected JComponent createControl(Map context) {
		return new TimeTextField();
	}

	@Override
	protected Binding doBind(JComponent control, FormModel formModel, String formPropertyPath, Map context) {
		return new TimeBinding(formModel, formPropertyPath, Date.class, (TimeTextField) control);
	}

}
