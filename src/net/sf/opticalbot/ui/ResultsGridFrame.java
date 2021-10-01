package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.opticalbot.OMRModelContext;
import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.resources.Dictionary;

public class ResultsGridFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane responsesGridPanel;
	private OMRModel form;
	private int rows;
	private int cols;
	String[] header;

	private class TemplateTableModel extends DefaultTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public TemplateTableModel(int rows, int cols) {
			super(rows, cols);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	/**
	 * Create the frame.
	 */
	public ResultsGridFrame(OMRModelContext model, OMRModel filledForm) {

		this.form = filledForm;

		OMRModel template = model.getTemplate();
		header = (String[]) template.getHeader();
		rows = template.getFields().size() + 1;
		cols = 2;
		setTitle(Dictionary.translate("results.grid.frame.title"));
		setBounds(700, 100, 230, 300);
		setResizable(true);

		table = createTable();
		clearTable();
		setupTable();
		responsesGridPanel = new JScrollPane(table);

		getContentPane().add(responsesGridPanel, BorderLayout.CENTER);
	}

	private void clearTable() {
		table.selectAll();
		table.clearSelection();
	}

	private void setupTable() {
		for (int i = 1; i < rows; i++) {
			FormField field = form.getFields().get(i - 1);
			if (field != null) {
				table.setValueAt(field.getValues(), i, 1);
			}
		}
	}

	private JTable createTable() {
		TemplateTableModel tableModel = new TemplateTableModel(rows, cols);
		TableColumnModel columnModel = new DefaultTableColumnModel();

		for (int i = 0; i < cols; i++) {
			TableColumn column = new TableColumn(i);
			column.setMinWidth(100);
			columnModel.addColumn(column);
		}

		JTable table = new JTable(tableModel, columnModel);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				final Component cell = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if (row == 0 || column == 0) {
					cell.setBackground(new java.awt.Color(238, 238, 238));
				} else {
					cell.setBackground(Color.white);
				}
				return cell;
			}
		});

		for (int i = 1; i < cols; i++) {
			table.setValueAt((String) Dictionary.translate("results"), 0, i);
		}

		for (int i = 1; i < rows; i++) {
			table.setValueAt(header[i], i, 0);
		}
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		return table;
	}
}
