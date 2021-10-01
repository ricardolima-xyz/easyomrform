package net.sf.opticalbot.omr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FormField {
	private String name;
	private boolean multiple;
	private HashMap<String, FormPoint> points;

	public FormField(String name, HashMap<String, FormPoint> points) {
		this.name = name;
		this.points = points;
	}

	public FormField(String name) {
		this.name = name;
		points = new HashMap<String, FormPoint>();
	}

	public void setPoint(String value, FormPoint point) {
		points.put(value, point);
	}

	public FormPoint getPoint(String value) {
		return points.get(value);
	}

	public HashMap<String, FormPoint> getPoints() {
		return points;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getValues() {
		ArrayList<String> results = new ArrayList<String>(points.keySet());
		Collections.sort(results);
		StringBuilder builder = new StringBuilder();
		for (String result : results) {
			builder.append(result);
			builder.append('|');
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	public String toString() {
		return getName();
	}
}
