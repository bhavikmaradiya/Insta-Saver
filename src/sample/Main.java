package sample;

import animatefx.animation.FadeIn;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {
    double x = 0, y = 0;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Insta Saver");
        Image image = new Image("/res/launcher.png");
        primaryStage.getIcons().add(image);
        primaryStage.initStyle(StageStyle.DECORATED);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.show();
        new FadeIn(root).setDelay(new Duration(1)).play();
        root.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (primaryStage.isFullScreen()) {
                    primaryStage.setFullScreen(false);
                } else {
                    primaryStage.setFullScreen(true);
                }
            }

        });
        root.setOnMousePressed(event -> {
            x = primaryStage.getX() - event.getScreenX();
            y = primaryStage.getY() - event.getScreenY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() + x);
            primaryStage.setY(event.getScreenY() + y);
        });
    }


    public static void main(String[] args) {
        System.out.println("Test  = " + Main.class.getResource("sample.fxml"));
        launch(args);
    }
}
