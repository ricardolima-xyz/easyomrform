package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.opticalbot.omr.Corner;
import net.sf.opticalbot.omr.FormField;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.OMRContext;
import net.sf.opticalbot.omr.ShapeType;
import net.sf.opticalbot.resources.Style;
import net.sf.opticalbot.resources.Settings.Setting;

public class ImageFrame extends JPanel {

	public enum Mode {
		CornerEdit, VIEW, SETUP_POINTS, MODIFY_POINTS;
	}

	private static final long serialVersionUID = 1L;

	protected final MouseMotionListener mmoImageFrame = new MouseMotionListener() {
		@Override
		public void mouseDragged(MouseEvent e) {
			switch (getMode()) {
				case CornerEdit:
					Corner cornerToEdit = uiOMRModel.getSelectedCorner();
					if (cornerToEdit != null) {
						setCorner(cornerToEdit, showCursorPosition(e));
					} 
					break;
				case SETUP_POINTS:
				case MODIFY_POINTS:
					if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
						FormPoint p = showCursorPosition(e);
						addTemporaryPoint(p);
					}
					break;
				case VIEW:
					if (e.isControlDown()) {
						showCursorPosition(e);
						p2 = new FormPoint(e.getPoint());
						if (p1.dist2(p2) >= 10) {
							double deltaX = (p1.getX() - p2.getX());
							double deltaY = (p1.getY() - p2.getY());
							p1 = p2;
							setScrollBars((int) deltaX, (int) deltaY);
						}
					}
				default:
					break;
			}

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			showCursorPosition(e);
		}
	};

	protected final MouseListener mouImageFrame = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
			switch (getMode()) {
				case CornerEdit:
					Corner cornerToEdit = uiOMRModel.getSelectedCorner();
					if (cornerToEdit != null) {
						setCorner(cornerToEdit, showCursorPosition(e));
					} 
					break;
				case MODIFY_POINTS:
					if (e.getButton() == MouseEvent.BUTTON3) {
						// TODO Resolve
						JOptionPane.showMessageDialog(null, "model.deleteNearestPointTo(getCursorPoint(e));");
					}
					break;
				case VIEW:
				default:
					break;
			}

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setImageCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setImageCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			buttonPressed = e.getButton();
			switch (getMode()) {
				case CornerEdit:
					Corner cornerToEdit = uiOMRModel.getSelectedCorner();
					if (cornerToEdit != null) {
						setCorner(cornerToEdit, showCursorPosition(e));
					} 
					break;
				case SETUP_POINTS:
				case MODIFY_POINTS:
					if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
						FormPoint p = getCursorPoint(e);
						addTemporaryPoint(p);
						break;
					}
				case VIEW:
					if (e.isControlDown()) {
						p1 = new FormPoint(e.getPoint());
						setImageCursor(new Cursor(Cursor.HAND_CURSOR));
					}
				default:
					break;
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			switch (getMode()) {
				case SETUP_POINTS:
				case MODIFY_POINTS:
					if (!e.isControlDown() && (buttonPressed == MouseEvent.BUTTON1)) {
						FormPoint p = getCursorPoint(e);
						clearTemporaryPoint();
						addPoint(p);
					}
					break;
				case VIEW:
					if (e.isControlDown()) {
						setImageCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					}
				default:
					break;
			}

		}
	};

	protected final MouseWheelListener mwhImageFrame = new MouseWheelListener() {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int delta = e.getUnitsToScroll();

			if (e.isControlDown()) {
				setScrollBars(delta, 0);
			} else {
				setScrollBars(0, delta);
			}
			FormPoint p = getCursorPoint(e);
			statusBar.displayCursorPosition(p);
		}

	};

	private FormPoint p1;
	private FormPoint p2;
	private int buttonPressed;
	private final UIOMRModel uiOMRModel;
	private final List<FormPoint> points;
	private OMRContext omrContext;
	public final ImagePanel imagePanel;
	private ImageScrollPane scrollPane;
	public final ImageStatusBar statusBar;
	private Mode mode;

	/**
	 * Create the frame.
	 */
	public ImageFrame(OMRContext omrContext, Mode mode, UIOMRModel uiOMRModel) {
		this.omrContext = omrContext;
		this.mode = mode;
		this.points = new LinkedList<FormPoint>();
		this.uiOMRModel = uiOMRModel;

		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		imagePanel = new ImagePanel();
		scrollPane = new ImageScrollPane(imagePanel, this);
		statusBar = new ImageStatusBar(this.mode);
		add(statusBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public class ImageStatusBar extends JPanel {
		private static final long serialVersionUID = 1L;

		private final JLabel lblPosition;

		public ImageStatusBar(Mode mode) {
			super(new BorderLayout());
			setOpaque(false);
			JLabel lblPosition = new JLabel(new FormPoint().toString(), JLabel.RIGHT);
			lblPosition.setOpaque(false);
			this.lblPosition = lblPosition;
			add(lblPosition, BorderLayout.EAST);
		}

		public void displayCursorPosition(FormPoint p) {
			lblPosition.setText(p.toString());
		}

	}

	private class ImageScrollPane extends JScrollPane {

		private static final long serialVersionUID = 1L;

		public ImageScrollPane(JPanel imagePanel, ImageFrame imageFrame) {
			super(imagePanel);
			verticalScrollBar.setValue(0);
			horizontalScrollBar.setValue(0);
			setWheelScrollingEnabled(false);
			addMouseMotionListener(mmoImageFrame);
			addMouseListener(mouImageFrame);
			addMouseWheelListener(mwhImageFrame);
		}

		public void setScrollBars(int deltaX, int deltaY) {
			horizontalScrollBar.setValue(horizontalScrollBar.getValue() + deltaX);
			verticalScrollBar.setValue(verticalScrollBar.getValue() + deltaY);
		}

		public int getHorizontalScrollBarValue() {
			return horizontalScrollBar.getValue();
		}

		public int getVerticalScrollBarValue() {
			return verticalScrollBar.getValue();
		}
	}

	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private FormPoint temporaryPoint;

		public ImagePanel() {
			super();
			setOpaque(false);
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Style.background);
			g.fillRect(0, 0, omrContext.getTemplate().getWidth(), omrContext.getTemplate().getHeight());
			
			BufferedImage image = omrContext.getTemplate().getImage();
			if (image != null) {
				g.drawImage(image, 0, 0, omrContext.getTemplate().getWidth(), omrContext.getTemplate().getHeight(), this);
			}
			
			drawPoints(g);
			drawCorners(g);
		}

		private void drawPoints(Graphics g) {
			for (FormPoint point : omrContext.getTemplate().getFieldsPoints()) {
				drawPoint(g, point);
			}

			Iterator<FormPoint> iterator = points.iterator();
			while (iterator.hasNext())
				drawPoint(g, iterator.next());

			drawPoint(g, temporaryPoint);
		}

		private void drawPoint(Graphics g, FormPoint point) {
			if (point != null) {
				// TODO Model will contain shape size and shape type
				int shapeSize = Integer.valueOf(omrContext.getSettings().get(Setting.ShapeSize));
				ShapeType shape = ShapeType.valueOf(omrContext.getSettings().get(Setting.Shape));
				int x = (int) point.getX();
				int y = (int) point.getY();

				Graphics g1 = g.create();
				g1.setColor(Style.field);

				// If the point is from a selected field, paint it in other color
				List<FormField> selection = uiOMRModel.lstFields.getSelectedValuesList();
				for (FormField selectedFormField : selection) {
					for (FormPoint selectedFormPoint : selectedFormField.getPoints().values())
						if (point.equals(selectedFormPoint))
							g1.setColor(Style.selectedField);
				}
				
				if (shape.equals(ShapeType.CIRCLE)) {
					g1.fillArc(x - shapeSize, y - shapeSize, 2 * shapeSize, 2 * shapeSize, 0, 360);
				} else if (shape.equals(ShapeType.SQUARE)) {
					g1.fillRect(x - shapeSize, y - shapeSize, 2 * shapeSize, 2 * shapeSize);
				}
				g1.dispose();
			}
		}

		public void drawCorners(Graphics g) {
			Map<Corner, FormPoint> corners = omrContext.getTemplate().getCorners();
			if (!corners.isEmpty()) {
				g.setColor(Style.corner);
				FormPoint p1 = corners.get(Corner.TOP_LEFT);
				FormPoint p2 = corners.get(Corner.TOP_RIGHT);
				FormPoint p3 = corners.get(Corner.BOTTOM_LEFT);
				FormPoint p4 = corners.get(Corner.BOTTOM_RIGHT);
				
				g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
				g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p3.getX(), (int) p3.getY());
				g.drawLine((int) p4.getX(), (int) p4.getY(), (int) p2.getX(), (int) p2.getY());
				g.drawLine((int) p4.getX(), (int) p4.getY(), (int) p3.getX(), (int) p3.getY());
			}
		}

		public void setTemporaryPoint(FormPoint p) {
			temporaryPoint = p;
		}

		public void clearTemporaryPoint() {
			temporaryPoint = null;
		}
	}

	public void setImageCursor(Cursor cursor) {
		imagePanel.setCursor(cursor);
	}

	public void setScrollBars(int deltaX, int deltaY) {
		scrollPane.setScrollBars(deltaX, deltaY);
		repaint();
	}

	public int getHorizontalScrollbarValue() {
		return scrollPane.getHorizontalScrollBarValue();
	}

	public int getVerticalScrollbarValue() {
		return scrollPane.getVerticalScrollBarValue();
	}

	public Mode getMode() {
		return mode;
	}

	public void setTemporaryPoint(FormPoint p) {
		imagePanel.setTemporaryPoint(p);
	}

	public void clearTemporaryPoint() {
		imagePanel.clearTemporaryPoint();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	private void setCorner(Corner corner, FormPoint point) {
		omrContext.getTemplate().setCornerAndUpdateDiagonalAndRotation(corner, point);
		uiOMRModel.updateCornerPosition();
		repaint();
	}

	public void addTemporaryPoint(FormPoint point) {
		setTemporaryPoint(point);
		repaint();
	}

	private FormPoint showCursorPosition(MouseEvent e) {
		FormPoint p = getCursorPoint(e);
		statusBar.displayCursorPosition(p);
		return p;
	}

	public void addPoint(FormPoint p) {
		switch (getMode()) {
			case SETUP_POINTS:
				int rows = uiOMRModel.getRowsNumber();
				int values = uiOMRModel.getValuesNumber();

				if (rows == 1 && values == 1) {
					if (points.isEmpty()) {
						points.add(p);
						uiOMRModel.createFields(points);
						// TODO: REIMPLEMENT DELETED METHOD
						// uiOMRModel.toFront();
					}
				} else {
					if (points.isEmpty() || (points.size() > 1)) {
						points.clear();
						points.add(p);
					} else { // points.size() == 1
						FormPoint p1 = points.get(0);
						points.clear();

						FormPoint orig = omrContext.getTemplate().getCorners().get(Corner.TOP_LEFT);
						double rotation = omrContext.getTemplate().getRotation();

						p1.relativePositionTo(orig, rotation);
						p.relativePositionTo(orig, rotation);

						HashMap<String, Double> delta = omrContext.calcDelta(rows, values, uiOMRModel.getOrientation(), p1,
								p);

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
								FormPoint pi = new FormPoint((p1.getX() + (delta.get("x") * colsMultiplier)),
										(p1.getY() + (delta.get("y") * rowsMultiplier)));
								pi.originalPositionFrom(orig, rotation);
								pts.add(pi);
							}
						}
						setMode(ImageFrame.Mode.VIEW);
						uiOMRModel.createFields(pts);
					}
				}
				break;
			case MODIFY_POINTS:
				// TODO: REIMPLEMENT METHOD
				JOptionPane.showMessageDialog(null,
						"It was supposed to call model.filledForm.addPoint(p); and UIMain's createResultsGridFrame(). Showing this message because refactoring is in progress.");

				break;
			default:
				break;
		}
		repaint();
	}

	private FormPoint getCursorPoint(MouseEvent e) {
		int dx = getHorizontalScrollbarValue();
		int dy = getVerticalScrollbarValue();

		int x = e.getX() + dx;
		int y = e.getY() + dy;

		return new FormPoint(x, y);
	}

	public List<FormPoint> getPoints() {
		return this.points;
	}

}