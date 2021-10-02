package net.sf.opticalbot.omr;

import net.sf.opticalbot.resources.Dictionary;

public enum Orientation {
	QUESTIONS_BY_ROWS(Dictionary.translate("questions.by.rows")), QUESTIONS_BY_COLS(
			Dictionary.translate("questions.by.cols"));

	private String value;

	Orientation(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}