package net.sf.opticalbot;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import net.sf.opticalbot.omr.OMRContext;
import net.sf.opticalbot.ui.UIMain;
import net.sf.opticalbot.ui.utilities.ErrorDialog;

public class App {

	public static String appName = "EasyOMRForm";
	public static String appVersion = "0.9";
	public static String appWebSite = "https://github.com/ricardolima-xyz/easyomrform";

	/**
	 * Launches the application.
	 */
	public static void main(String[] args) {

		// Look and Feel settings
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			new ErrorDialog(e);
		}

		// Initializing application context
		OMRContext omrContext = new OMRContext();

		// Initializing user interface
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new UIMain(omrContext).setVisible(true);
			}
		});
	}
}
