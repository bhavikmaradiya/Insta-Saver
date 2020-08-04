package sample;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.prefs.Preferences;

public class DownloadTask extends Task<Boolean> {
    List<String> url;
    File path;
    Preferences preferences;

    public DownloadTask(List<String> url) {
        this.url = url;
        initDirectory();
    }

    private void initDirectory() {
        preferences = Preferences.userRoot().node("destinationPath");
        {
            if (preferences.get("path", null) != null) {
                path = new File(preferences.get("path", ""));
            } else {
                if (new File(System.getProperty("user.home") + File.separator + "Pictures").canWrite()) {
                    path = new File(System.getProperty("user.home") + File.separator + "Pictures" + File.separator + "Insta Saver");
                    if (!path.exists()) {
                        path.mkdir();
                    } else {
                        path = null;
                    }
                }
            }
        }
    }

    @Override
    protected Boolean call() throws Exception {
        for (String s : url) {
            if (s.contains(".jpg")) {
                try {
                    FileUtils.copyURLToFile(new URL(s), new File(path + File.separator + System.currentTimeMillis() + ".jpg"), 3000, 100000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileUtils.copyURLToFile(new URL(s), new File(path + File.separator + System.currentTimeMillis() + ".mp4"), 3000, 100000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        showTrayMessage();
        return true;
    }

    private void showTrayMessage() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage image = ImageIO.read(DownloadTask.class.getResource("/res/launcher.png"));
            TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Insta Saver");
            tray.add(trayIcon);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
//                        https://www.instagram.com/p/CA5YZzhArbN/?utm_source=ig_web_copy_link
                        Desktop.getDesktop().open(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            trayIcon.displayMessage("Insta Saver", "Download Complete!", TrayIcon.MessageType.NONE);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }


}
