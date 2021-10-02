package net.sf.opticalbot.ui;

import javax.swing.AbstractListModel;

import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.OMRModel;

/**
 * This is a model for the field list displayed in UIOMRModel. It reflects the fields
 * stored in the OMRModel instance, but does not trigger any change to that instance.
 */
public class UIFieldListModel extends AbstractListModel<FormField> {

	private final OMRModel omrModel;

	private static final long serialVersionUID = 1L;

	public UIFieldListModel(OMRModel omrModel) {
		this.omrModel = omrModel;
	}

	@Override
	public int getSize() {
		return omrModel.getFields().size();
	}

	@Override
	public FormField getElementAt(int index) {
		return omrModel.getFields().get(index);
	}

	public void newRows(int indexStart, int indexEnd) {
		fireIntervalAdded(this, indexStart, indexEnd);
	}

}
