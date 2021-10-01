package net.sf.opticalbot.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.sf.opticalbot.OMRModelContext;
import net.sf.opticalbot.omr.Corner;
import net.sf.opticalbot.omr.FormPoint;
import net.sf.opticalbot.omr.OMRModel;
import net.sf.opticalbot.omr.ShapeType;
import net.sf.opticalbot.resources.Dictionary;
import net.sf.opticalbot.resources.Resources;
import net.sf.opticalbot.resources.ResourcesKeys;
import net.sf.opticalbot.resources.Settings.Setting;
import net.sf.opticalbot.ui.utilities.SpringUtilities;

public class ImageFrame extends JPanel {

	public enum Mode {
		VIEW, SETUP_POINTS, MODIFY_POINTS;
	}

	private static final long serialVersionUID = 1L;

	private final ActionListener actBtnTopLeft = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			statusBar.toggleCornerButton(Corner.TOP_LEFT);
		}
	};

	private final ActionListener actBtnBottomLeft = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			statusBar.toggleCornerButton(Corner.BOTTOM_LEFT);
		}
	};

	private final ActionListener actBtnTopRight = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			statusBar.toggleCornerButton(Corner.TOP_RIGHT);
		}
	};

	private final ActionListener actBtnBottomRight = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			statusBar.toggleCornerButton(Corner.BOTTOM_RIGHT);
		}
	};

	private OMRModelContext model;
	public final ImagePanel imagePanel;
	private ImageScrollPane scrollPane;
	private ImageFrameController controller;
	public final ImageStatusBar statusBar;
	private Mode mode;
	private OMRModel template;

	/**
	 * Create the frame.
	 */
	public ImageFrame(OMRModelContext model, BufferedImage image,
			OMRModel template, Mode mode, UIOMRModel uiOMRModel) {
		this.model = model;
		this.mode = mode;
		this.template = template;

		controller = new ImageFrameController(this.model, uiOMRModel);
		controller.add(this);

		this.setLayout(new BorderLayout());
		imagePanel = new ImagePanel(image);
		scrollPane = new ImageScrollPane(imagePanel);
		statusBar = new ImageStatusBar(this.mode);
		add(scrollPane, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
	}

	public class ImageStatusBar extends JPanel {
		private static final long serialVersionUID = 1L;

		private final JTextField txfXPosition;
		private final JTextField txfYPosition;
		private HashMap<Corner, JButton> cornerButtons = new HashMap<Corner, JButton>();
		private HashMap<Corner, JTextField> cornerPositions = new HashMap<Corner, JTextField>();

		public ImageStatusBar(Mode mode) {
			super();
			SpringLayout layout = new SpringLayout();
			setLayout(layout);

			this.txfXPosition = new JTextField(10);
			this.txfXPosition.setEditable(false);
			this.txfYPosition = new JTextField(10);
			this.txfYPosition.setEditable(false);

			for (Corner corner : Corner.values()) {
				JTextField txf = new JTextField(10);
				txf.setEditable(false);
				cornerPositions.put(corner, txf);
			}

			if (template != null)
				showCornerPosition();
			setCornerButtons(mode);

			add(new JLabel(Dictionary.translate("x.cursor.position.label")));
			add(txfXPosition);
			add(cornerButtons.get(Corner.TOP_LEFT));
			add(cornerPositions.get(Corner.TOP_LEFT));
			add(cornerButtons.get(Corner.TOP_RIGHT));
			add(cornerPositions.get(Corner.TOP_RIGHT));
			add(new JLabel(Dictionary.translate("y.cursor.position.label")));
			add(txfYPosition);
			add(cornerButtons.get(Corner.BOTTOM_LEFT));
			add(cornerPositions.get(Corner.BOTTOM_LEFT));
			add(cornerButtons.get(Corner.BOTTOM_RIGHT));
			add(cornerPositions.get(Corner.BOTTOM_RIGHT));

			SpringUtilities.makeCompactGrid(this, 2, 6, 3, 3, 3, 3);
		}

		private void setCornerButtons(Mode mode) {

			boolean enabled = (mode != Mode.VIEW);

			JButton btnTopLeft = new JButton();
			btnTopLeft.addActionListener(actBtnTopLeft);
			btnTopLeft
					.setIcon(Resources.getIcon(ResourcesKeys.DISABLED_BUTTON));
			btnTopLeft.setSelectedIcon(Resources
					.getIcon(ResourcesKeys.ENABLED_BUTTON));
			btnTopLeft.setEnabled(enabled);
			btnTopLeft.setSelected(false);
			btnTopLeft.setText(Dictionary.translate("top.left.corner"));

			JButton btnBottomLeft = new JButton();
			btnBottomLeft.addActionListener(actBtnBottomLeft);
			btnBottomLeft.setIcon(Resources
					.getIcon(ResourcesKeys.DISABLED_BUTTON));
			btnBottomLeft.setSelectedIcon(Resources
					.getIcon(ResourcesKeys.ENABLED_BUTTON));
			btnBottomLeft.setEnabled(enabled);
			btnBottomLeft.setSelected(false);
			btnBottomLeft.setText(Dictionary.translate("bottom.left.corner"));

			JButton btnTopRight = new JButton();
			btnTopRight.addActionListener(actBtnTopRight);
			btnTopRight.setIcon(Resources
					.getIcon(ResourcesKeys.DISABLED_BUTTON));
			btnTopRight.setSelectedIcon(Resources
					.getIcon(ResourcesKeys.ENABLED_BUTTON));
			btnTopRight.setEnabled(enabled);
			btnTopRight.setSelected(false);
			btnTopRight.setText(Dictionary.translate("top.right.corner"));

			JButton btnBottomRight = new JButton();
			btnBottomRight.addActionListener(actBtnBottomRight);
			btnBottomRight.setIcon(Resources
					.getIcon(ResourcesKeys.DISABLED_BUTTON));
			btnBottomRight.setSelectedIcon(Resources
					.getIcon(ResourcesKeys.ENABLED_BUTTON));
			btnBottomRight.setEnabled(enabled);
			btnBottomRight.setSelected(false);
			btnBottomRight.setText(Dictionary.translate("bottom.right.corner"));

			cornerButtons.put(Corner.TOP_LEFT, btnTopLeft);
			cornerButtons.put(Corner.BOTTOM_LEFT, btnBottomLeft);
			cornerButtons.put(Corner.TOP_RIGHT, btnTopRight);
			cornerButtons.put(Corner.BOTTOM_RIGHT, btnBottomRight);
		}

		public void displayPointPosition(FormPoint p) {
			txfXPosition.setText(Double.toString(p.getX()));
			txfYPosition.setText(Double.toString(p.getY()));
		}

		public void toggleCornerButton(Corner corner) {
			for (Entry<Corner, JButton> entryCorner : cornerButtons.entrySet()) {
				JButton button = entryCorner.getValue();

				if (entryCorner.getKey().equals(corner)) {
					button.setSelected(!button.isSelected());
				} else {
					button.setSelected(false);
				}
			}
		}

		public Corner getSelectedCorner() {
			for (Entry<Corner, JButton> entryCorner : cornerButtons.entrySet()) {
				JButton button = entryCorner.getValue();

				if (button.isSelected()) {
					return entryCorner.getKey();
				}
			}
			return null;
		}

		public void resetCornerButtons() {
			for (Entry<Corner, JButton> entryCorner : cornerButtons.entrySet()) {
				JButton button = entryCorner.getValue();
				button.setSelected(false);
			}
		}

		public void setCornerButtonsEnabled(Mode mode) {
			for (Entry<Corner, JButton> entryCorner : cornerButtons.entrySet()) {
				JButton button = entryCorner.getValue();
				button.setEnabled(mode != Mode.VIEW);
			}
		}

		public void showCornerPosition() {
			for (Entry<Corner, JTextField> entryCorner : cornerPositions
					.entrySet()) {
				JTextField cornerPosition = entryCorner.getValue();

				cornerPosition.setText(template.getCorner(entryCorner.getKey())
						.toString());
			}
		}
	}

	private class ImageScrollPane extends JScrollPane {

		private static final long serialVersionUID = 1L;

		public ImageScrollPane(JPanel imagePanel) {
			super(imagePanel);
			verticalScrollBar.setValue(0);
			horizontalScrollBar.setValue(0);
			setWheelScrollingEnabled(false);
			addMouseMotionListener(controller);
			addMouseListener(controller);
			addMouseWheelListener(controller);
		}

		public void setScrollBars(int deltaX, int deltaY) {
			horizontalScrollBar.setValue(horizontalScrollBar.getValue()
					+ deltaX);
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
		private BufferedImage image;
		private FormPoint temporaryPoint;

		public ImagePanel(BufferedImage image) {
			super();
			this.image = image;
			this.shapeSize = Integer.valueOf(model.getSettings().get(Setting.ShapeSize));
			this.shape = ShapeType.valueOf(model.getSettings().get(Setting.Shape));
		}

		@Override
		public void paintComponent(Graphics g) {
			width = (image == null) ? 0 : image.getWidth();
			height = (image == null) ? 0 : image.getHeight();
			setPreferredSize(new Dimension(width, height));
			g.drawImage(image, 0, 0, width, height, this);
			if (template != null) {
				drawPoints(g);
				drawCorners(g);
			}
		}

		private void drawPoints(Graphics g) {
			for (FormPoint point : template.getFieldsPoints()) {
				drawPoint(g, point);
			}

			Iterator<FormPoint> iterator = controller.getPoints().iterator();
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
					g1.fillArc(x - shapeSize, y - shapeSize, 2 * shapeSize,
							2 * shapeSize, 0, 360);
				} else if (shape.equals(ShapeType.SQUARE)) {
					g1.fillRect(x - shapeSize, y - shapeSize, 2 * shapeSize,
							2 * shapeSize);
				}
				g1.dispose();
			}
		}

		public void drawCorners(Graphics g) {
			Map<Corner, FormPoint> corners = template.getCorners();
			if (!corners.isEmpty()) {
				Graphics2D g2d = (Graphics2D) g.create();
				Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL, 0, new float[] { 5 }, 0);
				g2d.setStroke(dashed);
				g2d.setColor(Color.GREEN);
				FormPoint p1 = corners.get(Corner.TOP_LEFT);
				FormPoint p2 = corners.get(Corner.TOP_RIGHT);
				FormPoint p3 = corners.get(Corner.BOTTOM_LEFT);
				FormPoint p4 = corners.get(Corner.BOTTOM_RIGHT);
				g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(),
						(int) p2.getY());
				g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p3.getX(),
						(int) p3.getY());
				g2d.drawLine((int) p4.getX(), (int) p4.getY(), (int) p2.getX(),
						(int) p2.getY());
				g2d.drawLine((int) p4.getX(), (int) p4.getY(), (int) p3.getX(),
						(int) p3.getY());
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
		return new Dimension(imagePanel.getImageWidth(),
				imagePanel.getImageHeight());
	}

	public void setTemporaryPoint(FormPoint p) {
		imagePanel.setTemporaryPoint(p);
	}

	public void showCornerPosition() {
		statusBar.showCornerPosition();
	}

	@Deprecated
	public OMRModel getTemplate() {
		return template;
	}

	public void clearTemporaryPoint() {
		imagePanel.clearTemporaryPoint();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		statusBar.setCornerButtonsEnabled(mode);
	}
}