package net.sf.opticalbot;

import java.util.HashMap;
import java.util.List;

import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.omr.Orientation;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Settings;
import net.sf.opticalbot.ui.ImageFrame;

/** Main Application back-end. */
public class OMRContext {

	// TODO: CHANGE AND DELETE SF REPOSITORY
	public static final String WEB_PAGE = "http://opticalbot.sourceforge.net/";

	public HashMap<String, OMRModel> filledForms = new HashMap<String, OMRModel>();
	public boolean firstPass = true; // TODO whatis this?
	public OMRModel formTemplate;

	private final Settings settings;

	public OMRContext() {
		this.settings = new Settings();
		String lang = settings.get(Settings.Setting.Language);
		Dictionary.setDictionaryLanguage(lang);
	}

	public HashMap<String, Double> calcDelta(int rows, int values,
			Orientation orientation, FormPoint p1, FormPoint p2) {

		double dX = Math.abs((p2.getX() - p1.getX()));
		double dY = Math.abs((p2.getY() - p1.getY()));
		double valuesDivider = ((values > 1) ? (values - 1) : 1);
		double questionDivider = ((rows > 1) ? (rows - 1) : 1);

		HashMap<String, Double> delta = new HashMap<String, Double>();

		switch (orientation) {
		case QUESTIONS_BY_ROWS:
			delta.put("x", dX / valuesDivider);
			delta.put("y", dY / questionDivider);
			break;
		case QUESTIONS_BY_COLS:
			delta.put("x", dX / questionDivider);
			delta.put("y", dY / valuesDivider);
			break;
		default:
			break;
		}
		return delta;
	}

	public void clearTemporaryPoint(ImageFrame view) {
		view.clearTemporaryPoint();
	}

	// // TODO: what does it do?
	// public void deleteNearestPointTo(FormPoint cursorPoint) {
	// if (filledForm != null) {
	// filledForm.removePoint(cursorPoint);
	// view.createResultsGridFrame();
	// } else {
	// formTemplate.removePoint(cursorPoint);
	// }
	// view.repaint();
	// }
	//
	// public OMRModel getFilledForm() {
	// return filledForm;
	// }

//	public Dimension getDesktopSize() {
//		return view.getDesktopSize();
//	}

	public Settings getSettings() {
		return settings;
	}

	public OMRModel getTemplate() {
		return formTemplate;
	}

	// public void resetFirstPass() {
	// firstPass = true;
	// }

	public void setTemplate(OMRModel formTemplate) {
		this.formTemplate = formTemplate;
	}

//	public void setView(UIMain view) {
//		this.view = view;
//	}

	public void updateTemplate(List<FormField> fields) {
		formTemplate.setFields(fields);
	}

}