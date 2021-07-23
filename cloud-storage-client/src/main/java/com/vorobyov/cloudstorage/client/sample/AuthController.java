package com.vorobyov.cloudstorage.client.sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AuthController {
	
	Socket socket;
	DataOutputStream out;
	DataInputStream in;
	
	@FXML
	public TextField loginField;
	
	@FXML
	public PasswordField passwordField;
	
	@FXML
	public Button signupButton;
	
	@FXML
	public Button signInButton;
	
	@FXML
	public Label message;
	
	public void signUp(ActionEvent actionEvent) {
			
			String result = null;
			
			try {
				socket = new Socket(MainController.ADDRESS, MainController.PORT);
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				
				System.out.println("signup " + loginField.getText() + ":" + passwordField.getText());
				
				write("signup " + loginField.getText() + ":" + passwordField.getText());
				
				result = read();
				
				message.setText(result);
				
			if (result == null) {
				System.out.println("null!!!!!!!!");
			} else if ("OK".equals(result)) {
				exitSignup();
			}
			
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	private void write(String s) {
		try {
			out.write(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exitSignup() throws Exception {
		Main main = new Main();
		main.showWindow();
		Stage stage = (Stage) loginField.getScene().getWindow();
		stage.close();
	}
	
	public void signIn(ActionEvent actionEvent) {
	
	}
		
	public String read () throws IOException {
		byte[] data = null;
		int length;
		
		while (true) {
			if ((length = in.available()) > 0) {
				data = new byte[length];
				in.readFully(data, 0, length);
			} else {
				break;
			}
		}
		return new String(data, 0, length, StandardCharsets.UTF_8);
	}
}
