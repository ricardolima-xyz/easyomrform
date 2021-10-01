package net.sf.opticalbot.omr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.sf.opticalbot.omr.exception.UnsupportedImageException;
import net.sf.opticalbot.resources.Dictionary;

public class OMRModel {

	private static final int BLACK = 0;
	private static final int HALF_WINDOW_SIZE = 5;
	private static final int WHITE = 1;
	private static final int WINDOW_SIZE = (HALF_WINDOW_SIZE * 2) + 1;

	private final Map<Corner, FormPoint> corners;
	private final List<FormField> fields;
	private final List<FormPoint> pointList;

	private double diagonal;
	private File templateFile;
	private int halfHeight;
	private int halfWidth;
	private int height;
	private BufferedImage image;
	private double rotation;
	private OMRModel template;
	private int width;

	public OMRModel() {
		this.corners = new HashMap<Corner, FormPoint>();
		this.fields = new LinkedList<FormField>();
		this.pointList = new ArrayList<FormPoint>();
	}

	public OMRModel(File file) throws IOException, UnsupportedImageException {
		this(file, null);
	}

	// TODO: This modelling is dangerous. An OMRModel receiving another image
	// file and another model. This modelling has to be changed.
	public OMRModel(File imageFile, OMRModel template) throws IOException,
			UnsupportedImageException {
		this();
		this.image = ImageIO.read(imageFile);

		// We have to guarantee that the file passed as argument is a valid
		// image file. If it's not, we have to throw an exception that is
		// supposed to be caught on front-end code
		if (this.image == null)
			throw new UnsupportedImageException();

		this.height = image.getHeight();
		this.width = image.getWidth();
		this.halfWidth = (int) (width / 2);
		this.halfHeight = (int) (height / 2);
		this.template = template;
		this.corners.put(Corner.TOP_LEFT, new FormPoint(0, 0));
		this.corners.put(Corner.TOP_RIGHT, new FormPoint(width, 0));
		this.corners.put(Corner.BOTTOM_LEFT, new FormPoint(0, height));
		this.corners.put(Corner.BOTTOM_RIGHT, new FormPoint(width, height));
		this.rotation = 0;
		calculateDiagonal();
	}

	protected void addPointSimple(FormPoint point) {
		pointList.add(point);
	}

	public void addPoint(FormPoint cursorPoint) {
		pointList.add(cursorPoint);
		FormPoint templateOrigin = template.getCorner(Corner.TOP_LEFT);
		double templateRotation = template.getRotation();
		double scale = Math.sqrt(diagonal / template.getDiagonal());
		List<FormPoint> templatePoints = template.getFieldPoints();
		FormPoint point = new FormPoint();
		if (!templatePoints.isEmpty()) {

			FormPoint nearestTemplatePoint = templatePoints.get(0);

			point = nearestTemplatePoint.clone();
			point.relativePositionTo(templateOrigin, templateRotation);
			point.scale(scale);
			point.originalPositionFrom(corners.get(Corner.TOP_LEFT), rotation);

			double firstDistance = cursorPoint.dist2(nearestTemplatePoint);
			for (FormPoint templatePoint : templatePoints) {
				point = templatePoint.clone();
				point.relativePositionTo(templateOrigin, templateRotation);
				point.scale(scale);
				point.originalPositionFrom(corners.get(Corner.TOP_LEFT),
						rotation);

				double lastDistance = cursorPoint.dist2(point);
				if (lastDistance < firstDistance) {
					nearestTemplatePoint = templatePoint;
					firstDistance = lastDistance;
				}
			}

			// Map<String, FormField> templateFields = template.getFields();
			for (FormField templateField : template.getFields()) {

				// FormField fieldValue = templateField;
				for (Entry<String, FormPoint> templatePoint : templateField
						.getPoints().entrySet()) {
					if (nearestTemplatePoint.equals(templatePoint.getValue())) {
						FormField currentField = getField(templateField
								.getName());
						currentField.setPoint(templatePoint.getKey(),
								cursorPoint);
						fields.add(currentField);
						return;
					}
				}

			}

		}
	}

	private FormPoint calcResponsePoint(OMRModel template,
			FormPoint responsePoint) {
		FormPoint point = responsePoint.clone();
		FormPoint templateOrigin = template.getCorner(Corner.TOP_LEFT);
		double templateRotation = template.getRotation();
		double scale = Math.sqrt(diagonal / template.getDiagonal());

		point.relativePositionTo(templateOrigin, templateRotation);
		point.scale(scale);
		point.originalPositionFrom(corners.get(Corner.TOP_LEFT), rotation);
		return point;
	}

