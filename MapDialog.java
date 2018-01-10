// Kartankatseluohjelman graafinen käyttöliittymä

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class MapDialog extends JFrame {

    // Käyttöliittymän komponentit

    private JLabel imageLabel = new JLabel();
    private JPanel leftPanel = new JPanel();

    private JButton refreshB = new JButton("Päivitä");
    private JButton leftB = new JButton("<");
    private JButton rightB = new JButton(">");
    private JButton upB = new JButton("^");
    private JButton downB = new JButton("v");
    private JButton zoomInB = new JButton("+");
    private JButton zoomOutB = new JButton("-");

    public MapDialog() throws Exception {

        // Latausmanageri
        DownloadManager downloader = new DownloadManager();
        // XML-kääntäjä
        XmlParser parser = new XmlParser();
        // getCapabilities-kysely -> capabilities.xml
        downloader.download("http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities", "capabilities.xml");

        // Valmistele ikkuna ja lisää siihen komponentit

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // ALLA OLEVAN TESTIRIVIN VOI KORVATA JOLLAKIN MUULLA ERI ALOITUSNÄKYMÄN
        // LATAAVALLA RIVILLÄ
        imageLabel.setIcon(new ImageIcon(new URL("http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=-180,-90,180,90&SRS=EPSG:4326&WIDTH=953&HEIGHT=480&LAYERS=bluemarble,cities&STYLES=&FORMAT=image/png&TRANSPARENT=true")));

        add(imageLabel, BorderLayout.EAST);

        ButtonListener bl = new ButtonListener();
        refreshB.addActionListener(bl);
        leftB.addActionListener(bl);
        rightB.addActionListener(bl);
        upB.addActionListener(bl);
        downB.addActionListener(bl);
        zoomInB.addActionListener(bl);
        zoomOutB.addActionListener(bl);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        leftPanel.setMaximumSize(new Dimension(100, 600));

        // TODO:
        // ALLA OLEVIEN KOLMEN TESTIRIVIN TILALLE SILMUKKA JOKA LISÄÄ KÄYTTÖLIITTYMÄÄN
        // KAIKKIEN XML-DATASTA HAETTUJEN KERROSTEN VALINTALAATIKOT MALLIN MUKAAN
        /*leftPanel.add(new LayerCheckBox("bluemarble", "Maapallo", true));
        leftPanel.add(new LayerCheckBox("cities", "Kaupungit", false));*/

        Document xmlDoc = parser.getDocument("capabilities.xml");
        System.out.println("File found : " + xmlDoc.getDocumentElement().getNodeName());
        Node rootLayer = xmlDoc.getElementsByTagName("Layer").item(0);
        NodeList layers = parser.findNodes(rootLayer, "Layer");
        //System.out.println("Available layers: " + layers.getLength());
        for (int i = 0; i < layers.getLength(); i++) {
            Node current = parser.findNodes(layers.item(i), "Title").item(0);
            String currentTitle = current.getTextContent();
            current = parser.findNodes(layers.item(i), "Name").item(0);
            String currentName = current.getTextContent();
            leftPanel.add(new LayerCheckBox(currentName, currentTitle, true));
        }


        leftPanel.add(refreshB);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(leftB);
        leftPanel.add(rightB);
        leftPanel.add(upB);
        leftPanel.add(downB);
        leftPanel.add(zoomInB);
        leftPanel.add(zoomOutB);

        add(leftPanel, BorderLayout.WEST);

        pack();
        setVisible(true);

    }

    public static void main(String[] args) throws Exception {
        new MapDialog();

    }

    // Kontrollinappien kuuntelija
    // KAIKKIEN NAPPIEN YHTEYDESSÄ VOINEE HYÖDYNTÄÄ updateImage()-METODIA
    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == refreshB) {
                try {
                    updateImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (e.getSource() == leftB) {
                // TODO:
                // VASEMMALLE SIIRTYMINEN KARTALLA
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
            if (e.getSource() == rightB) {
                // TODO:
                // OIKEALLE SIIRTYMINEN KARTALLA
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
            if (e.getSource() == upB) {
                // TODO:
                // YLÖSPÄIN SIIRTYMINEN KARTALLA
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
            if (e.getSource() == downB) {
                // TODO:
                // ALASPÄIN SIIRTYMINEN KARTALLA
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
            if (e.getSource() == zoomInB) {
                // TODO:
                // ZOOM IN -TOIMINTO
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
            if (e.getSource() == zoomOutB) {
                // TODO:
                // ZOOM OUT -TOIMINTO
                // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA PÄIVITÄ KUVA
            }
        }
    }

    // Valintalaatikko, joka muistaa karttakerroksen nimen
    private class LayerCheckBox extends JCheckBox {
        private String name = "";

        public LayerCheckBox(String name, String title, boolean selected) {
            super(title, null, selected);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // Tarkastetaan mitkä karttakerrokset on valittu,
    // tehdään uudesta karttakuvasta pyyntö palvelimelle ja päivitetään kuva
    public void updateImage() throws Exception {
        String s = "";

        // Tutkitaan, mitkä valintalaatikot on valittu, ja
        // kerätään s:ään pilkulla erotettu lista valittujen kerrosten
        // nimistä (käytetään haettaessa uutta kuvaa)
        Component[] components = leftPanel.getComponents();
        for (Component com : components) {
            if (com instanceof LayerCheckBox)
                if (((LayerCheckBox) com).isSelected()) s = s + com.getName() + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1);

        // TODO:
        // getMap-KYSELYN URL-OSOITTEEN MUODOSTAMINEN JA KUVAN PÄIVITYS ERILLISESSÄ SÄIKEESSÄ
        // imageLabel.setIcon(new ImageIcon(url));
    }


} // MapDialog
