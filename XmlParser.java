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
            //jätetään huomiotta turhat osat
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setValidating(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            //palauttaa Document-olion annetussa tiedoston nimessä
            return builder.parse(new InputSource(docString));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /*
    Printtaa solmun lasten tiedot
     */
    private static void printTags(Node nodes) {
        if (nodes.hasChildNodes() || nodes.getNodeType() != 3) { //tarkastateen että nodella on lapsia
            System.out.println(nodes.getNodeName() + " : " + nodes.getTextContent());
            NodeList nl = nodes.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) printTags(nl.item(j));
        }
    }

    /*
    Muuttaa noden elementiksi findNodes-metodin käyttöä varten
     */
    private static Element nodeToElement(Node nd) {
        if (nd instanceof Element) {
            return (Element) nd;
        }
        return null;
    }

    /*
    Etsii hakusanalla solmuja
     */
    public static NodeList findNodes(Node nd, String name) {
        Element ele = nodeToElement(nd);
        return ele.getElementsByTagName(name);
    }
}
