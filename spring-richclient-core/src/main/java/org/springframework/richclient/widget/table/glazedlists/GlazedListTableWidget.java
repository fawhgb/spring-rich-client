package org.springframework.richclient.widget.table.glazedlists;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.xswingx.JXSearchField;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.richclient.util.RcpSupport;
import org.springframework.richclient.util.ValueMonitor;
import org.springframework.richclient.widget.AbstractWidget;
import org.springframework.richclient.widget.table.TableCellRenderers;
import org.springframework.richclient.widget.table.TableDescription;
import org.springframework.richclient.widget.table.TableWidget;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

public final class GlazedListTableWidget extends AbstractWidget implements TableWidget {
	private JXTable theTable = new JXTable();

	private JScrollPane tableScroller;

	private ValueMonitor selectionMonitor = new ValueMonitor();

	private EventTableModel<Object> tableModel;

	private EventSelectionModel<Object> selectionModel;

	private EventList<Object> dataList;

	private EventList<Object> shownList;

	private SortedList<Object> sortedList;

	private JTextField textFilterField;

	private AbstractCommand[] navigationCommands;

	private CommandGroup navigationCommandGroup;

	private CommandGroup selectColumnCommandGroup;

	private CommandConfigurer commandConfigurer;

	private JLabel countLabel;

	static {
		UIManager.put("JXTable.column.horizontalScroll", RcpSupport.getMessage("JXTable.horizontalScroll.label"));
		UIManager.put("JXTable.column.packAll", RcpSupport.getMessage("JXTable.packAll.label"));
		UIManager.put("JXTable.column.packSelected", RcpSupport.getMessage("JXTable.packSelected.label"));
	}

	/**
	 * CellEditorListener op de selectiekolom om de selectieListeners gezamelijk te
	 * triggeren .
	 */
	private CellEditorListener userSelectionCellEditorListener = new CellEditorListener() {

		@Override
		public void editingStopped(ChangeEvent e) {
			fireUserSelectionChangedEvent();
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
		}
	};

	private Set dirtyRows = new HashSet();

