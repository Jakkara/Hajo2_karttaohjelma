import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlParser {
    static Document getDocument(String docString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setValidating(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(docString));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void printTags(Node nodes) {
        if (nodes.hasChildNodes() || nodes.getNodeType() != 3) {
            System.out.println(nodes.getNodeName() + " : " + nodes.getTextContent());
            NodeList nl = nodes.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) printTags(nl.item(j));
        }
    }

    public static Element nodeToElement(Node nd) {
        if (nd instanceof Element) {
            Element docElement = (Element) nd;
            return docElement;
        }
        return null;
    }

    public static NodeList findNodes(Node nd, String name) {
        Element ele = nodeToElement(nd);
        return ele.getElementsByTagName(name);
    }
}
