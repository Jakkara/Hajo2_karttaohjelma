import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class DownloadManager extends Thread {
    private static boolean verbosity = false;

    public void run() {
    }

    /*
    Yleinen latausmetodi joka kysyy käyttäjältä syötteenä tiedot.
     */
    public void download() throws Exception {
        Scanner reader = new Scanner(System.in);
        System.out.println("Input file URL : ");
        String url = reader.nextLine();
        URI u = URI.create(url);
        System.out.println("Input file name.");
        Path path = Paths.get(reader.nextLine());
        try (InputStream in = u.toURL().openStream()) {
            Files.copy(in, path);
            System.out.println("\n--***--\nLataus valmis.\n--***--\n");
        } catch (FileAlreadyExistsException faeE) {
            System.out.println("Samanniminen tiedosto on jo olemassa. Anna uusi nimi : ");
            download(url, reader.nextLine());
        }
    }

    /* Lataa halutusta osoitteesta tiedoston ja säilöö sen annetulla nimellä.
    Palauttaa true latauksen onnistuessa, false virheen sattuessa.
    Ylikirjoittaa aiemman tiedoston tällaisen löytyessä.
    Kun verbosity == true, ilmoittaa aina mistä ladataan.
     */
    public boolean download(String url, String file) {
        Path path = Paths.get(file); //file path
        URI u = URI.create(url);
        try (InputStream in = u.toURL().openStream()) { //avaa stream halutusta osoitteesta
            Files.deleteIfExists(path); //poista aiempi tiedosto
            Files.copy(in, path);       //kopioi tiedosto kansioon
            if (verbosity) System.out.println("\n--***--\nDownload " + url + " complete.\n--***--\n");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
