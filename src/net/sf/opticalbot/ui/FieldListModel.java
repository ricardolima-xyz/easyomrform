package net.sf.opticalbot.ui;

import javax.swing.AbstractListModel;

import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.OMRModel;

public class FieldListModel extends AbstractListModel<FormField> {

	private final OMRModel omrModel;

	private static final long serialVersionUID = 1L;

	public FieldListModel(OMRModel omrModel) {
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
