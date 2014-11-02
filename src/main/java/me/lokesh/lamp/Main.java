package me.lokesh.lamp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.LAMPService;
import me.lokesh.lamp.ui.multiscreen.ScreensPane;

public class Main extends Application {

    private static final String TITLE = "LAMP | Local Area Music Player";

    private static ScreensPane mainContainer = new ScreensPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("prism.lcdtext", "true");
        Config.load();
        configureStage(primaryStage);
    }

    private void configureStage(Stage primaryStage) {
        mainContainer.loadScreen("signup", "signup.fxml");
        mainContainer.loadScreen("main", "main.fxml");

        if (Config.isRegistered()) {
            LAMPService.start();
            mainContainer.setScreen("main");
        } else {
            mainContainer.setScreen("signup");
        }

        AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(mainContainer, 0.0);
        AnchorPane.setBottomAnchor(mainContainer, 0.0);
        AnchorPane.setLeftAnchor(mainContainer, 0.0);
        AnchorPane.setRightAnchor(mainContainer, 0.0);

        root.getChildren().addAll(mainContainer);

        Scene scene = new Scene(root, 480, 540);
        primaryStage.setScene(scene);
        primaryStage.setTitle(TITLE);
        primaryStage.setMinHeight(270.0);
        primaryStage.setMinWidth(240.0);
        primaryStage.show();

        primaryStage.getIcons().add(Shared.getLogoImage());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if(Config.isRegistered()) {
            LAMPService.stop();
        }
    }
}
