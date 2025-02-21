package org.springframework.richclient.samples.dataeditor.ui;

import javax.swing.JComponent;

import org.springframework.richclient.form.FilterForm;
import org.springframework.richclient.form.builder.FormLayoutFormBuilder;
import org.springframework.richclient.samples.dataeditor.domain.ItemFilter;

import com.jgoodies.forms.layout.FormLayout;

public class ItemFilterForm extends FilterForm {
	public ItemFilterForm() {
		super("itemFilterForm");
	}

	@Override
	protected Object newFormObject() {
		return new ItemFilter();
	}

	@Override
	protected JComponent createFormControl() {
		FormLayout layout = new FormLayout("default, 3dlu, fill:pref:nogrow", "default");
		FormLayoutFormBuilder builder = new FormLayoutFormBuilder(getBindingFactory(), layout);
		builder.addHorizontalSeparator("Quick search", 3);
		builder.nextRow();
		builder.addPropertyAndLabel("quickSearch", "searchBinder");
		builder.nextRow();
		builder.addHorizontalSeparator("Item search fields", 3);
		builder.nextRow();
		builder.addPropertyAndLabel("nameContains");
		return builder.getPanel();
	}
}