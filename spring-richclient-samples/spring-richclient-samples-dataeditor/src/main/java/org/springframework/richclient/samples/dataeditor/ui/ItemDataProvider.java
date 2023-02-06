package org.springframework.richclient.samples.dataeditor.ui;

import java.util.List;

import org.springframework.richclient.samples.dataeditor.domain.ItemFilter;
import org.springframework.richclient.samples.dataeditor.domain.ItemService;
import org.springframework.richclient.widget.editor.provider.AbstractDataProvider;

public class ItemDataProvider extends AbstractDataProvider {
	private ItemService service;

	public ItemDataProvider(ItemService service) {
		this.service = service;
	}

	@Override
	public boolean supportsFiltering() {
		return true;
	}

	@Override
	public List getList(Object criteria) {
		if (criteria instanceof ItemFilter) {
			ItemFilter itemFilter = (ItemFilter) criteria;
			return service.findItems(itemFilter);
		} else {
			throw new IllegalArgumentException(
					"This provider can only filter through ItemFilter, not " + criteria.getClass());
		}
	}

	@Override
	public boolean supportsUpdate() {
		return true;
	}

	@Override
	public boolean supportsCreate() {
		return true;
	}

	@Override
	public boolean supportsClone() {
		return false;
	}

	@Override
	public boolean supportsDelete() {
		return true;
	}

	@Override
	public Object doCreate(Object newData) {
		return newData;
	}

	@Override
	public void doDelete(Object dataToRemove) {
	}

	@Override
	public Object doUpdate(Object updatedData) {
		return updatedData;
	}
}