	protected void calculateDiagonal() {
		this.diagonal = (corners.get(Corner.TOP_LEFT).dist2(
				corners.get(Corner.BOTTOM_RIGHT)) + corners.get(
				Corner.TOP_RIGHT).dist2(corners.get(Corner.BOTTOM_LEFT))) / 2;
	}

	public double calculateRotation() {
		FormPoint topLeftPoint = corners.get(Corner.TOP_LEFT);
		FormPoint topRightPoint = corners.get(Corner.TOP_RIGHT);

		double dx = (double) (topRightPoint.getX() - topLeftPoint.getX());
		double dy = (double) (topLeftPoint.getY() - topRightPoint.getY());

		return Math.atan(dy / dx);
	}

	public void clearPoints() {
		pointList.clear();
		fields.clear();
	}

	public void findCorners(int threshold, int density) {

		for (Corner position : Corner.values()) {

			FormPoint corner = getCircleCenter(threshold, density, position);
			if (corner != null) {
				corners.put(position, corner);
			}
		}

		calculateDiagonal();
		rotation = calculateRotation();
	}

	public void findPoints(int threshold, int density, int size) {
		boolean found;
		// Map<String, FormField> templateFields = template.getFields();
		// ArrayList<String> fieldNames = new ArrayList<String>(
		// templateFields.keySet());
		// Collections.sort(fieldNames);

		for (FormField templateField : template.getFields()) {

			HashMap<String, FormPoint> fieldPoints = templateField.getPoints();
			List<String> pointNames = new ArrayList<String>(
					fieldPoints.keySet()); // Makes no sense
			Collections.sort(pointNames); // Makes no sense
			found = false;

			for (String pointName : pointNames) {
				FormPoint responsePoint = calcResponsePoint(template,
						fieldPoints.get(pointName));

				if (found = isFilled(responsePoint, threshold, density, size)) {
					FormField filledField = getField(templateField,
							templateField.getName());
					filledField.setPoint(pointName, responsePoint);
					fields.add(filledField);
					pointList.add(responsePoint);
					if (!templateField.isMultiple()) {
						break;
					}
				}
			}

			if (!found) {
				FormField filledField = getField(templateField,
						templateField.getName());
				filledField.setPoint("", null);
				fields.add(filledField);
			}
		}
	}

