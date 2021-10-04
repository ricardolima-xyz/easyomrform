package net.sf.opticalbot.ui;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sf.opticalbot.resources.Dictionary;

public class UIFileChooser extends JFileChooser {

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
}
