package org.springframework.richclient.samples.dataeditor.ui;

import java.util.List;

import org.springframework.richclient.samples.dataeditor.domain.Supplier;
import org.springframework.richclient.samples.dataeditor.domain.SupplierFilter;
import org.springframework.richclient.samples.dataeditor.domain.SupplierService;
import org.springframework.richclient.widget.editor.provider.AbstractDataProvider;

public class SupplierDataProvider extends AbstractDataProvider {
	private SupplierService service;

	public SupplierDataProvider(SupplierService service) {
		this.service = service;
	}

	@Override
	public boolean supportsFiltering() {
		return true;
	}

	@Override
	public List getList(Object criteria) {
		if (criteria instanceof SupplierFilter) {
			return service.findSuppliers((SupplierFilter) criteria);
		} else if (criteria instanceof Supplier) {
			return service.findSuppliers(SupplierFilter.fromSupplier((Supplier) criteria));
		} else {
			throw new IllegalArgumentException(
					"This provider can only filter through SupplierFilter, not " + criteria.getClass());
		}
	}

	@Override
	public boolean supportsUpdate() {
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
}
