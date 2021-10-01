package net.sf.opticalbot.ui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

import net.sf.opticalbot.OMRModelContext;
import net.sf.opticalbot.omr.Corner;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.OMRModel;

public class ImageFrameController implements MouseMotionListener,
		MouseInputListener, MouseWheelListener {

	private OMRModelContext model;
	private ImageFrame view;
	private FormPoint p1;
	private FormPoint p2;
	private int buttonPressed;
	private final UIOMRModel uiOMRModel;
	private final List<FormPoint> points;

	public ImageFrameController(OMRModelContext model, UIOMRModel uiOMRModel) {
		this.model = model;
		this.uiOMRModel = uiOMRModel;
		this.points = new LinkedList<FormPoint>();
	}

	public void add(ImageFrame view) {
		this.view = view;
	}

	private void setCorner(Corner corner, FormPoint point) {
		OMRModel template = model.getTemplate();
		template.setCornerAndUpdateDiagonalAndRotation(corner, point);
		view.showCornerPosition();
		view.repaint();
	}

	public void addTemporaryPoint(FormPoint point) {
		view.setTemporaryPoint(point);
		view.repaint();
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {
		switch (view.getMode()) {
		case SETUP_POINTS:
		case MODIFY_POINTS:
			if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
				FormPoint p = showCursorPosition(e);
				Corner corner = view.statusBar.getSelectedCorner();
				if (corner != null) {
					setCorner(corner, p);
				} else {
					addTemporaryPoint(p);
				}
				break;
			}
		case VIEW:
			if (e.isControlDown()) {
				showCursorPosition(e);
				p2 = new FormPoint(e.getPoint());
				if (p1.dist2(p2) >= 10) {
					double deltaX = (p1.getX() - p2.getX());
					double deltaY = (p1.getY() - p2.getY());
					p1 = p2;
					view.setScrollBars((int) deltaX, (int) deltaY);
				}
			}
		default:
			break;
		}

	}

	private FormPoint showCursorPosition(MouseEvent e) {
		FormPoint p = getCursorPoint(e);
		view.statusBar.displayPointPosition(p);
		return p;
	}

	public void mouseMoved(MouseEvent e) {
		showCursorPosition(e);
	}

	// MouseInputListener
	public void mouseClicked(MouseEvent e) {
		switch (view.getMode()) {
		case MODIFY_POINTS:
			if (e.getButton() == MouseEvent.BUTTON3) {
				JOptionPane.showMessageDialog(null,
						"model.deleteNearestPointTo(getCursorPoint(e));");

			}
			break;
		case VIEW:
		default:
			break;
		}
	}

	public void mousePressed(MouseEvent e) {
		buttonPressed = e.getButton();
		switch (view.getMode()) {
		case SETUP_POINTS:
		case MODIFY_POINTS:
			if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
				FormPoint p = getCursorPoint(e);
				Corner corner = view.statusBar.getSelectedCorner();

				if (corner != null) {
					setCorner(corner, p);
				} else {
					addTemporaryPoint(p);
				}
				break;
			}
		case VIEW:
			if (e.isControlDown()) {
				p1 = new FormPoint(e.getPoint());
				view.setImageCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		default:
			break;
		}

	}

	public void addPoint(ImageFrame view, FormPoint p) {
		switch (view.getMode()) {
		case SETUP_POINTS:
			int rows = uiOMRModel.getRowsNumber();
			int values = uiOMRModel.getValuesNumber();

			if (rows == 1 && values == 1) {
				if (points.isEmpty()) {
					points.add(p);
					uiOMRModel.createFields(points);
//					uiOMRModel.toFront();
				}
			} else {
				if (points.isEmpty() || (points.size() > 1)) {
					points.clear();
					points.add(p);
				} else { // points.size() == 1
					FormPoint p1 = points.get(0);
					points.clear();

					FormPoint orig = model.getTemplate().getCorners()
							.get(Corner.TOP_LEFT);
					double rotation = model.getTemplate().getRotation();

					p1.relativePositionTo(orig, rotation);
					p.relativePositionTo(orig, rotation);

					HashMap<String, Double> delta = model.calcDelta(rows,
							values, uiOMRModel.getOrientation(), p1, p);

					List<FormPoint> pts = new LinkedList<FormPoint>();
					double rowsMultiplier;
					double colsMultiplier;
					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < values; j++) {
							switch (uiOMRModel.getOrientation()) {
							case QUESTIONS_BY_COLS:
								rowsMultiplier = j;
								colsMultiplier = i;
								break;
							case QUESTIONS_BY_ROWS:
							default:
								rowsMultiplier = i;
								colsMultiplier = j;
								break;
							}
							FormPoint pi = new FormPoint(
									(p1.getX() + (delta.get("x") * colsMultiplier)),
									(p1.getY() + (delta.get("y") * rowsMultiplier)));
							pi.originalPositionFrom(orig, rotation);
							pts.add(pi);
						}
					}
					view.setMode(ImageFrame.Mode.VIEW);
					uiOMRModel.createFields(pts);
				}
			}
			break;
		case MODIFY_POINTS:

			JOptionPane
					.showMessageDialog(
							null,
							"It was supposed to call model.filledForm.addPoint(p); and UIMain's createResultsGridFrame(). Showing this message because refactoring is in progress.");

			break;
		default:
			break;
		}
		view.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		switch (view.getMode()) {
		case SETUP_POINTS:
		case MODIFY_POINTS:
			if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
				FormPoint p = getCursorPoint(e);
				Corner corner = view.statusBar.getSelectedCorner();
				if (corner != null) {
					setCorner(corner, p);
					view.statusBar.resetCornerButtons();
				} else {
					model.clearTemporaryPoint(view);
					addPoint(view, p);
				}
				break;
			}
		case VIEW:
			if (e.isControlDown()) {
				view.setImageCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			}
		default:
			break;
		}
	}

	public void mouseEntered(MouseEvent e) {
		view.setImageCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	public void mouseExited(MouseEvent e) {
		view.setImageCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int delta = e.getUnitsToScroll();

		if (e.isControlDown()) {
			view.setScrollBars(delta, 0);
		} else {
			view.setScrollBars(0, delta);
		}

		FormPoint p = getCursorPoint(e);
		view.statusBar.displayPointPosition(p);
	}

	private FormPoint getCursorPoint(MouseEvent e) {
		int dx = view.getHorizontalScrollbarValue();
		int dy = view.getVerticalScrollbarValue();

		int x = e.getX() + dx;
		int y = e.getY() + dy;

		return new FormPoint(x, y);
	}

	public List<FormPoint> getPoints() {
		return this.points;
	}

}
