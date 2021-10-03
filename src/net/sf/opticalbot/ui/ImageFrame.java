package net.sf.opticalbot.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.sf.opticalbot.OMRContext;
import net.sf.opticalbot.omr.Corner;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.ShapeType;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Settings.Setting;
import net.sf.opticalbot.ui.utilities.SpringUtilities;

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
			statusBar.displayPointPosition(p);
		}

	};

	private FormPoint p1;
	private FormPoint p2;
	private int buttonPressed;
	private final UIOMRModel uiOMRModel;
	private final List<FormPoint> points;
	private OMRContext model;
	public final ImagePanel imagePanel;
	private ImageScrollPane scrollPane;
	public final ImageStatusBar statusBar;
	private Mode mode;

	/**
	 * Create the frame.
	 */
	public ImageFrame(OMRContext model, Mode mode, UIOMRModel uiOMRModel) {
		this.model = model;
		this.mode = mode;
		this.points = new LinkedList<FormPoint>();
		this.uiOMRModel = uiOMRModel;

		this.setLayout(new BorderLayout());
		imagePanel = new ImagePanel();
		scrollPane = new ImageScrollPane(imagePanel, this);
		statusBar = new ImageStatusBar(this.mode);
		add(scrollPane, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
	}

	public class ImageStatusBar extends JPanel {
		private static final long serialVersionUID = 1L;

		private final JTextField txfXPosition;
		private final JTextField txfYPosition;

		public ImageStatusBar(Mode mode) {
			super();
			SpringLayout layout = new SpringLayout();
			setLayout(layout);

			this.txfXPosition = new JTextField(10);
			this.txfXPosition.setEditable(false);
			this.txfYPosition = new JTextField(10);
			this.txfYPosition.setEditable(false);

			if (model.getTemplate() != null)
				showCornerPosition();

			add(new JLabel(Dictionary.translate("x.cursor.position.label")));
			add(txfXPosition);
			add(new JLabel(Dictionary.translate("y.cursor.position.label")));
			add(txfYPosition);

			SpringUtilities.makeCompactGrid(this, 1, 4, 1, 1, 3, 3);
		}

		public void displayPointPosition(FormPoint p) {
			txfXPosition.setText(Double.toString(p.getX()));
			txfYPosition.setText(Double.toString(p.getY()));
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
		private int width;
		private int height;
		private int shapeSize;
		private ShapeType shape;
		private int border = 1;
		private FormPoint temporaryPoint;

		public ImagePanel() {
			super();
			// TODO This will go to model itself
			this.shapeSize = Integer.valueOf(model.getSettings().get(Setting.ShapeSize));
			this.shape = ShapeType.valueOf(model.getSettings().get(Setting.Shape));
		}

		@Override
		public void paintComponent(Graphics g) {
			BufferedImage image = model.getTemplate().getImage();
			width = (image == null) ? 0 : image.getWidth();
			height = (image == null) ? 0 : image.getHeight();
			setPreferredSize(new Dimension(width, height));
			g.drawImage(image, 0, 0, width, height, this);
			if (model.getTemplate() != null) {
				drawPoints(g);
				drawCorners(g);
			}
		}

		private void drawPoints(Graphics g) {
			for (FormPoint point : model.getTemplate().getFieldsPoints()) {
				drawPoint(g, point);
			}

			Iterator<FormPoint> iterator = points.iterator();
			while (iterator.hasNext())
				drawPoint(g, iterator.next());

			drawPoint(g, temporaryPoint);
		}

		private void drawPoint(Graphics g, FormPoint point) {
			if (point != null) {
				int x = (int) point.getX() - border;
				int y = (int) point.getY() - border;

				Graphics g1 = g.create();
				g1.setColor(Color.RED);
				if (shape.equals(ShapeType.CIRCLE)) {
					g1.fillArc(x - shapeSize, y - shapeSize, 2 * shapeSize, 2 * shapeSize, 0, 360);
				} else if (shape.equals(ShapeType.SQUARE)) {
					g1.fillRect(x - shapeSize, y - shapeSize, 2 * shapeSize, 2 * shapeSize);
				}
				g1.dispose();
			}
		}

		public void drawCorners(Graphics g) {
			Map<Corner, FormPoint> corners = model.getTemplate().getCorners();
			if (!corners.isEmpty()) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setColor(new Color(0, 255, 0, 128));
				FormPoint p1 = corners.get(Corner.TOP_LEFT);
				FormPoint p2 = corners.get(Corner.TOP_RIGHT);
				FormPoint p3 = corners.get(Corner.BOTTOM_LEFT);
				FormPoint p4 = corners.get(Corner.BOTTOM_RIGHT);
				
				g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
				g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p3.getX(), (int) p3.getY());
				g2d.drawLine((int) p4.getX(), (int) p4.getY(), (int) p2.getX(), (int) p2.getY());
				g2d.drawLine((int) p4.getX(), (int) p4.getY(), (int) p3.getX(), (int) p3.getY());
				g2d.dispose();
			}
		}

		public int getImageWidth() {
			return width;
		}

		public int getImageHeight() {
			return height;
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

	public Dimension getImageSize() {
		return new Dimension(imagePanel.getImageWidth(), imagePanel.getImageHeight());
	}

	public void setTemporaryPoint(FormPoint p) {
		imagePanel.setTemporaryPoint(p);
	}

	public void showCornerPosition() {
		if (uiOMRModel != null) // TODO WHY THIS CHECK?
			uiOMRModel.updateCornerPosition();
	}

	public void clearTemporaryPoint() {
		imagePanel.clearTemporaryPoint();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	private void setCorner(Corner corner, FormPoint point) {
		model.getTemplate().setCornerAndUpdateDiagonalAndRotation(corner, point);
		showCornerPosition();
		repaint();
	}

	public void addTemporaryPoint(FormPoint point) {
		setTemporaryPoint(point);
		repaint();
	}

	private FormPoint showCursorPosition(MouseEvent e) {
		FormPoint p = getCursorPoint(e);
		statusBar.displayPointPosition(p);
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

						FormPoint orig = model.getTemplate().getCorners().get(Corner.TOP_LEFT);
						double rotation = model.getTemplate().getRotation();

						p1.relativePositionTo(orig, rotation);
						p.relativePositionTo(orig, rotation);

						HashMap<String, Double> delta = model.calcDelta(rows, values, uiOMRModel.getOrientation(), p1,
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