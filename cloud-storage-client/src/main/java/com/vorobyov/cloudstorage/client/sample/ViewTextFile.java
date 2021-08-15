package com.vorobyov.cloudstorage.client.sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;

public class ViewTextFile extends Application {
    
    ViewTextFileController controller;
    File textFile;
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewTextFile.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 460, 580);
        primaryStage.setTitle(textFile.getName());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.initModality(Modality.WINDOW_MODAL);
        controller = loader.getController();
        controller.textFile = textFile;
        controller.window = primaryStage;
        controller.init();
        primaryStage.setOnCloseRequest(e -> {
            primaryStage.hide();
//            Platform.exit();
        });
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void  showWindow(File textFile) throws Exception {
        this.textFile = textFile;
        start(new Stage());
    }
}
