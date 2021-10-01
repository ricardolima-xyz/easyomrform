package net.sf.opticalbot.omr;

import java.awt.Point;

//TODO: This should extend Point2D.Double
public class FormPoint {

	private double x;
	private double y;

	public FormPoint() {
		this.x = 0;
		this.y = 0;
	}

	public FormPoint(Point p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	public FormPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double dist2(FormPoint c2) {
		double dx = x - c2.getX();
		double dy = y - c2.getY();
		return (dx * dx) + (dy * dy);
	}

	public double dist2(double x2, double y2) {
		double dx = x - x2;
		double dy = y - y2;
		return (dx * dx) + (dy * dy);
	}

	public String toString() {
		return "[" + (int) getX() + "," + (int) getY() + "]";
	}

	public static FormPoint toPoint(String str) {
		String clearStr = str.substring(1, str.length() - 1);
		String[] coords = clearStr.split(",");

		return new FormPoint(Double.parseDouble(coords[0]),
				Double.parseDouble(coords[1]));
	}

	private void rotate(double alfa) {
		x = ((x * Math.cos(alfa)) - (y * Math.sin(alfa)));
		y = ((x * Math.sin(alfa)) + (y * Math.cos(alfa)));
	}

	private void move(double dx, double dy) {
		x = x + dx;
		y = y + dy;
	}

	public void scale(double scaleFactor) {
		x = (scaleFactor * x);
		y = (scaleFactor * y);
	}

	public void relativePositionTo(FormPoint o, double alfa) {
		move(0 - o.getX(), 0 - o.getY());
		rotate(alfa);
	}

	public void originalPositionFrom(FormPoint o, double alfa) {
		rotate(0 - alfa);
		move(o.getX(), o.getY());
	}

	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public FormPoint clone() {
		return new FormPoint(x, y);
	}
}
