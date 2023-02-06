package org.springframework.richclient.samples.dataeditor.ui;

import java.awt.Dimension;
import java.util.Map;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.binding.swing.editor.AbstractLookupBinder;
import org.springframework.richclient.form.binding.swing.editor.AbstractLookupBinding;
import org.springframework.richclient.samples.dataeditor.domain.Supplier;
import org.springframework.richclient.samples.dataeditor.domain.SupplierFilter;

public class SupplierBinder extends AbstractLookupBinder {
	public SupplierBinder() {
		super("supplierDataEditor");
	}

	@Override
	protected AbstractLookupBinding getLookupBinding(FormModel formModel, String formPropertyPath, Map context) {
		return new AbstractLookupBinding(getDataEditor(), formModel, formPropertyPath) {
			@Override
			public String getObjectLabel(Object o) {
				return ((Supplier) o).getName();
			}

			@Override
			protected Object createFilterFromString(String textFieldValue) {
				SupplierFilter s = new SupplierFilter();
				s.setNameContains(textFieldValue);
				return s;
			}

			@Override
			public Dimension getDialogSize() {
				return new Dimension(800, 600);
			}
		};
	}
}
