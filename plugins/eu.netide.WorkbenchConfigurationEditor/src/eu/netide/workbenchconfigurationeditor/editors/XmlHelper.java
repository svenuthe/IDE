package eu.netide.workbenchconfigurationeditor.editors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.netide.workbenchconfigurationeditor.model.LaunchConfigurationModel;
import eu.netide.workbenchconfigurationeditor.model.XmlConstants;

public class XmlHelper {

	public static void saveContentToXml(Document doc, IFile file) {
		saveContentToXml(doc, new File(file.getLocation().toOSString()));
	}

	public static void saveContentToXml(Document doc, File file) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(file);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void addModelToXmlFile(Document doc, LaunchConfigurationModel model, IFile file) {
		Node wb = doc.getFirstChild();

		if (wb.getNodeName().equals(XmlConstants.WORKBENCH)) {
			Element app = doc.createElement(XmlConstants.NODE_APP);
			app.setAttribute(XmlConstants.ATTRIBUTE_APP_NAME, model.getAppName());
			app.setAttribute(XmlConstants.ATTRIBUTE_APP_ID, "" + model.getID());

			Element appPath = doc.createElement(XmlConstants.ELEMENT_APP_PATH);
			appPath.setTextContent(model.getAppPath());
			app.appendChild(appPath);

			Element platform = doc.createElement(XmlConstants.ELEMENT_PLATFORM);
			platform.setTextContent(model.getPlatform());
			app.appendChild(platform);

			Element clientController = doc.createElement(XmlConstants.ELEMENT_CLIENT_CONTROLLER);
			clientController.setTextContent(model.getClientController());
			app.appendChild(clientController);

			Element serverController = doc.createElement(XmlConstants.ELEMENT_SERVER_CONTROLLER);
			serverController.setTextContent(model.getServerController());
			app.appendChild(serverController);

			wb.appendChild(app);

			XmlHelper.saveContentToXml(doc, file);

		}
	}

	public static void removeFromXml(Document doc, LaunchConfigurationModel model, IFile file) {

		Node workbenchNode = doc.getFirstChild();
		NodeList childs = workbenchNode.getChildNodes();

		for (int count = 0; count < childs.getLength(); count++) {
			Node tempNode = childs.item(count);

			if (tempNode.getNodeName().equals(XmlConstants.NODE_APP)) {
				NamedNodeMap nodeMap = tempNode.getAttributes();

				for (int i = 0; i < nodeMap.getLength(); i++) {

					Node node = nodeMap.item(i);
					if (node.getNodeName().equals(XmlConstants.ATTRIBUTE_APP_ID)) {
						workbenchNode.removeChild(tempNode);
						XmlHelper.saveContentToXml(doc, file);
					}
				}
			}

		}
	}

	public static Document getDocFromFile(IFile file) {
		File xmlFile = new File(file.getRawLocationURI());
		modelList = new ArrayList<LaunchConfigurationModel>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		Document doc = null;
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	}

	public static ArrayList<LaunchConfigurationModel> parseFileToModel(IFile file, Document doc) {
		try {
			File xmlFile = new File(file.getRawLocationURI());
			modelList = new ArrayList<LaunchConfigurationModel>();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			if (doc.hasChildNodes()) {

				printNote(doc.getChildNodes(), null);

			}

			return modelList;
		} catch (Exception e) {

		}
		return null;

	}

	private static ArrayList<LaunchConfigurationModel> modelList;

	public static void printNote(NodeList nodeList, LaunchConfigurationModel model) {

		for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				switch (tempNode.getNodeName()) {
				case XmlConstants.ELEMENT_APP_PATH:
					model.setAppPath(tempNode.getTextContent());
					break;
				case XmlConstants.ELEMENT_TOPOLOGY_PATH:
					LaunchConfigurationModel.setTopology(tempNode.getTextContent());

					break;
				case XmlConstants.NODE_APP:
					model = new LaunchConfigurationModel();
					modelList.add(model);

					if (tempNode.hasAttributes()) {
						NamedNodeMap nodeMap = tempNode.getAttributes();

						for (int i = 0; i < nodeMap.getLength(); i++) {

							Node node = nodeMap.item(i);
							if (node.getNodeName().equals(XmlConstants.ATTRIBUTE_APP_ID)) {
								model.setID(node.getNodeValue());
							} else {
								if (node.getNodeName().equals(XmlConstants.ATTRIBUTE_APP_NAME)) {
									model.setAppName(node.getNodeValue());
								}
							}
						}
					}
					break;
				case XmlConstants.ELEMENT_CLIENT_CONTROLLER:
					model.setClientController(tempNode.getTextContent());
					break;
				case XmlConstants.ELEMENT_SERVER_CONTROLLER:
					model.setServerController(tempNode.getTextContent());
					break;
				case XmlConstants.ELEMENT_PLATFORM:
					model.setPlatform(tempNode.getTextContent());
					break;
				default:
					System.err.println("No match for node: " + tempNode.getNodeName());

				}

				if (tempNode.hasChildNodes()) {
					// loop again if has child nodes
					printNote(tempNode.getChildNodes(), model);
				}
			}

		}

	}
}
