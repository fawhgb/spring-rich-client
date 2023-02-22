package org.springframework.richclient.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author peter.de.bruycker
 */
public class ListTableModelTests extends AbstractBaseTableModelTests {

	private final ListTableModel dummyListTableModel = new ListTableModel() {

		@Override
		protected Class[] createColumnClasses() {
			return new Class[] { String.class };
		}

		@Override
		protected String[] createColumnNames() {
			return new String[] { "column" };
		}

	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BaseTableModel getBaseTableModel() {
		return this.dummyListTableModel;
	}

	/**
	 * TestCase for bug #RCP-14
	 */
	@Test
	public void testConstructorThrowsNullPointerException() {
		try {
			ListTableModel model = new ListTableModel() {
				@Override
				protected Class[] createColumnClasses() {
					return new Class[] { String.class };
				}

				@Override
				protected String[] createColumnNames() {
					return new String[] { "column" };
				}
			};
			model.createColumnInfo();
			model.getColumnCount();
		} catch (NullPointerException e) {
			fail("Should not throw NullPointerException");
		}

		try {
			ListTableModel model = new ListTableModel(new ArrayList()) {
				@Override
				protected Class[] createColumnClasses() {
					return new Class[] { String.class };
				}

				@Override
				protected String[] createColumnNames() {
					return new String[] { "col0" };
				}
			};
			model.createColumnInfo();
			model.getColumnCount();
		} catch (NullPointerException e) {
			fail("Should not throw NullPointerException");
		}
	}

	@Test
	public void testGetValueAtInternalWithOneColumnNoArray() {
		ListTableModel model = new ListTableModel() {
			@Override
			protected Class[] createColumnClasses() {
				return new Class[] { String.class };
			}

			@Override
			protected String[] createColumnNames() {
				return new String[] { "col0" };
			}
		};
		model.setRowNumbers(false);

		String row = "col0";

		assertEquals("col0", model.getValueAtInternal(row, 0));
	}

	@Test
	public void testGetValueAtInternalWithArray() {
		ListTableModel model = new ListTableModel() {
			@Override
			protected Class[] createColumnClasses() {
				return new Class[] { String.class, String.class };
			}

			@Override
			protected String[] createColumnNames() {
				return new String[] { "col0", "col1" };
			}
		};
		model.setRowNumbers(false);

		String[] row = new String[] { "col0", "col1" };

		assertEquals("col0", model.getValueAtInternal(row, 0));
		assertEquals("col1", model.getValueAtInternal(row, 1));
	}

	@Test
	public void testGetValueAtInternalWithInvalidObjectType() {
		// model with two columns, but no list or array as rows
		ListTableModel model = new ListTableModel() {
			@Override
			protected Class[] createColumnClasses() {
				return new Class[] { String.class, String.class };
			}

			@Override
			protected String[] createColumnNames() {
				return new String[] { "col0", "col1" };
			}
		};
		model.setRowNumbers(false);

		String row = "col0";

		try {
			model.getValueAtInternal(row, 0);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			pass();
		}
	}

	private static void pass() {
		// test passes
	}

	@Test
	public void testGetValueAtInternalWithList() {
		ListTableModel model = new ListTableModel() {
			@Override
			protected Class[] createColumnClasses() {
				return new Class[] { String.class, String.class };
			}

			@Override
			protected String[] createColumnNames() {
				return new String[] { "col0", "col1" };
			}
		};
		model.createColumnInfo();
		List row = Arrays.asList(new String[] { "col0", "col1" });
		assertEquals("col0", model.getValueAtInternal(row, 0));
		assertEquals("col1", model.getValueAtInternal(row, 1));
	}

	@Test
	public void testGetValueAtInternalWithOneColumnAndArray() {
		ListTableModel model = new ListTableModel() {
			@Override
			protected Class[] createColumnClasses() {
				return new Class[] { String.class };
			}

			@Override
			protected String[] createColumnNames() {
				return new String[] { "col0" };
			}
		};
		model.setRowNumbers(false);

		String[] row = new String[] { "col0", "col1" };

		assertEquals("col0", model.getValueAtInternal(row, 0));
	}
}