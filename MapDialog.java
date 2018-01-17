// Kartankatseluohjelman graafinen käyttöliittymä

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Arrays;

public class MapDialog extends JFrame {

    static String currentUrl = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=-60,0,100,80&SRS=EPSG:4326&WIDTH=1200&HEIGHT=600&LAYERS=bluemarble,country_bounds&STYLES=&FORMAT=image/png&TRANSPARENT=true";
    double zoomFactor = 1.0;
    int[] bbox = parseBboxFromUrl(currentUrl);

    // Käyttöliittymän komponentit

    private JLabel imageLabel = new JLabel();
    private JPanel leftPanel = new JPanel();

    private JButton refreshB = new JButton("Refresh");
    private JButton leftB = new JButton("<");
    private JButton rightB = new JButton(">");
    private JButton upB = new JButton("^");
    private JButton downB = new JButton("v");
    private JButton zoomInB = new JButton("+");
    private JButton zoomOutB = new JButton("-");

    public MapDialog() throws Exception {

        // Latausmanageri
        DownloadManager downloader = new DownloadManager();
        downloader.start();
        // XML-kääntäjä
        XmlParser parser = new XmlParser();
        // getCapabilities-kysely -> capabilities.xml
        downloader.download("http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities", "capabilities.xml");

        // Valmistele ikkuna ja lisää siihen komponentit

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        downloader.download(currentUrl, "map.png");

        imageLabel.setIcon(new ImageIcon(new URL(currentUrl)));

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

        //Käsittelee checkboxien esityksen
        Document xmlDoc = parser.getDocument("capabilities.xml");
        System.out.println("File found : " + xmlDoc.getDocumentElement().getNodeName());
        Node rootLayer = xmlDoc.getElementsByTagName("Layer").item(0);
        NodeList layers = parser.findNodes(rootLayer, "Layer");
        for (int i = 0; i < layers.getLength(); i++) {
            Node current = parser.findNodes(layers.item(i), "Title").item(0); //haetaan layerin otsikko
            String currentTitle = current.getTextContent();
            current = parser.findNodes(layers.item(i), "Name").item(0); //haetaan layerin nimi XML:ssä
            String currentName = current.getTextContent();
            //asetetaan checkboxin rasti sillä perusteella, löytyykö se urlista
            leftPanel.add(new LayerCheckBox(currentName, currentTitle, currentUrl.indexOf(currentName) != -1));
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
                bbox[0] -= 20 / zoomFactor;
                bbox[2] -= 20 / zoomFactor;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == rightB) {
                bbox[0] += 20 / zoomFactor;
                bbox[2] += 20 / zoomFactor;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == upB) {
                // TODO:
                bbox[1] += 10 / zoomFactor;
                bbox[3] += 10 / zoomFactor;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == downB) {
                // TODO: tarkista sallittu liikkuminen
                bbox[1] -= 10 / zoomFactor;
                bbox[3] -= 10 / zoomFactor;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == zoomInB) {
                // TODO: tarkista sallittu liikkuminen
                bbox[0] += 10;
                bbox[2] -= 10;
                bbox[1] += 5;
                bbox[3] -= 5;
                zoomFactor += 0.1;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == zoomOutB) {
                bbox[0] -= 10;
                bbox[2] += 10;
                bbox[1] -= 5;
                bbox[3] += 5;
                if (zoomFactor > 0.1) zoomFactor -= 0.1;
                try {
                    updateImage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
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
    private void updateImage() throws Exception {
        System.gc();
        DownloadManager downloader = new DownloadManager();
        downloader.start();
        //URL-osoitteen alku aina sama
        String s = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=";
        //Lisätään rajaavat koordinaatit
        for (int i = 0; i < bbox.length; i++) {
            s += bbox[i] + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1); //poistetaan pilkku perästä
        //lisätään aina vakiona olevat määrittelyt
        s += "&SRS=EPSG:4326&WIDTH=1200&HEIGHT=600&LAYERS=";
        // Tutkitaan, mitkä valintalaatikot on valittu, ja
        // kerätään s:ään pilkulla erotettu lista valittujen kerrosten
        // nimistä (käytetään haettaessa uutta kuvaa)
        Component[] components = leftPanel.getComponents();
        for (Component com : components) {
            if (com instanceof LayerCheckBox)
                if (((LayerCheckBox) com).isSelected()) s = s + com.getName() + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1);
        //loppuosa aina vakio
        s += "&STYLES=&FORMAT=image/png&TRANSPARENT=true";
        downloader.download(s, "map.png");
        imageLabel.setIcon(new ImageIcon("map.png")); //VÄLIAIKAINEN, hidas kuin helvetti
    }

    private static int[] parseBboxFromUrl(String url) {
        String parsed = url.substring(url.indexOf("BBOX=") + 5);//leikkaa urlin alkupään
        parsed = parsed.substring(0, parsed.indexOf('&')); //leikkaa koordinaattien jälkeisen osan
        String[] coordinates = parsed.split(",");//erottelee koordinaatit toisistaan
        int[] results = new int[coordinates.length];
        //lisätään koordinaatit numeroarvoina
        for (int i = 0; i < coordinates.length; i++) {
            results[i] = Integer.parseInt(coordinates[i]);
        }
        return results;
    }
} // MapDialog
