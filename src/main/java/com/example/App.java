package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/primary.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
            // Stage boyutlarını ayarla
        primaryStage.setWidth(1280);  // İstediğiniz genişlik
        primaryStage.setHeight(720); // İstediğiniz yükseklik
        primaryStage.setScene(scene);
        primaryStage.setTitle("CSV Reader");
        primaryStage.show();
    }


    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}