	// TODO try to understand this function
	private FormPoint getCircleCenter(int threshold, int density,
			Corner position) {
		boolean found = false;
		boolean passed = false;
		double Xc = 0;
		double Yc = 0;
		int centralPoints = 0;
		int dx = 1;
		int dy = 1;
		int x = HALF_WINDOW_SIZE;
		int y = HALF_WINDOW_SIZE;
		int x1 = 0;
		int y1 = 0;
		int stato;
		int pixel;
		int old_pixel;
		// int whites;
		// int currentPixelIndex;
		int[] rgbArray = new int[halfWidth * halfHeight];
		FormPoint[] points = new FormPoint[4];

		switch (position) {
		case TOP_RIGHT:
			x = halfWidth - (HALF_WINDOW_SIZE + 1);
			x1 = width - (halfWidth + 1);
			dx = -1;
			break;
		case BOTTOM_LEFT:
			y = halfHeight - (HALF_WINDOW_SIZE + 1);
			y1 = height - (halfHeight + 1);
			dy = -1;
			break;
		case BOTTOM_RIGHT:
			x = halfWidth - (HALF_WINDOW_SIZE + 1);
			y = halfHeight - (HALF_WINDOW_SIZE + 1);
			x1 = width - (halfWidth + 1);
			y1 = height - (halfHeight + 1);
			dx = -1;
			dy = -1;
			break;
		default:
			break;
		}

		image.getRGB(x1, y1, halfWidth, halfHeight, rgbArray, 0, halfWidth);

		for (int yi = y; (yi < (halfHeight - HALF_WINDOW_SIZE))
				&& (yi >= HALF_WINDOW_SIZE); yi += dy) {
			stato = 0;
			pixel = WHITE;
			old_pixel = pixel;
			// whites = WINDOW_SIZE * WINDOW_SIZE;

			for (int xi = x; (xi < (halfWidth - HALF_WINDOW_SIZE))
					&& (xi >= HALF_WINDOW_SIZE); xi += dx) {

				// currentPixelIndex = ((yi * subImageWidth) + xi);
				// if ((xi > WINDOW_SIZE) && (Math.abs(x - xi) > WINDOW_SIZE)) {
				// if ((rgbArray[currentPixelIndex - (dx * WINDOW_SIZE)] &
				// (0xFF)) > threshold) {
				// whites--;
				// }
				// if ((rgbArray[currentPixelIndex] & (0xFF)) > threshold) {
				// whites++;
				// }
				// }

				// pixel = (whites > HALF_WINDOW_SIZE) ? WHITE : BLACK;
				pixel = isWhite(xi, yi, rgbArray, threshold, density);

				if (pixel != old_pixel) {
					stato++;
					old_pixel = pixel;
					switch (stato) {
					case 1:
						// points[0] = new FormPoint(x1 + xi - dx *
						// HALF_WINDOW_SIZE, y1 + yi);
						points[0] = new FormPoint(x1 + xi, y1 + yi);
					case 3:
						// points[2] = new FormPoint(x1 + xi - dx *
						// HALF_WINDOW_SIZE, y1 + yi);
						points[2] = new FormPoint(x1 + xi, y1 + yi);
						break;
					case 2:
						// points[1] = new FormPoint(x1 + xi - dx *
						// (HALF_WINDOW_SIZE + 1), y1 + yi);
						points[1] = new FormPoint(x1 + xi, y1 + yi);
					case 4:
						// points[3] = new FormPoint(x1 + xi - dx *
						// (HALF_WINDOW_SIZE + 1), y1 + yi);
						points[3] = new FormPoint(x1 + xi, y1 + yi);
						found = found || (stato == 4);
						break;
					default:
						break;
					}
				}

				if ((found && (stato == 4)) || (passed && (stato == 2))) {
					break;
				}
			}

			switch (stato) {
			case 2:
				passed = passed || (found && (stato == 2));
			case 4:
				double Xc1 = (points[0].getX() + points[3].getX()) / 2;
				double Xc2 = (points[1].getX() + points[2].getX()) / 2;
				centralPoints++;
				Xc += (Xc1 + Xc2) / 2;
				Yc += points[0].getY();
				break;
			case 0:
			case 1:
			case 3:
			default:
				break;
			}

			if (passed && found && (stato == 0)) {
				break;
			}
		}

		if (centralPoints == 0) {
			return null;
		}
		Xc = Xc / centralPoints;
		Yc = Yc / centralPoints;

		FormPoint p = new FormPoint(Xc, Yc);
		return p;
	}

	public FormPoint getCorner(Corner corner) {
		return corners.get(corner);
	}

	public Map<Corner, FormPoint> getCorners() {
		return corners;
	}

	public double getDiagonal() {
		return diagonal;
	}

	// TODO: what is it for?
	private FormField getField(FormField field, String fieldName) {
		FormField filledField = getField(fieldName);

		if (filledField == null) {
			filledField = new FormField(fieldName);
			filledField.setMultiple(field.isMultiple());
		}

		return filledField;
	}

	// TODO: what is it for?
	public FormField getField(String name) {
		for (FormField field : fields)
			if (field.getName().equals(name))
				return field;
		// If not found...
		return null;
	}

	@Deprecated
	public List<FormPoint> getFieldPoints() {
		return pointList;
	}

	// TODO DECIDE WHICH ONE TO USE BELOW OR ABOVE
	public List<FormPoint> getFieldsPoints() {
		List<FormPoint> result = new LinkedList<FormPoint>();
		for (FormField formField : fields)
			result.addAll(formField.getPoints().values());
		return result;
	}

	public List<FormField> getFields() {
		return fields;
	}

	/** Returns an unsorted header */
	public String[] getHeader() {
		String[] header = new String[fields.size() + 1];
		header[0] = Dictionary.translate("first.csv.column");
		for (int i = 0; i < fields.size(); i++)
			header[i + 1] = fields.get(i).getName();
		return header;
	}

	public BufferedImage getImage() {
		return image;
	}

	public FormPoint getPoint(int i) {
		return pointList.get(i);
	}

	public double getRotation() {
		return rotation;
	}

