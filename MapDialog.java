// Kartankatseluohjelman graafinen k‰yttˆliittym‰

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.*;
import java.util.Arrays;

public class MapDialog extends JFrame {
    //default-n‰kym‰n‰ Eurooppa
    static String currentUrl = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=-60,0,100,80&SRS=EPSG:4326&WIDTH=1200&HEIGHT=600&LAYERS=bluemarble,country_bounds&STYLES=&FORMAT=image/png&TRANSPARENT=true";
    //zoomFactor ohjaa kartalla liikkumisen nopeutta riippuen kuinka l‰hell‰ ollaan zoomattu
    double zoomFactor = 1.0;
    //bbox-taulu pit‰‰ kirjaa esitetyn kartan rajoista. alkuarvo p‰‰tell‰‰n default-kartasta
    int[] bbox = parseBboxFromUrl(currentUrl);
    //alustetaan latausmanagerin s‰ie
    DownloadManager downloader = new DownloadManager();

    // K‰yttˆliittym‰n komponentit

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

        // Latausmanagerin s‰ie k‰ynnistet‰‰n
        downloader.start();
        // XML-k‰‰nt‰j‰
        XmlParser parser = new XmlParser();
        // getCapabilities-kysely -> capabilities.xml
        downloader.download("http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities", "capabilities.xml");

        // Valmistele ikkuna ja lis‰‰ siihen komponentit

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        imageLabel.setIcon(getImage(currentUrl));
        add(imageLabel, BorderLayout.NORTH);

        ButtonListener bl = new ButtonListener();
        refreshB.addActionListener(bl);
        leftB.addActionListener(bl);
        rightB.addActionListener(bl);
        upB.addActionListener(bl);
        downB.addActionListener(bl);
        zoomInB.addActionListener(bl);
        zoomOutB.addActionListener(bl);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        leftPanel.setMaximumSize(new Dimension(100, 600));

        //K‰sittelee checkboxien esityksen
        Document xmlDoc = parser.getDocument("capabilities.xml");
        Node rootLayer = xmlDoc.getElementsByTagName("Layer").item(0);
        NodeList layers = parser.findNodes(rootLayer, "Layer");
        for (int i = 0; i < layers.getLength(); i++) {
            Node current = parser.findNodes(layers.item(i), "Title").item(0); //haetaan layerin otsikko
            String currentTitle = current.getTextContent();
            current = parser.findNodes(layers.item(i), "Name").item(0); //haetaan layerin nimi XML:ss‰
            String currentName = current.getTextContent();
            //asetetaan checkboxin rasti sill‰ perusteella, lˆytyykˆ se urlista
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
    // KAIKKIEN NAPPIEN YHTEYDESSƒ VOINEE HY÷DYNTƒƒ updateImage()-METODIA
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

    // Tarkastetaan mitk‰ karttakerrokset on valittu,
    // tehd‰‰n uudesta karttakuvasta pyyntˆ palvelimelle ja p‰ivitet‰‰n kuva
    private void updateImage() throws Exception {
        System.gc();
        //URL-osoitteen alku aina sama
        String s = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=";
        //Lis‰t‰‰n rajaavat koordinaatit
        for (int i = 0; i < bbox.length; i++) {
            s += bbox[i] + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1); //poistetaan pilkku per‰st‰
        //lis‰t‰‰n aina vakiona olevat m‰‰rittelyt
        s += "&SRS=EPSG:4326&WIDTH=1200&HEIGHT=600&LAYERS=";
        // Tutkitaan, mitk‰ valintalaatikot on valittu, ja
        // ker‰t‰‰n s:‰‰n pilkulla erotettu lista valittujen kerrosten
        // nimist‰ (k‰ytet‰‰n haettaessa uutta kuvaa)
        Component[] components = leftPanel.getComponents();
        for (Component com : components) {
            if (com instanceof LayerCheckBox)
                if (((LayerCheckBox) com).isSelected()) s = s + com.getName() + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1);
        //loppuosa aina vakio
        s += "&STYLES=&FORMAT=image/png&TRANSPARENT=true";
        imageLabel.setIcon(getImage(s));
    }

    public ImageIcon getImage(String url) {
        if (downloader.download(url, "map.png") == true) {
            try {
                BufferedImage img = ImageIO.read(new File("map.png"));
                ImageIcon icon = new ImageIcon(img);
                return icon;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }return null;
    }

    private static int[] parseBboxFromUrl(String url) {
        String parsed = url.substring(url.indexOf("BBOX=") + 5);//leikkaa urlin alkup‰‰n
        parsed = parsed.substring(0, parsed.indexOf('&')); //leikkaa koordinaattien j‰lkeisen osan
        String[] coordinates = parsed.split(",");//erottelee koordinaatit toisistaan
        int[] results = new int[coordinates.length];
        //lis‰t‰‰n koordinaatit numeroarvoina
        for (int i = 0; i < coordinates.length; i++) {
            results[i] = Integer.parseInt(coordinates[i]);
        }
        return results;
    }
} // MapDialog
