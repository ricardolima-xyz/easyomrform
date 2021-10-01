package net.sf.opticalbot.omr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.opticalbot.omr.exception.OMRModelLoadException;
import net.sf.opticalbot.omr.exception.OMRModelSaveException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is responsible for saving (serializing) OMRModel objects into XML
 * files and loading (deserializing) OMRModel objects from XML files.
 * 
 * Since OMRModel objects are complex to be created, this Factory class is used.
 */
public class OMRModelFactory {

	public static OMRModel load(File file) throws OMRModelLoadException {
		OMRModel omrModel = new OMRModel();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();

			Element templateElement = doc.getDocumentElement();
			Element rotationElement = (Element) templateElement
					.getElementsByTagName("rotation").item(0);
			double rotation = Double.parseDouble(rotationElement
					.getAttribute("angle"));
			omrModel.setRotation(rotation);

			Element cornersElement = (Element) templateElement
					.getElementsByTagName("corners").item(0);
			NodeList cornerList = cornersElement.getElementsByTagName("corner");
			for (int i = 0; i < cornerList.getLength(); i++) {
				Element cornerElement = (Element) cornerList.item(i);
				String postion = cornerElement.getAttribute("position");
				String xCoord = cornerElement.getAttribute("x");
				String yCoord = cornerElement.getAttribute("y");

				FormPoint cornerPoint = new FormPoint(
						Double.parseDouble(xCoord), Double.parseDouble(yCoord));
				omrModel.setCorner(Corner.valueOf(postion), cornerPoint);
			}

			omrModel.calculateDiagonal();
			Element fieldsElement = (Element) templateElement
					.getElementsByTagName("fields").item(0);
			NodeList fieldList = fieldsElement.getElementsByTagName("field");
			for (int i = 0; i < fieldList.getLength(); i++) {
				Element fieldElement = (Element) fieldList.item(i);
				Element nameElement = (Element) fieldElement
						.getElementsByTagName("name").item(0);
				String fieldName = nameElement.getTextContent();
				FormField field = new FormField(fieldName);
				field.setMultiple(Boolean.parseBoolean(fieldElement
						.getAttribute("multiple")));

				Element valuesElement = (Element) fieldElement
						.getElementsByTagName("values").item(0);
				NodeList valueList = valuesElement
						.getElementsByTagName("value");
				for (int j = 0; j < valueList.getLength(); j++) {
					Element valueElement = (Element) valueList.item(j);
					String xCoord = valueElement.getAttribute("x");
					String yCoord = valueElement.getAttribute("y");

					FormPoint point = new FormPoint(Double.parseDouble(xCoord),
							Double.parseDouble(yCoord));
					field.setPoint(valueElement.getTextContent(), point);
					omrModel.addPointSimple(point);
				}
				omrModel.getFields().add(field);

			}

			// image element
			Element imageElement = (Element) templateElement
					.getElementsByTagName("image").item(0);
			if (imageElement != null) {
				String imageDataString = imageElement.getTextContent();
				byte[] byteArray = Base64.getDecoder().decode(imageDataString);
				ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
				omrModel.setImage(ImageIO.read(bais));
			}
		} catch (ParserConfigurationException e) {
			throw new OMRModelLoadException(e);
		} catch (SAXException e) {
			throw new OMRModelLoadException(e);
		} catch (IOException e) {
			throw new OMRModelLoadException(e);
		}
		return omrModel;
	}

	/**
	 * Saves template to file specified on getFile()-setFile(). If this file is
	 * null, this method does nothing.
	 * 
	 * @throws OMRModelSaveException
	 */
	public static void save(OMRModel omrModel) throws OMRModelSaveException {
		if (omrModel.getFile() == null)
			return;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root element
			Document doc = docBuilder.newDocument();
			Element templateElement = doc.createElement("template");

			// rotation element
			doc.appendChild(templateElement);
			Element rotationElement = doc.createElement("rotation");
			rotationElement.setAttribute("angle",
					String.valueOf(omrModel.getRotation()));
			templateElement.appendChild(rotationElement);

			// corners element
			Element cornersElement = doc.createElement("corners");
			templateElement.appendChild(cornersElement);

			// corner elements
			for (Entry<Corner, FormPoint> corner : omrModel.getCorners()
					.entrySet()) {
				Element cornerElement = doc.createElement("corner");
				Corner cornerPosition = corner.getKey();
				FormPoint cornerValue = corner.getValue();
				cornerElement.setAttribute("position", cornerPosition.name());
				cornerElement.setAttribute("x",
						String.valueOf(cornerValue.getX()));
				cornerElement.setAttribute("y",
						String.valueOf(cornerValue.getY()));
				cornersElement.appendChild(cornerElement);
			}

			// fields element
			Element fieldsElement = doc.createElement("fields");
			templateElement.appendChild(fieldsElement);

			// field elements
			for (FormField field : omrModel.getFields()) {
				Element fieldElement = doc.createElement("field");

				fieldElement.setAttribute("multiple",
						String.valueOf(field.isMultiple()));

				// name element
				Element fieldNameElement = doc.createElement("name");
				fieldNameElement
						.appendChild(doc.createTextNode(field.getName()));
				fieldElement.appendChild(fieldNameElement);

				// values element
				Element valuesElement = doc.createElement("values");

				// value elements
				for (Entry<String, FormPoint> point : field.getPoints()
						.entrySet()) {
					Element valueElement = doc.createElement("value");
					FormPoint pointValue = point.getValue();
					valueElement.setAttribute("x",
							String.valueOf(pointValue.getX()));
					valueElement.setAttribute("y",
							String.valueOf(pointValue.getY()));
					valueElement
							.appendChild(doc.createTextNode(point.getKey()));
					valuesElement.appendChild(valueElement);
				}

				fieldElement.appendChild(valuesElement);
				fieldsElement.appendChild(fieldElement);
			}

			// image element
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(omrModel.getImage(), "png", baos);
			byte[] byteArray = baos.toByteArray();
			String imageDataString = Base64.getEncoder().encodeToString(
					byteArray);
			Element imageElement = doc.createElement("image");
			imageElement.setTextContent(imageDataString);
			templateElement.appendChild(imageElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(omrModel.getFile());
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			throw new OMRModelSaveException(pce);
		} catch (TransformerConfigurationException e) {
			throw new OMRModelSaveException(e);
		} catch (TransformerException e) {
			throw new OMRModelSaveException(e);
		} catch (IOException e) {
			throw new OMRModelSaveException(e);
		}
	}

}