	private boolean isFilled(FormPoint responsePoint, int threshold,
			int density, int size) {
		int total = size * size;
		int halfSize = (int) size / 2;
		int[] rgbArray = new int[total];
		int count = 0;

		int xCoord = (int) responsePoint.getX();
		int yCoord = (int) responsePoint.getY();

		rgbArray = image.getRGB(xCoord - halfSize, yCoord - halfSize, size,
				size, rgbArray, 0, size);

		for (int i = 0; i < total; i++) {
			// TODO Take into account all color dimensions. This works for
			// images in greyscale
			// int r = (rgb >> 16) & 0xFF;
			// int g = (rgb >> 8) & 0xFF;
			// int b = (rgb & 0xFF);
			// // and the gray is the average for (r , g , b), like this:
			// int gray = (r + g + b) / 3;
			if ((rgbArray[i] & (0xFF)) < threshold) {
				count++;
			}
		}
		return (count / (double) total) >= (density / 100.0);
	}

	private int isWhite(int xi, int yi, int[] rgbArray, int threshold,
			int density) {
		int blacks = 0;
		int total = WINDOW_SIZE * WINDOW_SIZE;
		for (int i = 0; i < WINDOW_SIZE; i++) {
			for (int j = 0; j < WINDOW_SIZE; j++) {
				int xji = xi - HALF_WINDOW_SIZE + j;
				int yji = yi - HALF_WINDOW_SIZE + i;
				int index = (yji * halfWidth) + xji;
				if ((rgbArray[index] & (0xFF)) < threshold) {
					blacks++;
				}
			}
		}
		if ((blacks / (double) total) >= (density / 100.0))
			return BLACK;
		return WHITE;
	}

	public void removeField(FormField field) {

		for (Entry<String, FormPoint> point : field.getPoints().entrySet()) {
			pointList.remove(point.getValue());
		}
		fields.remove(field);
	}

	public void removePoint(FormPoint cursorPoint) {
		if (!pointList.isEmpty()) {
			FormPoint nearestPoint = pointList.get(0);
			double firstDistance = cursorPoint.dist2(nearestPoint);
			for (FormPoint point : pointList) {
				double lastDistance = cursorPoint.dist2(point);
				if (lastDistance < firstDistance) {
					nearestPoint = point;
					firstDistance = lastDistance;
				}
			}
			pointList.remove(nearestPoint);

			for (FormField field : fields) {
				for (Entry<String, FormPoint> point : field.getPoints()
						.entrySet()) {
					if (nearestPoint.equals(point.getValue())) {
						field.getPoints().remove(point.getKey());
						return;
					}
				}
			}
		}
	}

	public void setCorner(Corner corner, FormPoint point) {
		corners.put(corner, point);
	}

	public void setCorners(HashMap<Corner, FormPoint> corners) {
		this.corners.putAll(corners);
	}

	public void setCornerAndUpdateDiagonalAndRotation(Corner corner,
			FormPoint point) {
		corners.put(corner, point);
		rotation = calculateRotation();
		calculateDiagonal();
	}

	public void setField(FormField field) {
		fields.add(field);
		for (Entry<String, FormPoint> point : field.getPoints().entrySet()) {
			pointList.add(point.getValue());
		}
	}

	public void setFields(List<FormField> fields) {
		for (FormField field : fields) {
			setField(field);
		}
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	// TODO: REWRITE THIS
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[\r\n[Rotation:").append(rotation).append("]")
				.append("\r\n[Corners: ");

		// corner elements
		for (Entry<Corner, FormPoint> corner : corners.entrySet()) {
			Corner cornerPosition = corner.getKey();
			FormPoint cornerValue = corner.getValue();

			builder.append("\n\t[").append(cornerPosition.name()).append(' ')
					.append(cornerValue.toString()).append(']');
		}

		builder.append("\n]").append("\r\n[Fields: ");

		// field elements
		for (FormField field : fields) {

			builder = builder.append("\r\n[name:").append(field.getName())
					.append(" is multiple:").append(field.isMultiple())
					.append(" values: ");

			// value elements
			for (Entry<String, FormPoint> point : field.getPoints().entrySet()) {
				builder.append("\r\n[response:").append(point.getKey())
						.append(" ").append(point.getValue().toString())
						.append(" ]");
			}

			builder = builder.append("]");
		}

		builder.append("]").append("]");
		return builder.toString();
	}

	public File getFile() {
		return this.templateFile;
	}

	public void setFile(File file) {
		this.templateFile = file;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

}
