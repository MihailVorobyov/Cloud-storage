package com.vorobyov.cloudstorage.client.sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AuthController {
	
	static final String DELIMITER = " ";
	static Socket  socket;
	static DataInputStream in;
	static DataOutputStream out;
	static ReadableByteChannel rbc;
	static ByteBuffer byteBuffer;
	
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
				rbc = Channels.newChannel(in);
				byteBuffer = ByteBuffer.allocate(8 * 1024);

				write("signup " + loginField.getText() + ":" + passwordField.getText());
				
				result = read();
				
				message.setText(result);
				
				if ("OK".equals(result.replace("\n", "").replace("\r", "").trim())) {
					this.loginField.getScene().getWindow().hide();
					Main main = new Main();
					main.showWindow();
//					Stage primaryStage = new Stage();
//					Parent root = FXMLLoader.load(getClass().getResource("/mainSample.fxml"));
//					primaryStage.setTitle("Cloud storage");
//					primaryStage.setScene(new Scene(root, 1300, 480));
//					primaryStage.setResizable(false);
//					primaryStage.setOnCloseRequest(e -> {
//						Platform.exit();
//						System.exit(0);
//					});
//					primaryStage.show();
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					
					rbc.close();
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
		main.start(new Stage());
		
		
		Stage stage = (Stage) loginField.getScene().getWindow();
		stage.close();
	}
	
	public void signIn(ActionEvent actionEvent) {
	
	}
		
	public String read () throws IOException {
		
		int readNumberBytes = rbc.read(byteBuffer);
		String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
		byteBuffer.clear();
		return serverAnswer;
		
	}
}
