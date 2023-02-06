package org.springframework.binding.form.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.support.AbstractPropertyChangePublisher;

/**
 * FieldMetadata implementation for read-only properties
 */
public class ReadOnlyFieldMetadata extends AbstractPropertyChangePublisher implements FieldMetadata {

	private Class propertyType;

	private boolean enabled = true;

	private boolean oldEnabled;

	private FormModel formModel;

	private final PropertyChangeListener formChangeHandler = new FormModelChangeHandler();

	private final Map userMetadata = new HashMap();

	public ReadOnlyFieldMetadata(FormModel formModel, Class propertyType) {
		this(formModel, propertyType, null);
	}

	public ReadOnlyFieldMetadata(FormModel formModel, Class propertyType, Map userMetadata) {
		this.propertyType = propertyType;
		this.formModel = formModel;
		this.formModel.addPropertyChangeListener(ENABLED_PROPERTY, formChangeHandler);
		if (userMetadata != null) {
			this.userMetadata.putAll(userMetadata);
		}
	}

	@Override
	public Map getAllUserMetadata() {
		return userMetadata;
	}

	@Override
	public Class getPropertyType() {
		return propertyType;
	}

	@Override
	public Object getUserMetadata(String key) {
		return userMetadata.get(key);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return enabled && formModel.isEnabled();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		firePropertyChange(ENABLED_PROPERTY, oldEnabled, isEnabled());
		oldEnabled = isEnabled();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
	}

	/**
	 * Responsible for listening for changes to the enabled property of the
	 * FormModel
	 */
	private class FormModelChangeHandler implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (FormModel.ENABLED_PROPERTY.equals(evt.getPropertyName())) {
				firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(isEnabled()));
				oldEnabled = isEnabled();
			}
		}
	}
}