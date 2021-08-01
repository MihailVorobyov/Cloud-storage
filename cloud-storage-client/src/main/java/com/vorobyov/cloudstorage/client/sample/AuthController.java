package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.Network;
import com.vorobyov.cloudstorage.client.utils.Static;
import com.vorobyov.cloudstorage.client.utils.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class AuthController {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private DataOutputStream out;
	private ReadableByteChannel rbc;
	private ByteBuffer byteBuffer;
	
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
	
	public AuthController() {
		Network.connect();
		
		out = Network.getDataOutputStream();
		rbc = Network.getRbc();
		byteBuffer = Network.getByteBuffer(); //TODO убрать?
	}
	
	public void signUp(ActionEvent actionEvent) {
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					auth("signup " + loginField.getText() + ":" + passwordField.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					rbc = null;
					out = null;
				}
			} else {
				message.setText("Password must consists of letters, numbers or symbols, without spaces.");
				loginField.setEditable(true);
				passwordField.setEditable(true);
			}
		} else {
			message.setText("Login must consists of letters, numbers and _ ");
		}
	}
	
	//TODO удалить после отладки
	public void signIn(ActionEvent actionEvent) {
		loginField.setText("user1");
		passwordField.setText("pass1");
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					auth("signIn " + loginField.getText() + ":" + passwordField.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					rbc = null;
					out = null;
				}
			} else {
				message.setText("Password must consists of letters, numbers or symbols, without spaces.");
			}
		} else {
			message.setText("Login must consists of letters, numbers and _ ");
		}
	}
	
	private void auth(String s) throws Exception {
		String result;
//		logger.info("write..." + s);
		write(s);
		
//		logger.info("read...");
		result = read().replace("\n", "").replace("\r", "").trim();
		
		if ("signIn successful".equals(result)) {
			setUpUser(loginField.getText());
			Main main = new Main();
			main.showWindow();
			this.loginField.getScene().getWindow().hide();
		} else {
			message.setText(result);
		}
	}
		
	public String read () throws IOException {
		int readNumberBytes = rbc.read(byteBuffer);
		String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
		logger.info(serverAnswer);
		
		byteBuffer.clear();
		return serverAnswer;
	}
	
	private void write(String s) {
//		logger.info(s);
		try {
			out.write(s.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setUpUser(String name) {
		User user = new User(name);
		Static.setUser(user);
	}
	
}
