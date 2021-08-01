package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.Network;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Cloud storage");
        Scene scene = new Scene(root, 1100, 555);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            Network.closeSocket();
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