	private CellEditorListener dirtyRowCellEditorListener = new CellEditorListener() {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			dirtyRows.add(getSelectedRows()[0]);
		}
	};

	/**
	 * De listeners geregistreerd op de selectiekolom, getriggerd door
	 * {@link #userSelectionCellEditorListener}.
	 */
	private List<PropertyChangeListener> userSelectionListeners;

	public GlazedListTableWidget(List<? extends Object> rows, TableDescription tableDesc) {
		this(rows, tableDesc, tableDesc.getDefaultComparator());
	}

	public GlazedListTableWidget(List<? extends Object> rows, TableDescription tableDesc, Comparator comparator) {
		this(tableDesc.getDataType(), rows, GlazedListsSupport.makeTableFormat(tableDesc),
				GlazedListsSupport.makeFilterProperties(tableDesc), comparator, tableDesc.hasSelectColumn());
		// Als de tablewidget met ons eigen TableDescription class is gemaakt
		// kunnen we additionele dingen als width/resizable/renderer en editor
		// zetten
		// bedenking: zouden we tabledesc van een iterator voorzien om over de
		// kolommen te lopen?
		TableCellEditor columnEditor = null;
		for (int i = 0; i < tableDesc.getColumnCount(); ++i) {
			TableColumnExt column = (TableColumnExt) theTable.getColumns(true).get(i);
			int columnWidth = tableDesc.getMaxColumnWidth(i);
			if (columnWidth > 0) {
				column.setMaxWidth(columnWidth);
			}
			columnWidth = tableDesc.getMinColumnWidth(i);
			if (columnWidth > 0) {
				column.setMinWidth(columnWidth);
			}
			column.setResizable(tableDesc.isResizable(i));
			column.setVisible(tableDesc.isVisible(i));
			columnEditor = tableDesc.getColumnEditor(i);
			if (columnEditor != null) {
				if (tableDesc.isSelectColumn(i)) {
					columnEditor.addCellEditorListener(userSelectionCellEditorListener);
				} else {
					columnEditor.addCellEditorListener(dirtyRowCellEditorListener);
				}
				column.setCellEditor(columnEditor);
			}
			if (tableDesc.getColumnRenderer(i) != null) {
				TableCellRenderer renderer = tableDesc.getColumnRenderer(i);
				column.setCellRenderer(renderer);
				if (renderer instanceof DefaultTableCellRenderer) {
					int align = ((DefaultTableCellRenderer) renderer).getHorizontalAlignment();
					switch (align) {
					case SwingConstants.CENTER:
						column.setHeaderRenderer(
								wrapInSortArrowHeaderRenderer(TableCellRenderers.CENTER_ALIGNED_HEADER_RENDERER));
						break;
					case SwingConstants.RIGHT:
						column.setHeaderRenderer(
								wrapInSortArrowHeaderRenderer(TableCellRenderers.RIGHT_ALIGNED_HEADER_RENDERER));
						break;
					default:
						break;
					}
				}
			}
		}
	}

	private TableCellRenderer wrapInSortArrowHeaderRenderer(TableCellRenderer renderer) {
		if (tableComparatorChooser != null) {
			return tableComparatorChooser.createSortArrowHeaderRenderer(renderer);
		} else {
			return renderer;
		}
	}

	public GlazedListTableWidget(Class dataType, List<? extends Object> rows, TableFormat format,
			String[] filterProperties) {
		this(dataType, rows, format, filterProperties, null, false);
	}

	public GlazedListTableWidget(Class dataType, List<? extends Object> rows, TableFormat format,
			String[] filterProperties, Comparator comparator, boolean addHighlightSelectColumn) {
		theTable.setColumnControlVisible(true);
		theTable.getSelectionMapper().setEnabled(false);
		commandConfigurer = (CommandConfigurer) Application.services().getService(CommandConfigurer.class);
		dataList = rows == null ? new BasicEventList<Object>() : GlazedLists.eventList(rows);

		sortedList = new SortedList<Object>(dataList, comparator);
		this.shownList = sortedList;

		if (filterProperties != null) {
			textFilterField = new JXSearchField(RcpSupport.getMessage("glazedListTableWidget.textFilterField.prompt"));
			textFilterField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					textFilterField.selectAll();
				}
			});
			shownList = new FilterList<Object>(shownList, new TextComponentMatcherEditor(textFilterField,
					GlazedLists.textFilterator(dataType, filterProperties)));
		}

		selectionModel = new EventSelectionModel<Object>(shownList);
		selectionModel.addListSelectionListener(new SelectionNavigationListener());
		theTable.setSelectionModel(selectionModel);

		tableModel = new EventJXTableModel<Object>(shownList, format);
		theTable.setModel(tableModel);

		if (addHighlightSelectColumn) {
			Highlighter selectHighlighter = new ColorHighlighter(HIGHLIGHTSELECTCOLUMN, new Color(0xF0, 0xF0, 0xE0),
					Color.BLACK);
			setHighlighters(HighlighterFactory.createSimpleStriping(), selectHighlighter);
			initializeSelectColumnCommands();
		} else {
			setHighlighters(HighlighterFactory.createSimpleStriping());
		}

		if (sortedList != null) {
			theTable.setSortable(false);
			theTable.getTableHeader().setDefaultRenderer(TableCellRenderers.LEFT_ALIGNED_HEADER_RENDERER);
			tableComparatorChooser = TableComparatorChooser.install(theTable, sortedList,
					AbstractTableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
			// the following is a fix for the selection sort and navigation problem
			tableComparatorChooser.addSortActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EventList<Object> selected = selectionModel.getSelected();
					int[] indexes = new int[selected.size()];
					int i = 0;
					for (Object o : selected) {
						indexes[i++] = shownList.indexOf(o);
					}
					selectionModel.clearSelection();
					for (int index : indexes) {
						selectionModel.addSelectionInterval(index, index);
					}
				}
			});
		}

		theTable.setPreferredScrollableViewportSize(new Dimension(50, 50));
		tableScroller = new JScrollPane(theTable);
		theTable.setHorizontalScrollEnabled(true);
		initializeNavigationCommands();
	}

	/**
	 * Enable the row height to diverge from the default height.
	 * <p/>
	 * NOTE: this is experimental as there is a problem with glazedlists and
	 * jxtable. (see note on ExtendedJXTable above)
	 */
	public void setRowHeightEnabled(boolean rowHeightEnabled) {
		theTable.setRowHeightEnabled(true);
	}

	private class SelectionNavigationListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// enkel op einde van reeks selection veranderingen reageren.
			if (!e.getValueIsAdjusting()) {
				if (selectionModel.getSelected().size() == 1) {
					selectionMonitor.setValue(selectionModel.getSelected().get(0));
				} else {
					Object[] selectedRows = selectionModel.getSelected().toArray();
					selectionMonitor.setValue(selectedRows.length > 0 ? selectedRows : null);
				}

				int selectedIndex = selectionModel.getAnchorSelectionIndex();
				int lastIndex = shownList.size() - 1;
				boolean emptyList = (lastIndex == -1);
				boolean onFirst = (selectedIndex == 0);
				boolean onLast = (selectedIndex == lastIndex);

				navigationCommands[NAVIGATE_FIRST].setEnabled(!emptyList && !onFirst);
				navigationCommands[NAVIGATE_PREVIOUS].setEnabled(!emptyList && !onFirst);
				navigationCommands[NAVIGATE_NEXT].setEnabled(!emptyList && !onLast);
				navigationCommands[NAVIGATE_LAST].setEnabled(!emptyList && !onLast);
			}
		}
	}

	public static final HighlightPredicate HIGHLIGHTSELECTCOLUMN = new HighlightSelectColumn();

	private TableComparatorChooser tableComparatorChooser;

	static class HighlightSelectColumn implements HighlightPredicate {

		@Override
		public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
			Object selectedValue = adapter.getValueAt(adapter.row, 0);
			return Boolean.TRUE.equals(selectedValue);
		}
	}

	public void setHighlighters(Highlighter... highlighters) {
		this.theTable.setHighlighters(highlighters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return this.dataList.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nrOfRows() {
		return this.tableModel.getRowCount();
	}

	private void initializeNavigationCommands() {
		this.navigationCommands = new AbstractCommand[4];
		this.navigationCommands[NAVIGATE_FIRST] = new ActionCommand(NAVIGATE_FIRSTROW_CMDID) {

			@Override
			protected void doExecuteCommand() {
				selectionModel.setSelectionInterval(0, 0);
				scrollToSelectedRow();
			}
		};
		this.navigationCommands[NAVIGATE_PREVIOUS] = new ActionCommand(NAVIGATE_PREVIOUSROW_CMDID) {

			@Override
			protected void doExecuteCommand() {
				int newIndex = selectionModel.getAnchorSelectionIndex() - 1;
				newIndex = (newIndex < 0) ? 0 : newIndex;
				selectionModel.setSelectionInterval(newIndex, newIndex);
				scrollToSelectedRow();
			}
		};
		this.navigationCommands[NAVIGATE_NEXT] = new ActionCommand(NAVIGATE_NEXTROW_CMDID) {

			@Override
			protected void doExecuteCommand() {
				int newIndex = selectionModel.getAnchorSelectionIndex() + 1;
				int lastIndex = shownList.size() - 1;
				newIndex = (newIndex > lastIndex) ? lastIndex : newIndex;
				selectionModel.setSelectionInterval(newIndex, newIndex);
				scrollToSelectedRow();
			}
		};
		this.navigationCommands[NAVIGATE_LAST] = new ActionCommand(NAVIGATE_LASTROW_CMDID) {

			@Override
			protected void doExecuteCommand() {
				int lastIndex = shownList.size() - 1;
				selectionModel.setSelectionInterval(lastIndex, lastIndex);
				scrollToSelectedRow();
			}
		};

		for (int i = 0; i < this.navigationCommands.length; i++) {
			this.commandConfigurer.configure(this.navigationCommands[i]);
			this.navigationCommands[i].setEnabled(false);
		}
		this.navigationCommandGroup = CommandGroup.createCommandGroup(this.navigationCommands);
	}

	private void fireUserSelectionChangedEvent() {
		if (userSelectionListeners != null) {
			for (Iterator listeners = userSelectionListeners.iterator(); listeners.hasNext();) {
				PropertyChangeListener listener = (PropertyChangeListener) listeners.next();
				listener.propertyChange(new PropertyChangeEvent(this, "selection", null, null));
			}
		}
	}

	public void addUserSelectionListener(PropertyChangeListener listener) {
		if (userSelectionListeners == null) {
			userSelectionListeners = new ArrayList<PropertyChangeListener>();
		}
		userSelectionListeners.add(listener);
	}

	private void initializeSelectColumnCommands() {
		final WritableTableFormat writableTableFormat = (WritableTableFormat) this.tableModel.getTableFormat();
		AbstractCommand selectAll = new ActionCommand(SELECT_ALL_ID) {

			@Override
			protected void doExecuteCommand() {
				shownList.getReadWriteLock().writeLock().lock();
				Iterator i = shownList.iterator();
				while (i.hasNext()) {
					writableTableFormat.setColumnValue(i.next(), Boolean.TRUE, 0);
				}
				shownList.getReadWriteLock().writeLock().unlock();
				theTable.repaint();
				fireUserSelectionChangedEvent();
			}
		};
		this.commandConfigurer.configure(selectAll);
		AbstractCommand selectNone = new ActionCommand(SELECT_NONE_ID) {

			@Override
			protected void doExecuteCommand() {
				shownList.getReadWriteLock().writeLock().lock();
				Iterator i = shownList.iterator();
				while (i.hasNext()) {
					writableTableFormat.setColumnValue(i.next(), Boolean.FALSE, 0);
				}
				shownList.getReadWriteLock().writeLock().unlock();
				theTable.repaint();
				fireUserSelectionChangedEvent();
			}
		};
		this.commandConfigurer.configure(selectNone);
		AbstractCommand selectInverse = new ActionCommand(SELECT_INVERSE_ID) {

			@Override
			protected void doExecuteCommand() {
				shownList.getReadWriteLock().writeLock().lock();
				Iterator i = shownList.iterator();
				while (i.hasNext()) {
					Object rowObject = i.next();
					Object columnValue = writableTableFormat.getColumnValue(rowObject, 0);
					writableTableFormat.setColumnValue(rowObject,
							Boolean.TRUE.equals(columnValue) ? Boolean.FALSE : Boolean.TRUE, 0);
				}
				shownList.getReadWriteLock().writeLock().unlock();
				theTable.repaint();
				fireUserSelectionChangedEvent();
			}
		};
		this.commandConfigurer.configure(selectInverse);
		this.selectColumnCommandGroup = CommandGroup
				.createCommandGroup(new Object[] { selectAll, selectNone, selectInverse });
	}

	@Override
	public final void setRows(Collection newRows) {
		this.dataList.getReadWriteLock().writeLock().lock();
		try {
			this.dirtyRows.clear();
			theTable.clearSelection();
			this.dataList.clear();
			this.dataList.addAll(newRows);

			scrollToSelectedRow(); // new rows, scroll back to top
		} finally {
			this.dataList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public final List getRows() {
		return new ArrayList<Object>(this.dataList);
	}

	@Override
	public final List getVisibleRows() {
		return new ArrayList<Object>(this.shownList);
	}

	@Override
	public void addRowObject(Object newObject) {
		this.dataList.getReadWriteLock().writeLock().lock();
		try {
			this.dataList.add(newObject);
		} finally {
			this.dataList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public void addRows(Collection rows) {
		this.dataList.getReadWriteLock().writeLock().lock();
		try {
			this.dataList.addAll(rows);
		} finally {
			this.dataList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public void removeRowObject(Object objectToRemove) {
		this.dataList.getReadWriteLock().writeLock().lock();
		try {
			dirtyRows.remove(objectToRemove);
			this.dataList.remove(objectToRemove);
		} finally {
			this.dataList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public int selectRowObject(Object toPointTo, Observer originatingObserver) {
		int index = this.shownList.indexOf(toPointTo);
		selectRowObject(index, originatingObserver);
		return index;
	}

	@Override
	public void selectRowObject(final int index, final Observer originatingObserver) {
		Runnable doSelectRowObject = new Runnable() {

			@Override
			public void run() {
				if (originatingObserver != null) {
					selectionMonitor.deleteObserver(originatingObserver);
				}

				if ((index > -1) && (shownList.size() > index)) {
					selectionModel.setSelectionInterval(index, index);
				} else {
					selectionModel.clearSelection();
				}
				scrollToSelectedRow();

				if (originatingObserver != null) {
					selectionMonitor.addObserver(originatingObserver);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			doSelectRowObject.run();
		} else {
			SwingUtilities.invokeLater(doSelectRowObject);
		}

	}

	@Override
	public void addSelection(final Object[] rows, final Observer originatingObserver) {
		Runnable doAddSelection = new Runnable() {
			@Override
			public void run() {
				if (originatingObserver != null) {
					selectionMonitor.deleteObserver(originatingObserver);
				}
				for (int i = 0; i < rows.length; i++) {
					int index = shownList.indexOf(rows[i]);
					selectionModel.addSelectionInterval(index, index);
				}
				if (originatingObserver != null) {
					selectionMonitor.addObserver(originatingObserver);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			doAddSelection.run();
		} else {
			SwingUtilities.invokeLater(doAddSelection);
		}
	}

	@Override
	public boolean hasSelection() {
		return !this.selectionModel.isSelectionEmpty();
	}

	public synchronized void scrollToSelectedRow() {
		Runnable doScrollToSelectedRow = new Runnable() {
			@Override
			public void run() {
				if (theTable.isVisible()) {
					int selectedRow = theTable.getSelectedRow();
					if (selectedRow != -1) {
						Rectangle cellRect = theTable.getCellRect(selectedRow, 0, true);
						Rectangle viewRect = tableScroller.getViewport().getViewRect();
						if (!viewRect.contains(cellRect)) {
							if (cellRect.y < viewRect.y) // cell is above view (or cut above)
							{
								tableScroller.getViewport().setViewPosition(cellRect.getLocation());
							} else // cell is below view (or cut below)
							{
								tableScroller.getViewport().scrollRectToVisible(cellRect);
							}
						}
					} else {
						tableScroller.getViewport().setViewPosition(new Point(0, 0));
					}
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			doScrollToSelectedRow.run();
		} else {
			SwingUtilities.invokeLater(doScrollToSelectedRow);
		}
	}

	@Override
	public void replaceRowObject(Object oldObject, Object newObject, Observer originatingObserver) {
		this.dataList.getReadWriteLock().writeLock().lock();
		try {
			dirtyRows.remove(oldObject);
			int index = this.dataList.indexOf(oldObject);
			if (index != -1) {
				boolean wasSelected = this.selectionModel.isSelectedIndex(this.shownList.indexOf(oldObject));

				if (wasSelected && (originatingObserver != null)) {
					this.selectionMonitor.deleteObserver(originatingObserver);
				}

				this.dataList.set(index, newObject);

				if (wasSelected) {
					int indexToSelect = this.shownList.indexOf(newObject);
					this.selectionModel.addSelectionInterval(indexToSelect, indexToSelect);
					if (originatingObserver != null) {
						this.selectionMonitor.addObserver(originatingObserver);
					}
				}
			}
		} finally {
			this.dataList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public void replaceRows(final Collection oldObject, final Collection newObject) {
		Runnable doReplaceRows = new Runnable() {
			@Override
			public void run() {
				dataList.getReadWriteLock().writeLock().lock();
				try {
					dirtyRows.clear();
					dataList.removeAll(oldObject);
					dataList.addAll(newObject);
				} finally {
					dataList.getReadWriteLock().writeLock().unlock();
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			doReplaceRows.run();
		} else {
			SwingUtilities.invokeLater(doReplaceRows);
		}
	}

	@Override
	public void unSelectAll() {
		Runnable doUnselectAll = new Runnable() {
			@Override
			public void run() {
				selectionModel.clearSelection();
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			doUnselectAll.run();
		} else {
			SwingUtilities.invokeLater(doUnselectAll);
		}
	}

	@Override
	public Object[] getSelectedRows() {
		return this.selectionModel.getSelected().toArray();
	}

	@Override
	public JComponent getComponent() {
		return this.tableScroller;
	}

	@Override
	public JTable getTable() {
		return this.theTable;
	}

	@Override
	public void addSelectionObserver(Observer observer) {
		this.selectionMonitor.addObserver(observer);
	}

	@Override
	public void removeSelectionObserver(Observer observer) {
		this.selectionMonitor.deleteObserver(observer);
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		this.tableModel.addTableModelListener(listener);
	}

	@Override
	public void removeTableModelListener(TableModelListener listener) {
		this.tableModel.removeTableModelListener(listener);
	}

	@Override
	public void updateTable() {
		this.tableModel.fireTableDataChanged();
	}

	@Override
	public JTextField getTextFilterField() {
		return textFilterField;
	}

	@Override
	public AbstractCommand[] getNavigationCommands() {
		return navigationCommands;
	}

	@Override
	public JComponent getNavigationButtonBar() {
		return getNavigationButtonBar(Sizes.PREFERRED, BorderFactory.createEmptyBorder());
	}

	public JComponent getNavigationButtonBar(Size size, Border border) {
		return this.navigationCommandGroup.createButtonBar(size, border);
	}

	public CommandGroup getNavigationCommandGroup() {
		return this.navigationCommandGroup;
	}

	public CommandGroup getSelectColumnCommandGroup() {
		return this.selectColumnCommandGroup;
	}

	@Override
	public JComponent getSelectButtonBar() {
		return this.selectColumnCommandGroup.createButtonBar(Sizes.PREFERRED, BorderFactory.createEmptyBorder());
	}

	@Override
	public JComponent getButtonBar() {
		if (this.selectColumnCommandGroup != null) {
			JPanel buttons = new JPanel(
					new FormLayout("fill:pref, 3dlu, fill:pref, 3dlu, fill:pref", "fill:pref:grow"));
			CellConstraints cc = new CellConstraints();
			buttons.add(getSelectButtonBar(), cc.xy(1, 1));
			buttons.add(new JSeparator(SwingConstants.VERTICAL), cc.xy(3, 1));
			buttons.add(getNavigationButtonBar(), cc.xy(5, 1));
			return buttons;
		}
		return getNavigationButtonBar();
	}

	@Override
	public JLabel getListSummaryLabel() {
		if (countLabel == null) {
			countLabel = createCountLabel();
		}
		return countLabel;
	}

	private JLabel createCountLabel() {
		final JLabel label = new JLabel("");

		setTextForListSummaryLabel(label);

		shownList.addListEventListener(new ListEventListener<Object>() {
			@Override
			public void listChanged(ListEvent<Object> evt) {
				if (!evt.isReordering()) {
					setTextForListSummaryLabel(label);
				}
			}
		});

		theTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					setTextForListSummaryLabel(label);
				}
			}

		});

		return label;
	}

	private void setTextForListSummaryLabel(final JLabel label) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Integer index = 0;
				Integer selectedCount = 0;
				Integer totalCount = shownList.size();

				if (getSelectedRows() != null && getSelectedRows().length > 0) {
					index = shownList.indexOf(getSelectedRows()[0]);
					index++;
					selectedCount = getSelectedRows().length;
				}

				label.setText(RcpSupport.getMessage("glazedListTableWidget", "listSummary", "label",
						new Object[] { index, selectedCount, totalCount }));
			}
		});
	}

	@Override
	public void onAboutToShow() {
		super.onAboutToShow();
		this.theTable.requestFocusInWindow();
	}

	public Set getDirtyRows() {
		return dirtyRows;
	}

}