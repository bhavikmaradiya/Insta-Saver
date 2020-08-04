package sample;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import sample.InstaModals.GetInstaSlideUrl;
import sample.InstaModals.InstaVideoModal;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class Controller implements Initializable {


    public static final String INSTA_SLIDE = "GraphSidecar";
    /*Instagram Image Post Type*/
    public static final String INSTA_IMAGE = "GraphImage";
    /*Instagram Video Post Type*/
    public static final String INSTA_VIDEO = "GraphVideo";
    public Button saveBtn;
    public TextField urlField;
    public MenuItem close;
    ScrollPane scrollPane;
    public Pane pane;
    public MenuItem selectDestination;
    Preferences preferences;
    InstaVideoModal instaVideoModal;
    List<String> urls;
    File path = null;
    boolean isDownloading = false;


    @Override
    public void initialize(URL urL, ResourceBundle resourceBundle) {
        preferences = Preferences.userRoot().node("destinationPath");
        initDirectory();
        saveBtn.setText("Load");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (!isDownloading) {
                    Platform.exit();
                }
            }
        });

        saveBtn.setOnAction(event -> {
            if (saveBtn.getText().equals("Load") && urlField.getText().contains("instagram.com") && !urlField.getText().trim().isEmpty()) {
                try {
                    get(urlField.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (urlField.getText().trim().isEmpty() || !urlField.getText().contains("instagram.com")) {
                Notify.create()
                        .title("Insta Saver")
                        .position(Pos.CENTER)
                        .hideCloseButton()
                        .hideAfter(3500)
                        .text("Please enter valid url")
                        .show();
            }
        });
    }


    private void initDirectory() {
        if (preferences.get("path", null) != null) {
            path = new File(preferences.get("path", ""));
            selectDestination.setText("Change Directory - " + preferences.get("path", ""));
        } else {
            selectDestination.setText("Choose Destination");
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

    public void get(String url) throws IOException, InterruptedException {
        scrollPane = new ScrollPane();
        scrollPane.setLayoutX(29);
        scrollPane.setLayoutY(150);
        String requesturl = null;
        final String Url[] = url.split("\\?");
        if (Url[0].contains("https://www.instagram.com/p/") || Url[0].contains("https://www.instagram.com/tv/")) {
            requesturl = Url[0] + "?__a=1";
        } else {
            requesturl = Url[0] + "/?__a=1";
        }

        LoadInstaTask loadInstaTask = new LoadInstaTask(requesturl);
        loadInstaTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                ImageView imageView = new ImageView();
                instaVideoModal = loadInstaTask.getValue();
                if (pane.getChildren().contains(imageView)) {
                    pane.getChildren().remove(imageView);
                }
                if (pane.getChildren().contains(scrollPane)) {
                    pane.getChildren().remove(scrollPane);
                }
                switch (instaVideoModal.getGraphql().getShortcodeMedia().getTypename()) {
                    case Controller.INSTA_VIDEO:
                        if (!instaVideoModal.getGraphql().getShortcodeMedia().getOwner().getIsPrivate()) {
                            imageView.setImage(new Image(instaVideoModal.getGraphql().getShortcodeMedia().getDisplayResources().get(0).getSrc()));
                            imageView.setFitWidth(250);
                            imageView.setLayoutX(29);
                            imageView.setLayoutY(150);
                            imageView.setFitHeight(250);
                            imageView.setPreserveRatio(true);
                            pane.getChildren().add(imageView);
                            saveBtn.setText("Save");
                            saveBtn.setDisable(false);
                            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    List<String> urls = new ArrayList();
                                    urls.add(instaVideoModal.getGraphql().getShortcodeMedia().getVideoUrl());
                                    DownloadTask task = new DownloadTask(urls);
                                    task.setOnRunning(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            isDownloading = true;
                                            System.out.println("Download Running...");
                                            saveBtn.setDisable(true);
                                            saveBtn.setText("Saving...");
                                        }
                                    });

                                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            isDownloading = false;
                                            saveBtn.setDisable(false);
                                            saveBtn.setText("Load");
                                            urlField.setText("Saved Successfully....");
                                            System.out.println("Download Success");
                                            if (pane.getChildren().contains(imageView)) {
                                                pane.getChildren().remove(imageView);
                                            }
                                            if (pane.getChildren().contains(scrollPane)) {
                                                pane.getChildren().remove(scrollPane);
                                            }
                                            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                                                @Override
                                                public void handle(ActionEvent event) {
                                                    if (saveBtn.getText().equals("Load") && urlField.getText().contains("instagram.com") && !urlField.getText().trim().isEmpty()) {
                                                        try {
                                                            get(urlField.getText().toString());
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else if (urlField.getText().trim().isEmpty() || !urlField.getText().contains("instagram.com")) {
                                                        Notify.create()
                                                                .title("Insta Saver")
                                                                .position(Pos.CENTER)
                                                                .hideCloseButton()
                                                                .hideAfter(3500)
                                                                .text("Please enter valid url")
                                                                .show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    if (saveBtn.getText().equals("Save")) {
                                        ExecutorService executorService = Executors.newFixedThreadPool(1);
                                        executorService.execute(task);
                                        executorService.shutdown();
                                    }

                                }
                            });
                        }
                        break;
                    case Controller.INSTA_IMAGE:
                        if (!instaVideoModal.getGraphql().getShortcodeMedia().getOwner().getIsPrivate()) {
                            imageView.setImage(new Image(instaVideoModal.getGraphql().getShortcodeMedia().getDisplayResources().get(0).getSrc()));
                            imageView.setFitWidth(250);
                            imageView.setLayoutX(29);
                            imageView.setLayoutY(150);
                            imageView.setFitHeight(250);
                            imageView.setPreserveRatio(true);
                            pane.getChildren().add(imageView);
                            saveBtn.setText("Save");
                            saveBtn.setDisable(false);
                            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    List<String> urls = new ArrayList();
                                    urls.add(instaVideoModal.getGraphql().getShortcodeMedia().getDisplayResources().get(2).getSrc());
                                    DownloadTask task = new DownloadTask(urls);
                                    task.setOnRunning(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            isDownloading = true;
                                            System.out.println("Download Running...");
                                            saveBtn.setDisable(true);
                                            saveBtn.setText("Saving...");
                                        }
                                    });

                                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            isDownloading = false;
                                            saveBtn.setDisable(false);
                                            saveBtn.setText("Load");
                                            urlField.setText("Saved Successfully....");
                                            System.out.println("Download Success");
                                            if (pane.getChildren().contains(imageView)) {
                                                pane.getChildren().remove(imageView);
                                            }
                                            if (pane.getChildren().contains(scrollPane)) {
                                                pane.getChildren().remove(scrollPane);
                                            }
                                            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                                                @Override
                                                public void handle(ActionEvent event) {
                                                    if (saveBtn.getText().equals("Load") && urlField.getText().contains("instagram.com") && !urlField.getText().trim().isEmpty()) {
                                                        try {
                                                            get(urlField.getText().toString());
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else if (urlField.getText().trim().isEmpty() || !urlField.getText().contains("instagram.com")) {
                                                        Notify.create()
                                                                .title("Insta Saver")
                                                                .position(Pos.CENTER)
                                                                .hideCloseButton()
                                                                .hideAfter(3500)
                                                                .text("Please enter valid url")
                                                                .show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    if (saveBtn.getText().equals("Save")) {
                                        ExecutorService executorService = Executors.newFixedThreadPool(1);
                                        executorService.execute(task);
                                        executorService.shutdown();
                                    }
                                }
                            });
                        }
                        break;
                    case Controller.INSTA_SLIDE:
                        urls = new ArrayList<>();
                        urls.clear();
                        GridPane gridPane = new GridPane();
                        gridPane.setPrefWidth(500);
                        gridPane.setPrefHeight(250);
                        gridPane.setLayoutX(29);
                        gridPane.setLayoutY(150);
                        gridPane.setHgap(5);
                        gridPane.setVgap(5);

                        GetInstaSlideUrl getInstaSlideUrl = new GetInstaSlideUrl(instaVideoModal);
                        getInstaSlideUrl.setOnRunning(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {
                                saveBtn.setDisable(true);
                            }
                        });
                        getInstaSlideUrl.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {
                                urls = getInstaSlideUrl.getValue();
                                int val = 0;
                                for (int i = 0; i < urls.size(); ++i) {
                                    ImageView imageView1 = new ImageView(new Image(urls.get(i)));
                                    imageView1.setFitWidth(150);
                                    imageView1.setFitHeight(150);
                                    imageView1.setPreserveRatio(true);
                                    System.out.println(urls.get(i));
                                    if (i < 5) {
                                        gridPane.add(imageView1, i, 0, 1, 1);
                                    } else {
                                        gridPane.add(imageView1, val, 1, 1, 1);
                                        ++val;
                                    }
                                }
                                scrollPane.setContent(gridPane);
                                pane.getChildren().add(scrollPane);
                                saveBtn.setText("Save");
                                saveBtn.setDisable(false);
                            }
                        });

                        ExecutorService executorService = Executors.newFixedThreadPool(1);
                        executorService.execute(getInstaSlideUrl);
                        executorService.shutdown();


                        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                DownloadTask task = new DownloadTask(urls);
                                task.setOnRunning(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent workerStateEvent) {
                                        isDownloading = true;
                                        System.out.println("Download Running...");
                                        saveBtn.setDisable(true);
                                        saveBtn.setText("Saving...");
                                    }
                                });

                                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent workerStateEvent) {
                                        isDownloading = false;
                                        saveBtn.setDisable(false);
                                        saveBtn.setText("Load");
                                        urlField.setText("Saved Successfully....");
                                        System.out.println("Download Success");
                                        if (pane.getChildren().contains(imageView)) {
                                            pane.getChildren().remove(imageView);
                                        }
                                        if (pane.getChildren().contains(scrollPane)) {
                                            pane.getChildren().remove(scrollPane);
                                        }
                                        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent event) {
                                                if (saveBtn.getText().equals("Load") && urlField.getText().contains("instagram.com") && !urlField.getText().trim().isEmpty()) {
                                                    try {
                                                        get(urlField.getText().toString());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else if (urlField.getText().trim().isEmpty() || !urlField.getText().contains("instagram.com")) {
                                                    Notify.create()
                                                            .title("Insta Saver")
                                                            .position(Pos.CENTER)
                                                            .hideCloseButton()
                                                            .hideAfter(3500)
                                                            .text("Please enter valid url")
                                                            .show();
                                                }
                                            }
                                        });
                                    }
                                });

                                if (saveBtn.getText().equals("Save")) {
                                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                                    executorService.execute(task);
                                    executorService.shutdown();
                                }
                            }
                        });

                        break;
                }
            }
        });

        loadInstaTask.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                saveBtn.setDisable(true);
                saveBtn.setText("Loading...");
            }
        });

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(loadInstaTask);
        executorService.shutdown();


    }

    public void selectDestinationPath(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Download Folder");
        if (path != null) {
            directoryChooser.setInitialDirectory(path);
        }
        File file = directoryChooser.showDialog(selectDestination.getParentPopup().getScene().getWindow());
        if (file != null) {
            preferences.put("path", file.getAbsolutePath());
            selectDestination.setText("Change Directory - " + preferences.get("path", ""));
        }
    }

    public void onSave(ActionEvent actionEvent) throws FileNotFoundException {

    }

    public void openInstaPage(ActionEvent event) throws Exception {
        Desktop.getDesktop().browse(new URI("https://www.instagram.com/androidjavaworld/"));
    }

    public void close(ActionEvent event) {
        Platform.exit();
    }
}
