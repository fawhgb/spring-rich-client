package org.springframework.richclient.samples.dataeditor.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.TitledWidgetApplicationDialog;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.SimpleValidationResultsReporter;
import org.springframework.richclient.widget.AbstractTitledWidget;
import org.springframework.util.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class FormTester {
	private FormTester(String context) {
		Application.load((Application) new ClassPathXmlApplicationContext(context).getBean("application"));
	}

	public static FormTester createFormTesterWithContext(String context) {
		return new FormTester(context);
	}

	public void showFormDialog(final AbstractForm form, boolean modal) {
		AbstractTitledWidget widget = new AbstractTitledWidget() {
			@Override
			public JComponent createWidgetContent() {
				final JTextArea area = new JTextArea(20, 40);
				area.setLineWrap(true);
				area.setWrapStyleWord(true);
				form.getFormModel().addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						area.append(
								evt.getPropertyName() + ": " + evt.getOldValue() + " -> " + evt.getNewValue() + "\n");
					}
				});
				JPanel panel = new JPanel(
						new FormLayout("fill:pref:nogrow,fill:5dlu:nogrow, fill:pref:grow", "fill:pref:grow"));
				panel.add(form.getControl(), new CellConstraints(1, 1));
				JTabbedPane pane = new JTabbedPane();
				pane.addTab("Properties", new JScrollPane(area));
				final PropertySheetTableModel propertySheetTableModel = new PropertySheetTableModel();
				for (final Object property : form.getFormModel().getFieldNames()) {
					Property p = new AbstractProperty() {
						private static final long serialVersionUID = 1L;

						@Override
						public String getName() {
							return property.toString();
						}

						@Override
						public String getDisplayName() {
							return property.toString();
						}

						@Override
						public String getShortDescription() {
							return property.toString();
						}

						@Override
						public Class getType() {
							return form.getFormModel().getFieldMetadata(property.toString()).getPropertyType();
						}

						@Override
						public boolean isEditable() {
							return false;
						}

						@Override
						public String getCategory() {
							return form.getFormModel().getId();
						}

						@Override
						public Object getValue() {
							Object value = form.getFormModel().getValueModel(property.toString()).getValue();
							if (value == null) {
								return "«null»";
							} else if (!StringUtils.hasText(value.toString())) {
								return "«empty string»";
							} else {
								return value;
							}
						}

						@Override
						public void readFromObject(Object o) {
							throw new UnsupportedOperationException("Method readFromObject not yet implemented");
						}

						@Override
						public void writeToObject(Object o) {
							throw new UnsupportedOperationException("Method writeToObject not yet implemented");
						}
					};
					propertySheetTableModel.addProperty(p);
				}
				final PropertySheetTable table = new PropertySheetTable(propertySheetTableModel);
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									propertySheetTableModel.fireTableDataChanged();
									// table.repaint();
								}
							});
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				pane.addTab("Valuemodels", new JScrollPane(table));
				final JTextArea objectArea = new JTextArea(20, 40);
				objectArea.setLineWrap(true);
				objectArea.setWrapStyleWord(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									objectArea.setText(ToStringBuilder.reflectionToString(form.getFormObject(),
											ToStringStyle.MULTI_LINE_STYLE));
								}
							});
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				pane.addTab("Form object", new JScrollPane(objectArea));
				final JList validationList = new JList();
				validationList.setCellRenderer(new DefaultCellRenderer() {
					private static final long serialVersionUID = 1L;

					@Override
					protected String convertToString(Object o) {
						if (o instanceof Message) {
							Message message = (Message) o;
							return message.getSeverity() + ": " + message.getMessage();
						}
						return o.toString();
					}
				});
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									validationList.setListData(
											form.getFormModel().getValidationResults().getMessages().toArray());
								}
							});
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				pane.addTab("Validations", new JScrollPane(validationList));
				panel.add(pane, new CellConstraints(3, 1));
				return panel;
			}

			@Override
			public List<? extends AbstractCommand> getCommands() {
				List<AbstractCommand> commands = new ArrayList<AbstractCommand>();
				commands.add(form.getCommitCommand());
				commands.add(form.getRevertCommand());
				return commands;
			}
		};
		TitledWidgetApplicationDialog dialog = new TitledWidgetApplicationDialog(widget);
		form.addValidationResultsReporter(
				new SimpleValidationResultsReporter(form.getFormModel().getValidationResults(), widget));
		dialog.setTitle("Form test");
		dialog.setModal(modal);
		dialog.showDialog();
		dialog.getParentWindow().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public void showFormJFrame(final AbstractForm form) {
		JFrame frame = createJFrame(form);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public JFrame createJFrame(final AbstractForm form) {
		AbstractTitledWidget widget = new AbstractTitledWidget() {
			@Override
			public JComponent createWidgetContent() {
				form.getFormModel().addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						System.out
								.println(evt.getPropertyName() + ": " + evt.getOldValue() + " -> " + evt.getNewValue());
					}
				});
				return form.getControl();
			}

			@Override
			public List<? extends AbstractCommand> getCommands() {
				List<AbstractCommand> commands = new ArrayList<AbstractCommand>();
				commands.add(form.getCommitCommand());
				commands.add(form.getRevertCommand());
				return commands;
			}
		};
		form.addValidationResultsReporter(
				new SimpleValidationResultsReporter(form.getFormModel().getValidationResults(), widget));
		JFrame frame = new JFrame();
		frame.add(widget.getComponent());
		return frame;
	}
}
