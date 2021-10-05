package net.sf.opticalbot.ui;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
	private final FileListModel lsmFiles;
	private final OMRContext omrContext;
	private final List<File> openedFiles;
	private final JTable tblResult;
	private final UIFormView uiView;

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
						File imageFile = lsmFiles.getElementAt(i);

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

		JPanel pnlMainToolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlMainToolbar.setOpaque(false);

		JPanel pnlMainMutableToolbar = new JPanel(new CardLayout());
		pnlMainMutableToolbar.setOpaque(false);

		JPanel pnlMain = new JPanel(new CardLayout());
		pnlMain.setOpaque(false);

		String view1 = "DICT Files";
		String view2 = "DICT Result table";
		JComboBox<String> cbxView = new JComboBox<String>();
		cbxView.setModel(new DefaultComboBoxModel<String>(new String[]{view1, view2}));
		cbxView.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent evt) {
				CardLayout cl1 = (CardLayout)(pnlMain.getLayout());
				cl1.show(pnlMain, (String)evt.getItem());
				CardLayout cl2 = (CardLayout)(pnlMainMutableToolbar.getLayout());
				cl2.show(pnlMainMutableToolbar, (String)evt.getItem());
			}
		});
		pnlMainToolbar.add(cbxView);
		pnlMainToolbar.add(pnlMainMutableToolbar);

		JPanel pnlView1 = new JPanel(new BorderLayout());
		pnlView1.setOpaque(false);

		JPanel pnlView1Toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlView1Toolbar.setOpaque(false);

		JButton btnOpen = new JButton();
		btnOpen.addActionListener(actOpenImages);
		btnOpen.setIcon(Resources.getIcon(Icons.OPEN_IMAGES_ICON));
		btnOpen.setToolTipText(Dictionary.translate("open.images.tooltip"));

		JButton btnClose = new JButton();
		// btnClose.addActionListener(actCloseImages); TODO
		btnClose.setIcon(Resources.getIcon(Icons.CLOSE_IMAGES_ICON));
		// btnClear.setToolTipText(Dictionary.translate("clear.images.tooltip")); TODO

		JButton btnClear = new JButton();
		btnClear.addActionListener(actClearList);
		btnClear.setIcon(Resources.getIcon(Icons.CLEAR_IMAGES_ICON));
		// btnClear.setToolTipText(Dictionary.translate("clear.images.tooltip")); TODO

		JButton btnStartAll = new JButton();
		btnStartAll.addActionListener(actAnalyzeFilesAll);
		btnStartAll.setIcon(Resources.getIcon(Icons.ANALYZE_FILES_ALL_ICON));
		btnStartAll.setToolTipText(Dictionary.translate("analyze.files.all.tooltip"));

		pnlView1Toolbar.add(btnOpen);
		pnlView1Toolbar.add(btnClose);
		pnlView1Toolbar.add(btnClear);
		pnlView1Toolbar.add(btnStartAll);

		this.lsmFiles = new FileListModel();
		JList<File> lstFiles = new JList<File>(lsmFiles);
		lstFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		pnlMainMutableToolbar.add(pnlView1Toolbar, view1);

		this.uiView = new UIFormView(omrContext, null);

		pnlView1.add(new JScrollPane(lstFiles), BorderLayout.WEST);
		pnlView1.add(this.uiView, BorderLayout.CENTER);

		JPanel pnlView2 = new JPanel(new BorderLayout());
		pnlView2.setOpaque(false);

		JPanel pnlView2Toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlView2Toolbar.setOpaque(false);

		JButton btnExportResult = new JButton();
		// btnExportResult.addActionListener(actExportResult); TODO
		btnExportResult.setIcon(Resources.getIcon(Icons.EXPORT_RESULT));
		// btnExportResult.setToolTipText(Dictionary.translate("clear.images.tooltip")); TODO

		pnlView2Toolbar.add(btnExportResult);


		pnlMainMutableToolbar.add(pnlView2Toolbar, view2);

		List<String> header = Arrays.asList(omrContext.getTemplate().getHeader());
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		this.tblResult = new JTable();
		this.tblResult.setModel(new UIResultTableModel(header, results));
		this.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		pnlView2.add(new JScrollPane(this.tblResult), BorderLayout.CENTER);

		pnlMain.add(pnlView1, view1);
		pnlMain.add(pnlView2, view2);

		add(pnlMainToolbar, BorderLayout.NORTH);
		add(pnlMain, BorderLayout.CENTER);
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
