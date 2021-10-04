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
	private JList<File> lstFiles;
	private final FileListModel lsmFiles;
	private final OMRContext omrContext;
	private JScrollPane scrollPane;
	private ImageFrame uiView;
	private int analyzedFileIndex = 0;
	public OMRModel filledForm;
	public boolean firstPass = true; // TODO whatis this?
	public final List<File> openedFiles;
	public final JTable tblResult;

	/** */
	public final ActionListener actAnalyzeFilesAll = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int threshold = Integer.valueOf(omrContext.getSettings().get(Setting.Threshold));
			int density = Integer.valueOf(omrContext.getSettings().get(Setting.Density));
			int shapeSize = Integer.valueOf(omrContext.getSettings().get(Setting.ShapeSize));

			try {
				if (openedFiles.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"DICT There are no files to scan");
				} else {

					for (int i = 0; i < openedFiles.size(); i++) {
						analyzedFileIndex = i;
						selectFileAt(analyzedFileIndex);
						File imageFile = openedFiles.get(analyzedFileIndex);

						filledForm = new OMRModel(imageFile, omrContext.formTemplate);
						// TODO Why is filledForm global?
						filledForm.findCorners(threshold, density);
						filledForm.findPoints(threshold, density, shapeSize);
						omrContext.filledForms.put(imageFile.getName(), filledForm);
					}


					// TODO HELP!
					// Move the functions getResults and getHeader to appropriate locations
					// Make ResultsGridFrame work, or delete it and tranform output into a table
					List<String> header = Arrays.asList(omrContext.getTemplate().getHeader());
					List<Map<String, String>> results = getResults(omrContext.filledForms);
					UIResultTableModel tbmResult = new UIResultTableModel(header, results);
					tblResult.setModel(tbmResult);
					/* tblResult.append(header.toString());
					tblResult.append("\n");
					tblResult.append(results.toString());
					tblResult.append("\n"); */

					resetFirstPass();
				}
			} catch (IOException e1) {
				new ErrorDialog(e1).setVisible(true);
			} catch (UnsupportedImageException e1) {
				// TODO: Add dictionary entries
				JOptionPane.showMessageDialog(null, Dictionary
						.translate("DICT error.unsupported.image.type"),
						Dictionary.translate("DICT 	error.popup.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	/** */
	public final ActionListener actAnalyzeFilesFirst = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int threshold = Integer.valueOf(omrContext.getSettings().get(Setting.Threshold));
			int density = Integer.valueOf(omrContext.getSettings().get(Setting.Density));
			int shapeSize = Integer.valueOf(omrContext.getSettings().get(Setting.ShapeSize));

			try {
				if (openedFiles.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"DICT There are no files to scan");
				} else {
					if (firstPass) {
						analyzedFileIndex = getSelectedItemIndex();
						firstPass = false;
					} else {
						analyzedFileIndex++;
					}

					if (openedFiles.size() > analyzedFileIndex) {
						selectFileAt(analyzedFileIndex);
						File imageFile = openedFiles.get(analyzedFileIndex);

						filledForm = new OMRModel(imageFile, omrContext.formTemplate);
						filledForm.findCorners(threshold, density);
						filledForm.findPoints(threshold, density, shapeSize);
						// points = filledForm.getFieldPoints();
						omrContext.filledForms.put(imageFile.getName(), filledForm);

						// view.createFormImageFrame(filledForm.getImage(),
						// filledForm, ImageFrame.Mode.MODIFY_POINTS);
						// ImageFrame(model, image, template, mode,
						// null);
						uiView = new ImageFrame(omrContext, null);
						uiView.revalidate();
						uiView.repaint();
						//createResultsGridFrame(filledForm);

					} else {
						resetFirstPass();
					}
				}
			} catch (IOException e1) {
				new ErrorDialog(e1).setVisible(true);
			} catch (UnsupportedImageException e1) {
				JOptionPane.showMessageDialog(null, Dictionary
						.translate("DICT error.unsupported.image.type"),
						Dictionary.translate("DICT error.popup.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	/** */
	public final ActionListener actAnalyzeFilesCurrent = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int threshold = Integer.valueOf(omrContext.getSettings().get(Setting.Threshold));
			int density = Integer.valueOf(omrContext.getSettings().get(Setting.Density));
			int shapeSize = Integer.valueOf(omrContext.getSettings().get(Setting.ShapeSize));

			selectFileAt(analyzedFileIndex);
			File imageFile = openedFiles.get(analyzedFileIndex);
			filledForm = omrContext.getTemplate();
			filledForm.clearPoints();
			filledForm.findPoints(threshold, density, shapeSize);
			// points = filledForm.getFieldPoints();
			omrContext.filledForms.put(imageFile.getName(), filledForm);
			uiView.repaint();
			//createResultsGridFrame(filledForm);
		}
	};

	/** Action for Open Images option */
	public final ActionListener actOpenImages = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// model.openImages();
			omrContext.filledForms.clear();
			openedFiles.clear();
			omrContext.firstPass = true;

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
	 * This private inner class is a custom ListModel which relies on
	 * information contained on OMRModelContext (which files are open).
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

		JButton btnStart = new JButton();
		btnStart.addActionListener(actAnalyzeFilesFirst);
		btnStart.setIcon(Resources.getIcon(Icons.ANALYZE_FILES_ICON));
		btnStart.setToolTipText(Dictionary.translate("analyze.files.tooltip"));

		JButton btnStartAll = new JButton();
		btnStartAll.addActionListener(actAnalyzeFilesAll);
		btnStartAll.setIcon(Resources
				.getIcon(Icons.ANALYZE_FILES_ALL_ICON));
		btnStartAll.setToolTipText(Dictionary
				.translate("analyze.files.all.tooltip"));

		JButton btnReload = new JButton();
		btnReload.addActionListener(actAnalyzeFilesCurrent);
		btnReload.setIcon(Resources
				.getIcon(Icons.ANALYZE_FILES_CURRENT_ICON));
		btnReload.setToolTipText(Dictionary
				.translate("analyze.files.current.tooltip"));

		pnlOptions.add(btnOpen);
		pnlOptions.add(btnClear);
		pnlOptions.add(btnStartAll);
		pnlOptions.add(btnStart);
		pnlOptions.add(btnReload);

		this.lsmFiles = new FileListModel();
		this.lstFiles = new JList<File>(lsmFiles);
		this.lstFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.scrollPane = new JScrollPane(lstFiles);
		this.uiView = new ImageFrame(omrContext, null);

		List<String> header = Arrays.asList(omrContext.getTemplate().getHeader());
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		this.tblResult = new JTable();
		this.tblResult.setModel(new UIResultTableModel(header, results));

		add(pnlOptions, BorderLayout.NORTH);
		add(this.scrollPane, BorderLayout.WEST);
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

	public void resetFirstPass() {
		firstPass = true;
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
