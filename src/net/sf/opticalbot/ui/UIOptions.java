package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.sf.opticalbot.omr.OMRContext;
import net.sf.opticalbot.omr.ShapeType;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Settings;
import net.sf.opticalbot.resources.Settings.Setting;

public class UIOptions extends JDialog {

	private static final long serialVersionUID = 1L;
	private OMRContext model;
	private JSpinner thresholdValue;
	private JSpinner densityValue;
	private JComboBox<ShapeType> cbxShapeType;
	private JSpinner shapeSizeValue;

	public final ActionListener actSave = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int threshold = getThresholdValue();
			int density = getDensityValue();
			int shapeSize = getShapeSize();
			ShapeType shapeType = getShapeType();

			model.getSettings().set(Settings.Setting.Threshold,
					String.valueOf(threshold));
			model.getSettings().set(Settings.Setting.Density,
					String.valueOf(density));
			model.getSettings().set(Settings.Setting.ShapeSize,
					String.valueOf(shapeSize));
			model.getSettings().set(Settings.Setting.Shape,
					shapeType.name());
			model.getSettings().store();
			dispose();
		}
	};

	public final ActionListener actCancel = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	/**
	 * Create the frame.
	 */
	public UIOptions(OMRContext model, UIMain fraMain) {
		super(fraMain, true);

		this.model = model;
		this.setTitle(Dictionary.translate("options.frame.title"));
		this.setResizable(false);

		JPanel optionsPanel = getOptionsPanel();
		JPanel shapePanel = getShapePanel();

		JPanel masterPanel = new JPanel(new BorderLayout());
		masterPanel.add(optionsPanel, BorderLayout.NORTH);
		masterPanel.add(shapePanel, BorderLayout.SOUTH);

		JPanel buttonPanel = getButtonPanel();
		setDefaultValues();

		getContentPane().add(masterPanel, BorderLayout.NORTH);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(fraMain);
	}

	private void setDefaultValues() {
		thresholdValue.setValue(Integer.valueOf(model.getSettings().get(Setting.Threshold)));
		densityValue.setValue(Integer.valueOf(model.getSettings().get(Setting.Density)));
		cbxShapeType.setSelectedItem(ShapeType.valueOf(model.getSettings().get(Setting.Shape)));
		shapeSizeValue.setValue(Integer.valueOf(model.getSettings().get(Setting.ShapeSize)));
	}

	private JPanel getOptionsPanel() {
		thresholdValue = new JSpinner();
		thresholdValue.setModel(new SpinnerNumberModel(1,
				Settings.MIN_THRESHOLD, Settings.MAX_THRESHOLD, 1));

		densityValue = new JSpinner();
		densityValue.setModel(new SpinnerNumberModel(1, Settings.MIN_DENSITY,
				Settings.MAX_DENSITY, 1));

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.add(new JLabel(Dictionary
				.translate("threshold.option.label")));
		pnlOptions.add(thresholdValue);
		pnlOptions
				.add(new JLabel(Dictionary.translate("density.option.label")));
		pnlOptions.add(densityValue);
		pnlOptions.setBorder(BorderFactory.createTitledBorder(Dictionary
				.translate("scan.options")));

		// TODO: Improve design of this window
		for (java.awt.Component c: pnlOptions.getComponents()) {
			javax.swing.JComponent jc = (javax.swing.JComponent)c;
			jc.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		}

		return pnlOptions;
	}

	private JPanel getShapePanel() {
		ComboBoxModel<ShapeType> cbmShapeType = new DefaultComboBoxModel<ShapeType>(
				ShapeType.values());
		cbxShapeType = new JComboBox<ShapeType>();
		cbxShapeType.setModel(cbmShapeType);

		shapeSizeValue = new JSpinner();
		shapeSizeValue.setModel(new SpinnerNumberModel(1,
				Settings.MIN_SHAPESIZE, Settings.MAX_SHAPESIZE, 1));

		JPanel pnlShape = new JPanel();
		pnlShape.setLayout(new BoxLayout(pnlShape, BoxLayout.Y_AXIS));
		pnlShape.add(new JLabel(Dictionary.translate("shape.type.option.label")));
		pnlShape.add(cbxShapeType);
		pnlShape.add(new JLabel(Dictionary.translate("shape.size.option.label")));
		pnlShape.add(shapeSizeValue);
		pnlShape.setBorder(BorderFactory.createTitledBorder(Dictionary
				.translate("marker.options")));

		// TODO: Improve design of this window
		for (java.awt.Component c: pnlShape.getComponents()) {
			javax.swing.JComponent jc = (javax.swing.JComponent)c;
			jc.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		}

		return pnlShape;
	}

	private JPanel getButtonPanel() {
		JButton saveButton = new JButton();
		saveButton.setText(Dictionary.translate("save.options.button"));
		saveButton.addActionListener(actSave);

		JButton cancelButton = new JButton();
		cancelButton.setText(Dictionary.translate("cancel.button"));
		cancelButton.addActionListener(actCancel);

		JPanel pnlButton = new JPanel(new FlowLayout());
		pnlButton.add(saveButton);
		pnlButton.add(cancelButton);
		return pnlButton;
	}

	private int getThresholdValue() {
		return (Integer) thresholdValue.getValue();
	}

	private int getDensityValue() {
		return (Integer) densityValue.getValue();
	}

	private int getShapeSize() {
		return (Integer) shapeSizeValue.getValue();
	}

	private ShapeType getShapeType() {
		return cbxShapeType.getItemAt(cbxShapeType.getSelectedIndex());
	}

}
