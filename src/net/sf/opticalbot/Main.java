package net.sf.opticalbot;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import net.sf.opticalbot.ui.UIMain;
import net.sf.opticalbot.ui.utilities.ErrorDialog;

public class Main {

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

		// Initializing application model
		final OMRModelContext model = new OMRModelContext();

		// Initializing User Interface
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new UIMain(model).setVisible(true);
			}
		});
	}
}
