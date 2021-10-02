package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import net.sf.opticalbot.omr.Orientation;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.ui.utilities.SpringUtilities;

public class UIMultipleFieldsCreation extends JDialog {

	public enum Action {
		CANCEL, OK;
	}

	private static final long serialVersionUID = 1L;
	private final ActionListener actCancel = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			action = Action.CANCEL;
			dispose();
		}
	};

	private Action action = Action.CANCEL;

	private final ActionListener actOK = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			action = Action.OK;
			dispose();
		}
	};

	private final JButton btnCancel;
	private final JButton btnOK;
	private final JCheckBox chbMultiple;
	private final JComboBox<Orientation> cmbOrientation;
	private final JSpinner spnAutoNumberingStart;
	private final JSpinner spnRows;
	private final JSpinner spnValues;

	public UIMultipleFieldsCreation(Frame frame, int autoNumberingStart) {
		super(frame, true);
		this.setLayout(new BorderLayout());
		this.setTitle(Dictionary.translate("field.properties.tab.name"));

		cmbOrientation = new JComboBox<Orientation>();
		cmbOrientation.setModel(new DefaultComboBoxModel<Orientation>(Orientation
				.values()));

		chbMultiple = new JCheckBox();
		chbMultiple.setSelected(false);

		spnRows = new JSpinner();
		spnRows.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

		spnValues = new JSpinner();
		spnValues.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

		spnAutoNumberingStart = new JSpinner();
		spnAutoNumberingStart.setModel(new SpinnerNumberModel(
				autoNumberingStart, 1, Integer.MAX_VALUE, 1));

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new SpringLayout());

		pnlOptions.add(new JLabel(Dictionary
				.translate("field.properties.type.label")));
		pnlOptions.add(cmbOrientation);
		pnlOptions.add(new JLabel(Dictionary
				.translate("field.properties.is.multiple")));
		pnlOptions.add(chbMultiple);
		pnlOptions.add(new JLabel(Dictionary
				.translate("field.properties.number.rows.columns.label")));
		pnlOptions.add(spnRows);
		pnlOptions.add(new JLabel(Dictionary
				.translate("field.properties.number.values.label")));
		pnlOptions.add(spnValues);
		pnlOptions.add(new JLabel(Dictionary
				.translate("field.properties.auto.numbering.start.label")));
		pnlOptions.add(spnAutoNumberingStart);
		SpringUtilities.makeGrid(pnlOptions, 5, 2, 3, 3, 3, 3);

		btnOK = new JButton();
		btnOK.addActionListener(actOK);
		btnOK.setText(Dictionary.translate("ok.button"));

		btnCancel = new JButton();
		btnCancel.addActionListener(actCancel);
		btnCancel.setText(Dictionary.translate("cancel.button"));

		JPanel pnlOKCancel = new JPanel();
		pnlOKCancel.setLayout(new SpringLayout());
		pnlOKCancel.add(btnOK);
		pnlOKCancel.add(btnCancel);
		SpringUtilities.makeCompactGrid(pnlOKCancel, 1, 2, 3, 3, 3, 3);

		this.add(pnlOptions, BorderLayout.CENTER);
		this.add(pnlOKCancel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(frame);
	}

	public Action getAction() {
		return action;
	}

	public int getAutoNumberingStart() {
		return (Integer) spnAutoNumberingStart.getValue();
	}

	public boolean getMultiple() {
		return chbMultiple.isSelected();
	}

	public Orientation getOrientation() {
		return cmbOrientation.getItemAt(cmbOrientation.getSelectedIndex());
	}

	public int getRowsNumber() {
		return (Integer) spnRows.getValue();
	}

	public int getValuesNumber() {
		return (Integer) spnValues.getValue();
	}

}
