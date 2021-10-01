package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import net.sf.opticalbot.OMRModelContext;
import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.OMRModelFactory;
import net.sf.opticalbot.omr.exception.OMRModelSaveException;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Resources;
import net.sf.opticalbot.resources.ResourcesKeys;
import net.sf.opticalbot.ui.utilities.JSildeBar;
import net.sf.opticalbot.ui.utilities.SpringUtilities;

public class UIOMRModel extends JPanel {

	private static final long serialVersionUID = 1L;

	public final ActionListener actFieldDelete = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			List<FormField> selection = lstFields.getSelectedValuesList();

			if (selection.isEmpty()) {
				JOptionPane.showMessageDialog(fraMain,
						Dictionary.translate("msg.no.selected.field"),
						Dictionary.translate("msg.error"),
						JOptionPane.ERROR_MESSAGE);
			} else {
				for (FormField field : selection) {
					opticalbot.getTemplate().removeField(field);
					lstFields.repaint();
					uiImage.repaint();
				}
			}
		}
	};

	public final ActionListener actMultipleFieldsCreation = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			UIMultipleFieldsCreation ui = new UIMultipleFieldsCreation(fraMain,
					autoNumberingStart);
			ui.setVisible(true);
			switch (ui.getAction()) {
			case CANCEL:
				break;
			case OK:
				lastUIMultipleCreation = ui;
				autoNumberingStart = ui.getAutoNumberingStart();
				uiImage.setMode(ImageFrame.Mode.SETUP_POINTS);
				break;
			default:
				break;
			}
		}
	};

	public final ActionListener actSaveTemplate = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {

			File file = opticalbot.getTemplate().getFile();
			if (file == null) {
				// OMRModel has not been saved before. Showing File Dialog
				// so user can choose a file name.
				UIFileChooser flc = new UIFileChooser();
				flc.setMultiSelectionEnabled(false);
				flc.setTemplateFilter();
				int outcome = flc.showSaveDialog(fraMain);
				if (outcome == JFileChooser.APPROVE_OPTION) {
					// User selected a file name. Setting this file to OMRmodel.
					file = flc.getSelectedFile();
					opticalbot.getTemplate().setFile(file);
				} else {
					// User clicked "cancel" button on save dialog. Aborting.
					return;
				}
			}
			// At this point, we can guarantee that the current OMRModel has a
			// file associated with it.
			try {
				OMRModelFactory.save(opticalbot.getTemplate());
				JOptionPane.showMessageDialog(null,
						Dictionary.translate("template.saved"),
						Dictionary.translate("template.saved.popup.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (OMRModelSaveException e1) {
				JOptionPane.showMessageDialog(null,
						Dictionary.translate("template.not.saved"),
						Dictionary.translate("template.not.saved.popup.title"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	private final UIMain fraMain;
	private FieldListModel lsmFields;
	private JList<FormField> lstFields;
	private final OMRModelContext opticalbot;
	private int autoNumberingStart = 1;
	private ImageFrame uiImage;
	private UIMultipleFieldsCreation lastUIMultipleCreation;

	public UIOMRModel(OMRModelContext opticalbot, UIMain fraMain) {
		this.setLayout(new BorderLayout());
		this.opticalbot = opticalbot;
		this.fraMain = fraMain;
		this.lsmFields = new FieldListModel(opticalbot.getTemplate());

		JPanel pnlModel = new JPanel(new BorderLayout());
		// Fields panel at west
		lstFields = new JList<FormField>(lsmFields);
		lstFields
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scpFields = new JScrollPane(lstFields);

		JButton btnMultipleFieldsCreation = new JButton();
		btnMultipleFieldsCreation.addActionListener(actMultipleFieldsCreation);
		btnMultipleFieldsCreation.setIcon(Resources
				.getIcon(ResourcesKeys.ADD_FIELD_BUTTON));
		btnMultipleFieldsCreation.setToolTipText(Dictionary
				.translate("add.field.button.tooltip"));

		JButton btnFieldDelete = new JButton();
		btnFieldDelete.addActionListener(actFieldDelete);
		btnFieldDelete.setIcon(Resources
				.getIcon(ResourcesKeys.REMOVE_FIELD_BUTTON));
		btnFieldDelete.setToolTipText(Dictionary
				.translate("remove.field.button.tooltip"));

		JPanel pnlFieldsOptions = new JPanel();
		pnlFieldsOptions.setOpaque(false);
		pnlFieldsOptions.setLayout(new SpringLayout());
		pnlFieldsOptions.add(btnMultipleFieldsCreation);
		pnlFieldsOptions.add(btnFieldDelete);
		SpringUtilities.makeGrid(pnlFieldsOptions, 1, 2, 3, 3, 3, 3);

		JPanel pnlFieldList = new JPanel(new BorderLayout());
		pnlFieldList.add(scpFields, BorderLayout.CENTER);
		pnlFieldList.add(pnlFieldsOptions, BorderLayout.NORTH);

		// pnlModel.add(pnlFieldList, BorderLayout.WEST);
		JSildeBar jobToolbar = new JSildeBar();
		jobToolbar.addBar("DICT Fields", pnlFieldList);
		// TODO ADD Corner buttons here
		jobToolbar.addBar("DICT Corners", new JPanel());

		pnlModel.add(jobToolbar, BorderLayout.WEST);

		// Image panel at center
		this.uiImage = new ImageFrame(opticalbot, opticalbot.getTemplate()
				.getImage(), opticalbot.getTemplate(), ImageFrame.Mode.VIEW,
				this);

		pnlModel.add(uiImage, BorderLayout.CENTER);

		// Options panel at north
		JButton btnSave = new JButton();
		btnSave.addActionListener(actSaveTemplate);
		btnSave.setText(Dictionary.translate("save.template.button"));

		JPanel pnlOMRModelOptions = new JPanel();
		pnlOMRModelOptions.setLayout(new SpringLayout());
		pnlOMRModelOptions.add(btnSave);
		SpringUtilities.makeCompactGrid(pnlOMRModelOptions, 1, 1, 3, 3, 3, 3);
		pnlModel.add(pnlOMRModelOptions, BorderLayout.NORTH);

		JTabbedPane tbpOMRModel = new JTabbedPane();
		tbpOMRModel.add("DICT Model", pnlModel);
		tbpOMRModel.add("DICT Scan", new UIScan(opticalbot));
		this.add(tbpOMRModel, BorderLayout.CENTER);
	}

	public void createFields(List<FormPoint> points) {

		String response = Dictionary.translate("response") + " ";
		String question = Dictionary.translate("question") + " ";
		List<FormField> list = new LinkedList<FormField>();

		for (int i = 0; i < lastUIMultipleCreation.getRowsNumber(); i++) {
			String name = question
					+ String.format("%03d", autoNumberingStart + i);
			FormField field = new FormField(name);

			for (int j = 0; j < lastUIMultipleCreation.getValuesNumber(); j++) {
				String responseName = response + String.format("%02d", j);
				int index = (lastUIMultipleCreation.getValuesNumber() * i) + j;
				FormPoint p = points.get(index);
				field.setPoint(responseName, p);
			}
			field.setMultiple(lastUIMultipleCreation.getMultiple());
			list.add(field);
		}
		autoNumberingStart += lastUIMultipleCreation.getRowsNumber();
		opticalbot.getTemplate().setFields(list);
		int indexStart = opticalbot.getTemplate().getFields().size();
		int indexEnd = indexStart + list.size() - 1;
		lsmFields.newRows(indexStart, indexEnd);
		lstFields.repaint();
		uiImage.repaint();
	}

	public Orientation getOrientation() {
		return lastUIMultipleCreation.getOrientation();
	}

	public int getRowsNumber() {
		return lastUIMultipleCreation.getRowsNumber();
	}

	public int getValuesNumber() {
		return lastUIMultipleCreation.getValuesNumber();
	}

	// @Deprecated
	// public void setImageFrame(ImageFrame uiImage) {
	// // Deprecated because UIOMRModel will contain an image frame itself.
	// this.uiImage = uiImage;
	// }

}