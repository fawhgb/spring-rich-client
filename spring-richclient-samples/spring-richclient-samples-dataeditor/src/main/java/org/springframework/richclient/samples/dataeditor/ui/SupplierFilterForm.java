package org.springframework.richclient.samples.dataeditor.ui;

import javax.swing.JComponent;

import org.springframework.richclient.form.FilterForm;
import org.springframework.richclient.form.builder.FormLayoutFormBuilder;
import org.springframework.richclient.samples.dataeditor.domain.Supplier;
import org.springframework.richclient.samples.dataeditor.domain.SupplierFilter;

import com.jgoodies.forms.layout.FormLayout;

public class SupplierFilterForm extends FilterForm {
	public SupplierFilterForm() {
		super("supplierFilterForm");
	}

	@Override
	protected Object newFormObject() {
		return new SupplierFilter();
	}

	@Override
	public void setFormObject(Object formObject) {
		if (formObject instanceof Supplier) {
			super.setFormObject(SupplierFilter.fromSupplier((Supplier) formObject));
		} else {
			super.setFormObject(formObject);
		}
	}

	@Override
	protected JComponent createFormControl() {
		FormLayout layout = new FormLayout("default, 3dlu, fill:pref:nogrow", "default");
		FormLayoutFormBuilder builder = new FormLayoutFormBuilder(getBindingFactory(), layout);
		builder.addPropertyAndLabel("nameContains");
		builder.nextRow();
		builder.addPropertyAndLabel("contactNameContains");
		return builder.getPanel();
	}
}
