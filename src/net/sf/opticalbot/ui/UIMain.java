package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import net.sf.opticalbot.App;
import net.sf.opticalbot.omr.OMRContext;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.omr.OMRModelFactory;
import net.sf.opticalbot.omr.exception.OMRModelLoadException;
import net.sf.opticalbot.omr.exception.OMRModelSaveException;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Languages;
import net.sf.opticalbot.resources.Settings;
import net.sf.opticalbot.ui.utilities.ErrorDialog;
import net.sf.opticalbot.ui.utilities.Hyperlink;
import net.sf.opticalbot.ui.utilities.HyperlinkException;

/** Main Application Window */
public class UIMain extends JFrame {

	private UIOMRModel uiOMRModel;
	private JMenuItem mniClose;
	private JMenuItem mniSave;
	private JMenuItem mniSaveAs;
	private final OMRContext omrContext;
	private final UIMain instance = this;
	private static final long serialVersionUID = 1L;

	/** Action for About screen */
	public final ActionListener actAbout = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new UIAbout(instance).setVisible(true);
		}
	};

	/** Action for Exit option */
	public final ActionListener actExit = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
			System.exit(0);
		}
	};

	/** Action for Help option */
	public final ActionListener actHelp = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Hyperlink.open(App.appWebSite);
			} catch (HyperlinkException e1) {
				new ErrorDialog(e1).setVisible(true);
			}
		}
	};

	/** Action for new model */
	public final ActionListener actNew = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			OMRModel omrModel = new OMRModel();
			omrContext.setTemplate(omrModel);
			UIOMRModel uiOMRModel = new UIOMRModel(omrContext, instance);
			addUIOMRModel(uiOMRModel);
		}
	};

	/** Action for open template */
	public final ActionListener actOpen = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				UIFileChooser flc = new UIFileChooser();
				flc.setMultiSelectionEnabled(false);
				flc.setTemplateFilter();
				int returnValue = flc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					OMRModel loadedOMRModel = OMRModelFactory.load(flc.getSelectedFile());
					omrContext.setTemplate(loadedOMRModel);
					UIOMRModel uiOMRModel = new UIOMRModel(omrContext, instance);
					addUIOMRModel(uiOMRModel);
				} else {
					// User clicked "cancel" on open file dialog. Aborting.
					return;
				}
			} catch (OMRModelLoadException e1) {
				JOptionPane.showMessageDialog(instance,
						Dictionary.translate("template.not.loaded"),
						Dictionary.translate("template.not.loaded.popup.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	public final ActionListener actClose = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			omrContext.setTemplate(null);
			removeUIOMRModel();
		}
	};

	public final ActionListener actSave = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				File file = omrContext.getTemplate().getFile();
				if (file == null) {
					// OMRModel has not been saved before. Showing File Dialog
					UIFileChooser flc = new UIFileChooser();
					flc.setTemplateFilter();
					if (flc.showSaveDialog(instance) == JFileChooser.APPROVE_OPTION) {
						file = flc.getSelectedFile(); // User selected a file name.
					} else {
						return; // User clicked "cancel". Aborting.
					}
				}
				// At this point, OMRModel has a file associated with it.
				omrContext.getTemplate().setFile(file);
				OMRModelFactory.save(omrContext.getTemplate());
				JOptionPane.showMessageDialog(instance,
						Dictionary.translate("template.saved"),
						Dictionary.translate("template.saved.popup.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (OMRModelSaveException e1) {
				JOptionPane.showMessageDialog(instance,
						Dictionary.translate("template.not.saved"),
						Dictionary.translate("template.not.saved.popup.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	public final ActionListener actSaveAs = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				UIFileChooser flc = new UIFileChooser();
				flc.setTemplateFilter();
				if (flc.showSaveDialog(instance) == JFileChooser.APPROVE_OPTION) {
					// User selected a file name.
					File file = flc.getSelectedFile();
					omrContext.getTemplate().setFile(file);
					OMRModelFactory.save(omrContext.getTemplate());
					JOptionPane.showMessageDialog(instance,
						Dictionary.translate("template.saved"),
						Dictionary.translate("template.saved.popup.title"),
						JOptionPane.INFORMATION_MESSAGE);
				} else {
					return; // User clicked "cancel". Aborting.
				}
			} catch (OMRModelSaveException e1) {
				JOptionPane.showMessageDialog(instance,
						Dictionary.translate("template.not.saved"),
						Dictionary.translate("template.not.saved.popup.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	/** Action for options pane */
	public final ActionListener actOptions = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new UIOptions(omrContext, instance).setVisible(true);
		}
	};

	/** */
	public final ActionListener actLanguage = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButtonMenuItem object = (JRadioButtonMenuItem) e.getSource();
			omrContext.getSettings().set(Settings.Setting.Language, object.getName());
			omrContext.getSettings().store();
			JOptionPane.showMessageDialog(instance, Dictionary.translate("language.changed.message"),
					Dictionary.translate("settings.popup.title"), JOptionPane.WARNING_MESSAGE);
		}
	};

	/**
	 * Constructor for UIMain - Main User Interface - Application Window.
	 */
	public UIMain(OMRContext omrContext) {
		this.omrContext = omrContext;

		// Window appearance and behavior
		setTitle(App.appName);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Menu bar (start)
		JMenuItem mniHelp = new JMenuItem(Dictionary.translate("help"));
		mniHelp.addActionListener(actHelp);
		mniHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		JMenuItem mniAbout = new JMenuItem(Dictionary.translate("about"));
		mniAbout.addActionListener(actAbout);
		mniAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.ALT_DOWN_MASK));

		JMenu mnuHelp = new JMenu(Dictionary.translate("help.menu"));
		mnuHelp.add(mniHelp);
		mnuHelp.add(mniAbout);

		JMenuItem mniNew = new JMenuItem(Dictionary.translate("DICT New model"));
		mniNew.addActionListener(actNew);
		mniNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));

		JMenuItem mniOpen = new JMenuItem(Dictionary.translate("DICT Open model..."));
		mniOpen.addActionListener(actOpen);
		mniOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

		JMenuItem mniClose = new JMenuItem(Dictionary.translate("DICT Close model..."));
		mniClose.setEnabled(false);
		mniClose.addActionListener(actClose);
		mniClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
		this.mniClose = mniClose;

		JMenuItem mniSave = new JMenuItem(Dictionary.translate("DICT Save model"));
		mniSave.setEnabled(false);
		mniSave.addActionListener(actSave);
		this.mniSave = mniSave;

		JMenuItem mniSaveAs = new JMenuItem(Dictionary.translate("DICT Save model as..."));
		mniSaveAs.setEnabled(false);
		mniSaveAs.addActionListener(actSaveAs);
		this.mniSaveAs = mniSaveAs;

		JMenuItem mniExit = new JMenuItem(Dictionary.translate("exit"));
		mniExit.addActionListener(actExit);
		mniExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));

		JMenu mnuFile = new JMenu(Dictionary.translate("file.menu"));
		mnuFile.add(mniNew);
		mnuFile.add(mniOpen);
		mnuFile.add(mniClose);
		mnuFile.add(new JSeparator(JSeparator.HORIZONTAL));
		mnuFile.add(mniSave);
		mnuFile.add(mniSaveAs);
		mnuFile.add(new JSeparator(JSeparator.HORIZONTAL));
		mnuFile.add(mniExit);

		String language = omrContext.getSettings().get(Settings.Setting.Language);
		JMenu menuBuilder = new JMenu(Dictionary.translate("language"));
		JRadioButtonMenuItem languageItem;
		ButtonGroup buttonGroup = new ButtonGroup();
		for (String l : Languages.getAvailableLanguages()) {
			languageItem = new JRadioButtonMenuItem(Dictionary.translate("language." + l));
			if (l.equals(language)) {
				languageItem.setSelected(true);
			}
			languageItem.addActionListener(actLanguage);
			languageItem.setName(l); // Name is used on ActionListener
			buttonGroup.add(languageItem);
			menuBuilder.add(languageItem);
		}
		JMenuItem mniLanguage = menuBuilder;

		JMenuItem mniOptions = new JMenuItem(Dictionary.translate("options"));
		mniOptions.addActionListener(actOptions);
		mniOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));

		JMenu mnuSettings = new JMenu(Dictionary.translate("settings.menu"));
		mnuSettings.add(mniLanguage);
		mnuSettings.add(mniOptions);

		JMenuBar mnbMain = new JMenuBar();
		mnbMain.add(mnuFile);
		mnbMain.add(mnuSettings);
		mnbMain.add(mnuHelp);
		setJMenuBar(mnbMain);
		// Menu bar (end)
	}

	/**
	 * This method adds an UIOMRModel (the main user interface for OMR Models) to
	 * the main window. If there is another UIOMRModel on the main window, this
	 * method removes the current ui before adding the new one.
	 */
	private void addUIOMRModel(UIOMRModel newUIOMRModel) {
		if (this.uiOMRModel != null)
			this.remove(this.uiOMRModel);
		this.uiOMRModel = newUIOMRModel;
		this.mniClose.setEnabled(true);
		this.mniSave.setEnabled(true);
		this.mniSaveAs.setEnabled(true);
		add(newUIOMRModel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private void removeUIOMRModel() {
		this.remove(this.uiOMRModel);
		this.mniClose.setEnabled(false);
		this.mniSave.setEnabled(false);
		this.mniSaveAs.setEnabled(false);
		revalidate();
		repaint();
	}

}
