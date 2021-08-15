package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class InputNameDialog extends Application {
    
//    protected String newFileName;
    private final MainController mainController;
    
    Stage stage = new Stage();
    
    protected InputNameDialog(MainController controller) throws Exception {
        mainController = controller;
        start(new Stage());
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InputNameDialog.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 300, 170);
        primaryStage.setTitle("Input");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        InputNameDialogController controller = loader.getController();
        controller.mainController = mainController;
        primaryStage.setOnCloseRequest(e -> {
            primaryStage.hide();
//            Platform.exit();
        });
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    protected void setName(String s) {
        try {
            mainController.makeDir(s);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
