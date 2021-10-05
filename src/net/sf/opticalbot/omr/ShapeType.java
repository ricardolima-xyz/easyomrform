package net.sf.opticalbot.omr;

import net.sf.opticalbot.resources.Dictionary;

public enum ShapeType {

	SQUARE, CIRCLE;

	public String toString() {
		return Dictionary.translate("shape."+this.name().toLowerCase());
	}

}