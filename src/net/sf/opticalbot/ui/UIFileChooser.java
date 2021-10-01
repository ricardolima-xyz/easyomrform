package net.sf.opticalbot.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.resources.Dictionary;

import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public class UIFileChooser extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UIFileChooser() {
		super();
	}

	private File chooseFile() {
		File file = null;
		int returnValue = showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			file = getSelectedFile();
		}
		return file;
	}

	private File[] chooseFiles() {
		File[] files = null;
		int returnValue = showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			files = getSelectedFiles();
		}
		return files;
	}

	public File[] chooseImages() {
		setMultiSelectionEnabled(true);
		setImagesFilter();
		return chooseFiles();
	}

	public File chooseImage() {
		setMultiSelectionEnabled(false);
		setImagesFilter();
		return chooseFile();
	}

	public File chooseTemplate() {
		setMultiSelectionEnabled(false);
		setTemplateFilter();
		return chooseFile();
	}

	public void setImagesFilter() {
		resetChoosableFileFilters();

		// Creating a set for preveting duplication of entries
		Set<String> setOfExtensions = new TreeSet<String>();
		// Iterating all possible image suffixes the current jvm can open
		for (String suffix : ImageIO.getReaderFileSuffixes()) {
			setOfExtensions.add(suffix);
		}
		// Creating the individual file filters
		for (String ext : setOfExtensions)
			setFileFilter(new FileNameExtensionFilter(ext.toUpperCase(), ext));
		// Creating the all images file filter (the one which opens any
		// supported image type)
		FileNameExtensionFilter allImagesFilter = new FileNameExtensionFilter(
				Dictionary.translate("all.images"),
				ImageIO.getReaderFileSuffixes());
		setFileFilter(allImagesFilter);
	}

	public void setTemplateFilter() {
		resetChoosableFileFilters();
		FileNameExtensionFilter templateFilter = new FileNameExtensionFilter(
				Dictionary.translate("template.file"), "xtmpl");
		setFileFilter(templateFilter);
	}

	private void setCsvFilter() {
		resetChoosableFileFilters();
		FileNameExtensionFilter templateFilter = new FileNameExtensionFilter(
				Dictionary.translate("csv.file"), "csv");
		setFileFilter(templateFilter);
	}

	public File saveCsvAs(File file, HashMap<String, OMRModel> filledForms) {
		String[] header = getHeader(filledForms);
		ArrayList<HashMap<String, String>> results = getResults(filledForms,
				header);

		ICsvMapWriter mapWriter = null;
		try {
			try {
				setMultiSelectionEnabled(false);
				setCsvFilter();
				setSelectedFile(file);

				int returnValue = showSaveDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					file = getSelectedFile();
					mapWriter = new CsvMapWriter(new FileWriter(file),
							CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
					mapWriter.writeHeader(header);

					for (HashMap<String, String> result : results) {
						mapWriter.write(result, header);
					}
				}
			} finally {
				if (mapWriter != null) {
					mapWriter.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private ArrayList<HashMap<String, String>> getResults(
			HashMap<String, OMRModel> filledForms, String[] header) {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		for (Entry<String, OMRModel> filledForm : filledForms.entrySet()) {
			OMRModel form = filledForm.getValue();
			List<FormField> fields = form.getFields();

			HashMap<String, String> result = new HashMap<String, String>();
			result.put(header[0], filledForm.getKey());
			for (int i = 1; i < header.length; i++) {
				FormField field = fields.get(i - 1);
				result.put(header[i], field.getValues());
			}

			results.add(result);
		}
		return results;
	}

	private String[] getHeader(HashMap<String, OMRModel> filledForms) {
		String aKey = (String) filledForms.keySet().toArray()[0];
		OMRModel aForm = filledForms.get(aKey);
		String[] header = (String[]) aForm.getHeader();
		return header;
	}
}
