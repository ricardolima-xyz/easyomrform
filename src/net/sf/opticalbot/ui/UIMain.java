package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

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

import net.sf.opticalbot.OMRModelContext;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.omr.OMRModelFactory;
import net.sf.opticalbot.omr.exception.OMRModelLoadException;
import net.sf.opticalbot.omr.exception.UnsupportedImageException;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Languages;
import net.sf.opticalbot.resources.Settings;
import net.sf.opticalbot.ui.utilities.ErrorDialog;
import net.sf.opticalbot.ui.utilities.Hyperlink;
import net.sf.opticalbot.ui.utilities.HyperlinkException;

/** Main Application Window */
public class UIMain extends JFrame {

	private final UIMain instance = this;
	private OMRModel omrModel;
	private UIOMRModel uiOMRModel;
	private OMRModelContext model;
	private static final long serialVersionUID = 1L;

	/** Action for About screen */
	public final ActionListener actAbout = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showAboutScreen();
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
				Hyperlink.open(OMRModelContext.WEB_PAGE);
			} catch (HyperlinkException e1) {
				new ErrorDialog(e1).setVisible(true);
			}
		}
	};

	/** Action for new template */
	public final ActionListener actNew = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			UIFileChooser flc = new UIFileChooser();
			flc.setMultiSelectionEnabled(false);
			flc.setImagesFilter();

			if (flc.showOpenDialog(instance) == JFileChooser.APPROVE_OPTION) {
				File imageFile = flc.getSelectedFile();

				try {
					int threshold = model.getSettings().getThreshold();
					int density = model.getSettings().getDensity();

					OMRModel omrModel = new OMRModel(imageFile);
					omrModel.findCorners(threshold, density);
					model.setTemplate(omrModel);

					instance.omrModel = omrModel;
					UIOMRModel uiOMRModel = new UIOMRModel(model, instance);
					addUIOMRModel(uiOMRModel);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,
							Dictionary.translate("io.error"),
							Dictionary.translate("io.error.popup.title"),
							JOptionPane.ERROR_MESSAGE);
				} catch (UnsupportedImageException e2) {
					JOptionPane.showMessageDialog(null, Dictionary
							.translate("DICT error.unsupported.image.type"),
							Dictionary.translate("DICT error.popup.title"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
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
					File file = flc.getSelectedFile();
					OMRModel loadedOMRModel = OMRModelFactory.load(file);
					omrModel = loadedOMRModel;
					model.setTemplate(omrModel);
					UIOMRModel uiOMRModel = new UIOMRModel(model, instance);
					addUIOMRModel(uiOMRModel);
				} else {
					// User clicked "cancel" on open file dialog. Aborting.
					return;
				}
			} catch (OMRModelLoadException e1) {
				new ErrorDialog(e1).setVisible(true);
			}
		}
	};

	public final ActionListener actSave = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	public final ActionListener actSaveAs = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	/** Action for options pane */
	public final ActionListener actOptions = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new UIOptions(model, instance).setVisible(true);
		}
	};

	/** */
	public final ActionListener actLanguage = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButtonMenuItem object = (JRadioButtonMenuItem) e.getSource();
			model.getSettings().set(Settings.Setting.Language, object.getName());
			//model.getSettings().setProperty(Settings.LANG, object.getName());
			model.getSettings().store();
			JOptionPane.showMessageDialog(null,
					Dictionary.translate("language.changed.message"),
					Dictionary.translate("settings.popup.title"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	};

	/**
	 * Constructor for UIMain - Main User Interface - Application Window.
	 */
	public UIMain(OMRModelContext model) {
		this.model = model;

		// Window appearance and behavior
		setTitle(Dictionary.translate("application.title"));
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Creating menu bar (start)

		JMenuItem mniHelp = new JMenuItem(Dictionary.translate("help"));
		mniHelp.addActionListener(actHelp);
		mniHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		JMenuItem mniAbout = new JMenuItem(Dictionary.translate("about"));
		mniAbout.addActionListener(actAbout);
		mniAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
				InputEvent.ALT_DOWN_MASK));

		JMenu mnuHelp = new JMenu(Dictionary.translate("help.menu"));
		mnuHelp.setMnemonic(Dictionary.mnemonic("help.menu.mnemonic"));
		mnuHelp.add(mniHelp);
		mnuHelp.add(mniAbout);

		JMenuItem mniNew = new JMenuItem(Dictionary.translate("DICT New model"));
		mniNew.addActionListener(actNew);
		mniNew.setMnemonic(Dictionary.mnemonic("create.template.mnemonic"));
		mniNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				InputEvent.CTRL_DOWN_MASK));

		JMenuItem mniOpen = new JMenuItem(
				Dictionary.translate("DICT Open model..."));
		mniOpen.addActionListener(actOpen);
		mniOpen.setMnemonic(Dictionary.mnemonic("load.template.mnemonic"));
		mniOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem mniSave = new JMenuItem(
				Dictionary.translate("DICT Save model"));
		mniSave.addActionListener(actSave);

		JMenuItem mniSaveAs = new JMenuItem(
				Dictionary.translate("DICT Save model as..."));
		mniSaveAs.addActionListener(actSaveAs);

		JMenuItem mniExit = new JMenuItem(Dictionary.translate("exit"));
		mniExit.addActionListener(actExit);
		mniExit.setMnemonic(Dictionary.mnemonic("exit.mnemonic"));
		mniExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_DOWN_MASK));

		JMenu mnuFile = new JMenu(Dictionary.translate("file.menu"));
		mnuFile.setMnemonic(Dictionary.mnemonic("file.menu.mnemonic"));
		mnuFile.add(mniNew);
		mnuFile.add(mniOpen);
		mnuFile.add(new JSeparator(JSeparator.HORIZONTAL));
		mnuFile.add(mniSave);
		mnuFile.add(mniSaveAs);
		mnuFile.add(new JSeparator(JSeparator.HORIZONTAL));
		mnuFile.add(mniExit);

		String language = model.getSettings().get(Settings.Setting.Language);
		JMenu menuBuilder = new JMenu(Dictionary.translate("language"));
		JRadioButtonMenuItem languageItem;
		ButtonGroup buttonGroup = new ButtonGroup();
		for (String l : Languages.getAvailableLanguages()) {
			languageItem = new JRadioButtonMenuItem(
					Dictionary.translate("language." + l));
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
		mniOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
				InputEvent.CTRL_DOWN_MASK));

		JMenu mnuSettings = new JMenu(Dictionary.translate("settings.menu"));
		mnuSettings.setMnemonic(Dictionary.mnemonic("settings.menu.mnemonic"));
		mnuSettings.add(mniLanguage);
		mnuSettings.add(mniOptions);

		JMenuBar mnbMain = new JMenuBar();
		mnbMain.add(mnuFile);
		mnbMain.add(mnuSettings);
		mnbMain.add(mnuHelp);
		setJMenuBar(mnbMain);
		// Creating menu bar (end)

	}

	/** Shows the "about" screen. */
	private void showAboutScreen() {
		new UIAbout(this).setVisible(true);
	}

	/**
	 * This method adds an UIOMRModel (the main user interface for OMR Models)
	 * to the main window. If there is another UIOMRModel on the main window,
	 * this method removes the current ui before adding the new one.
	 */
	private void addUIOMRModel(UIOMRModel newUIOMRModel) {
		if (this.uiOMRModel != null)
			this.remove(this.uiOMRModel);
		this.uiOMRModel = newUIOMRModel;
		add(newUIOMRModel, BorderLayout.CENTER);
		revalidate();
	}

}
