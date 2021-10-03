package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.sf.opticalbot.App;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Resources;
import net.sf.opticalbot.ui.utilities.ErrorDialog;
import net.sf.opticalbot.ui.utilities.Hyperlink;
import net.sf.opticalbot.ui.utilities.HyperlinkException;
import net.sf.opticalbot.ui.utilities.SpringUtilities;

/** User interface for About screen */
public class UIAbout extends JDialog {

	private static final long serialVersionUID = 1L;

	public final ActionListener actOK = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	public final HyperlinkListener hylAbout = new HyperlinkListener() {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					Hyperlink.open(e.getURL());
				} catch (HyperlinkException e1) {
					new ErrorDialog(e1).setVisible(true);
				}
			}
		}
	};

	/**
	 * Create the frame.
	 */
	public UIAbout(Frame owner) {
		super(owner, true);
		this.setTitle(Dictionary.translate("about.title"));
		this.setResizable(false);

		JPanel pnlAbout = getAboutPanel();
		JPanel pnlLicense = getLicensePanel();

		JButton btnOK = new JButton();
		btnOK.setText(Dictionary.translate("ok.button"));
		btnOK.addActionListener(actOK);

		// Buttons panel
		JPanel pnlButtons = new JPanel(new SpringLayout());
		pnlButtons.add(btnOK);
		SpringUtilities.makeCompactGrid(pnlButtons, 1, 1, 3, 3, 3, 3);

		JTabbedPane tbpAbout = new JTabbedPane(JTabbedPane.TOP);
		tbpAbout.addTab(Dictionary.translate("about.tab.about"), pnlAbout);
		tbpAbout.addTab(Dictionary.translate("about.tab.license"), pnlLicense);

		getContentPane().add(tbpAbout, BorderLayout.CENTER);
		getContentPane().add(pnlButtons, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(owner);
	}

	private JPanel getLicensePanel() {
		JScrollPane licenseTextPanel = getLiceseTextPanel();

		JPanel pnlLicense = new JPanel(new BorderLayout());
		pnlLicense.add(licenseTextPanel, BorderLayout.CENTER);
		return pnlLicense;
	}

	private JScrollPane getLiceseTextPanel() {
		JTextArea textArea = new JTextArea(300, 500);
		textArea.setEditable(false);
		textArea.setTabSize(4);
		textArea.getCaret().setDot(0);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEnabled(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(400, 300));
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

		String licenseText = "";
		try {
			File license = Resources.getLicense();
			FileReader fileReader;
			fileReader = new FileReader(license);
			BufferedReader reader = new BufferedReader(fileReader);

			String temp;
			while ((temp = reader.readLine()) != null) {
				licenseText += temp + "\n";
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		textArea.append(licenseText);
		textArea.getCaret().setDot(0);

		return scrollPane;
	}

	private JPanel getAboutPanel() {
		JEditorPane aboutTextPanel = getAboutTextPanel();
		JPanel pnlAbout = new JPanel(new BorderLayout());
		pnlAbout.add(aboutTextPanel, BorderLayout.CENTER);
		return pnlAbout;
	}

	private JEditorPane getAboutTextPanel() {
		StringBuilder aboutText = new StringBuilder();
		aboutText.append("<h1 align=\"center\">"+App.appName+"</h1>");
		aboutText.append("<h3 align=\"center\">"+Dictionary.translate("about.version")+App.appVersion+"</h1>");
		aboutText.append("<h3 align=\"center\">"+Dictionary.translate("about.author")+"Luiz Ricardo de Lima</h1>");
		aboutText.append("<p  align=\"center\">"+Dictionary.translate("about.text")+"</p>");
		JEditorPane text = new JEditorPane();
		text.setAlignmentX(Component.CENTER_ALIGNMENT);
		text.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		text.setContentType("text/html");
		text.setOpaque(true);
		text.addHyperlinkListener(hylAbout);
		text.setText(aboutText.toString());
		text.setEditable(false);
		return text;
	}

}
