package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.OMRContext;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.omr.exception.UnsupportedImageException;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Icons;
import net.sf.opticalbot.resources.Resources;
import net.sf.opticalbot.resources.Settings.Setting;
import net.sf.opticalbot.ui.utilities.ErrorDialog;

public class UIScan extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JList<File> lstFiles;
	private final FileListModel lsmFiles;
	private final OMRContext omrContext;
	private final List<File> openedFiles;
	private final JTable tblResult;
	private UIFormView uiView;

	/** */
	public final ActionListener actAnalyzeFilesAll = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int threshold = Integer.valueOf(omrContext.getSettings().get(Setting.Threshold));
			int density = Integer.valueOf(omrContext.getSettings().get(Setting.Density));
			int shapeSize = Integer.valueOf(omrContext.getSettings().get(Setting.ShapeSize));

			try {
				if (openedFiles.isEmpty()) {
					JOptionPane.showMessageDialog(null, "DICT There are no files to scan");
				} else {
					for (int i = 0; i < openedFiles.size(); i++) {
						selectFileAt(i);
						File imageFile = openedFiles.get(i);

						OMRModel filledForm = new OMRModel(imageFile, omrContext.formTemplate);
						filledForm.findCorners(threshold, density);
						filledForm.findPoints(threshold, density, shapeSize);
						omrContext.filledForms.put(imageFile.getName(), filledForm);
					}

					List<String> header = Arrays.asList(omrContext.getTemplate().getHeader());
					List<Map<String, String>> results = getResults(omrContext.filledForms);
					UIResultTableModel tbmResult = new UIResultTableModel(header, results);
					tblResult.setModel(tbmResult);
				}
			} catch (IOException e1) {
				new ErrorDialog(e1).setVisible(true);
			} catch (UnsupportedImageException e1) {
				// TODO: Add dictionary entries
				JOptionPane.showMessageDialog(null, Dictionary.translate("DICT error.unsupported.image.type"),
						Dictionary.translate("DICT 	error.popup.title"), JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	/** Action for Open Images option */
	public final ActionListener actOpenImages = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			omrContext.filledForms.clear();
			openedFiles.clear();

			UIFileChooser flc = new UIFileChooser();
			flc.setMultiSelectionEnabled(true);
			flc.setImagesFilter();

			if (flc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File[] files = flc.getSelectedFiles();
				openedFiles.clear();
				openedFiles.addAll(Arrays.asList(files));
				lsmFiles.update();
			}
		}
	};

	public final ActionListener actClearList = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			openedFiles.clear();
			lsmFiles.update();
		}
	};

	/**
	 * This private inner class is a custom ListModel which relies on information
	 * contained on OMRModelContext (which files are open).
	 */
	private class FileListModel extends AbstractListModel<File> {
		private static final long serialVersionUID = 1L;

		@Override
		public File getElementAt(int index) {
			return openedFiles.get(index);
		}

		@Override
		public int getSize() {
			return openedFiles.size();
		}

		public void update() {
			fireContentsChanged(this, 0, getSize());
		}
	};

	public UIScan(final OMRContext omrContext) {
		super(new BorderLayout());
		this.setOpaque(false);
		this.omrContext = omrContext;
		this.openedFiles = new LinkedList<File>();

		JPanel pnlOptions = new JPanel();
		pnlOptions.setOpaque(false);

		JButton btnOpen = new JButton();
		btnOpen.addActionListener(actOpenImages);
		btnOpen.setIcon(Resources.getIcon(Icons.OPEN_IMAGES_ICON));
		btnOpen.setToolTipText(Dictionary.translate("open.images.tooltip"));

		JButton btnClear = new JButton("Clear list");
		btnClear.addActionListener(actClearList);

		JButton btnStartAll = new JButton();
		btnStartAll.addActionListener(actAnalyzeFilesAll);
		btnStartAll.setIcon(Resources.getIcon(Icons.ANALYZE_FILES_ALL_ICON));
		btnStartAll.setToolTipText(Dictionary.translate("analyze.files.all.tooltip"));

		pnlOptions.add(btnOpen);
		pnlOptions.add(btnClear);
		pnlOptions.add(btnStartAll);

		this.lsmFiles = new FileListModel();
		this.lstFiles = new JList<File>(lsmFiles);
		this.lstFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.uiView = new UIFormView(omrContext, null);

		List<String> header = Arrays.asList(omrContext.getTemplate().getHeader());
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		this.tblResult = new JTable();
		this.tblResult.setModel(new UIResultTableModel(header, results));
		this.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		add(pnlOptions, BorderLayout.NORTH);
		add(new JScrollPane(lstFiles), BorderLayout.WEST);
		add(this.uiView, BorderLayout.CENTER);
		add(new JScrollPane(this.tblResult), BorderLayout.SOUTH);
	}

	public String getItemByIndex(int index) {
		lstFiles.setSelectedIndex(index);
		return lstFiles.getSelectedValue().toString();
	}

	public String getSelectedItem() {
		return lstFiles.getSelectedValue().toString();
	}

	public int getSelectedItemIndex() {
		return (lstFiles.isSelectionEmpty()) ? 0 : lstFiles.getSelectedIndex();
	}

	public void selectFileAt(int index) {
		lstFiles.setSelectedIndex(index);
	}

	public void update() {
		lsmFiles.update();
	}

	public List<Map<String, String>> getResults(Map<String, OMRModel> filledForms) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		for (Entry<String, OMRModel> filledForm : filledForms.entrySet()) {
			OMRModel form = filledForm.getValue();
			List<FormField> fields = form.getFields();

			HashMap<String, String> result = new HashMap<String, String>();
			result.put(Dictionary.translate("first.csv.column"), filledForm.getKey());

			for (FormField field : fields) {
				result.put(field.getName(), field.getValues());
			}

			results.add(result);
		}
		return results;
	}
}
