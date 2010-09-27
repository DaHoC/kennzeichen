/**
 * @author gawe design
 */
package kennzeichen;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import kennzeichen.kennzeichendb;
import kennzeichen.simpleImageCanvas;

public class kennzeichenMidlet extends MIDlet implements CommandListener {

    private Display display;
    private Form form;
    private simpleImageCanvas canvas;
    private static final String SPLASH_IMAGE = "/images/gawesplash.png";
    private static final String KENNZEICHEN_DB = "/database/kfzliste.csv";
    private static kennzeichendb kdb;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private final Command search = new Command("Suche", Command.OK, 2);
    private TextField badge = new TextField("Kennzeichen", "", 3, TextField.ANY);
    private TextField town = new TextField("Stadt", "", 20, TextField.ANY);

    public kennzeichenMidlet() {
        this.display = Display.getDisplay(this);
    }

    public void startApp() {
        this.showSplash();
        form = new Form("Lade Datenbank");
        display.setCurrent(form);
        this.loadDatabase();
        this.refreshForm();
        form.append("");
    }

    private void refreshForm() {
        form.setTitle("Kennzeichen suchen");
        form.addCommand(search);
        form.addCommand(exitCommand);
//        badge.addCommand(search);
//        badge.setDefaultCommand(search);
        form.append(badge);
        form.append(town);
        form.setCommandListener(this);
    }

    private void loadDatabase() {
        // Reading resources of JAR file
        InputStream inStream = this.getClass().getResourceAsStream(KENNZEICHEN_DB);
        if (inStream == null) {
            throw new Error("Failed to load database '" + KENNZEICHEN_DB + "': Not found!");
        }
        byte[] buf = new byte[14*1000]; // csv file is 13979 Bytes
        if (inStream != null) {
            try {
                int total = 0;
                while (true) {
                    int numRead = inStream.read(buf, total, buf.length - total);
                    if (numRead <= 0) {
                        break;
                    }
                    total += numRead;
                }
                byte[] bufferWithCorrectLength = new byte[total];
                System.arraycopy(buf, 0, bufferWithCorrectLength, 0, total);
                kdb = new kennzeichendb(new String(bufferWithCorrectLength));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void showSplash() {
        canvas = new simpleImageCanvas(SPLASH_IMAGE);
        display = Display.getDisplay(this);
        canvas.setFullScreenMode(true);
        display.setCurrent(canvas);
        waitMilliseconds(2500); // Wait some time
    }

    private static void waitMilliseconds(long ms) {
        try {
            Thread.sleep(ms); // ms, e.g for pause to avoid cpu starvation
        } catch (Exception ex) {
            System.err.print("Thread backgrounding failed!");
        }
    }

    public void commandAction(Command c, Displayable d) {
        boolean reverseSearch = false;
        if (c == exitCommand) {
            destroyApp(false);
            System.out.println("Exit");
        } else if (c == search) {
            form.delete(form.size()-1);
            String searchResult = "";
            reverseSearch = (this.badge.getString().equalsIgnoreCase(""));
            if (!reverseSearch) {
                searchResult = kdb.getPlaceOfBadge(this.badge.getString());
            } else {
                if (!this.town.getString().equalsIgnoreCase("")) {
                    searchResult = kdb.getBadgeOfTown(this.town.getString());
                }
            }
            if (searchResult.equalsIgnoreCase("")) {
                form.append("Kennzeichen / Stadt nicht gefunden!");
            } else {
                if (reverseSearch) {
                    form.append("Kennzeichen: " + searchResult.toUpperCase());
                } else {
                    String state = kdb.getStateOfBadge(this.badge.getString());
                    if (!state.equalsIgnoreCase("")) {
                        form.append(searchResult + " (" + kdb.getStateOfBadge(this.badge.getString()) + ")");
                    } else {
                        form.append(searchResult);
                    }
                }
            }
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Stopped");
        notifyDestroyed();
    }
}
