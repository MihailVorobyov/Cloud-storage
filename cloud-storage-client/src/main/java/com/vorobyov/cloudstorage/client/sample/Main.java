package com.vorobyov.cloudstorage.client.sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    Stage stage = new Stage();

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/mainSample.fxml"));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 1300, 480));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void  showWindow() throws Exception {
        start(stage);
    }
}
