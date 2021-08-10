package com.vorobyov.cloudstorage.client.sample;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Logger;

public class InputNameDialogController {
	Logger logger = Logger.getLogger(this.getClass().getName());
	protected MainController mainController;
	
	@FXML   TextField nameField;
	@FXML   Label messageLabel;
	@FXML	Button cancelButton;
	@FXML	Button okButton;
	
	@FXML
	public void ok() throws IOException {
		if (nameField.getText().matches(".*[/:*?\"<>|\\\\]+.*")) {
			messageLabel.setText("Symbols \\/:*?\"<>| not provided");
		} else {
			mainController.makeDir(nameField.getText());
			nameField.getScene().getWindow().hide();
		}
	}
	
	@FXML
	public void cancel() throws IOException {
		mainController.makeDir(null);
		nameField.getScene().getWindow().hide();
	}
}
