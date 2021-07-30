package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.Network;
import com.vorobyov.cloudstorage.client.utils.Static;
import com.vorobyov.cloudstorage.client.utils.User;
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

public class AuthController {
	
	private DataInputStream in;
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
	
	public void signUp(ActionEvent actionEvent) {
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					Network.connect();
					
					out = Network.getDataOutputStream();
					in = Network.getDataInputStream(); //TODO убрать?
					rbc = Network.getRbc();
					byteBuffer = Network.getByteBuffer(); //TODO убрать?
		
					write("signup " + loginField.getText() + ":" + passwordField.getText());
					
					result = read().replace("\n", "").replace("\r", "").trim();
					// Если пользователь уже зарегистрирован
					if ("User already exists".equals(result)) {
						//TODO
					} else if ("wrong name or password".equals(result)) {
						//TODO
					}else if ("signIn successful".equals(result)) {
						setUpUser(loginField.getText());
						this.loginField.getScene().getWindow().hide();
						Main main = new Main();
						main.showWindow();
					} else {
						message.setText(result);
					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					rbc = null;
					in = null;
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
		
		String result;
		
			try {
				Network.connect();
				
				out = Network.getDataOutputStream();
				in = Network.getDataInputStream();
				rbc = Network.getRbc();
				byteBuffer = Network.getByteBuffer();
				
				write("signIn " + loginField.getText() + ":" + passwordField.getText());
				
				result = read();
				
				if ("OK".equals(result.replace("\n", "").replace("\r", "").trim())) {
					setUpUser(loginField.getText());
					this.loginField.getScene().getWindow().hide();
					Main main = new Main();
					main.showWindow();
				} else {
					loginField.setEditable(true);
					passwordField.setEditable(true);
					message.setText(result);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				rbc = null;
				in = null;
				out = null;
			}
	}
	
	//TODO переименовать в signIn после отладки
	public void signIn0(ActionEvent actionEvent) {
		loginField.setEditable(false);
		passwordField.setEditable(false);
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					Network.connect();
					
					out = Network.getDataOutputStream();
					in = Network.getDataInputStream();
					rbc = Network.getRbc();
					byteBuffer = Network.getByteBuffer();
					
					write("signIn " + loginField.getText() + ":" + passwordField.getText());
					
					result = read();
					
					if ("OK".equals(result.replace("\n", "").replace("\r", "").trim())) {
						setUpUser(loginField.getText());
						this.loginField.getScene().getWindow().hide();
						Main main = new Main();
						main.showWindow();
					} else {
						loginField.setEditable(true);
						passwordField.setEditable(true);
						message.setText(result);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					rbc = null;
					in = null;
					out = null;
				}
			} else {
				message.setText("Password must consists of letters, numbers or symbols, without spaces.");
				loginField.setEditable(true);
				passwordField.setEditable(true);
			}
		} else {
			message.setText("Login must consists of letters, numbers and _ ");
			loginField.setEditable(true);
			passwordField.setEditable(true);
		}
	}
		
	public String read () throws IOException {
		int readNumberBytes = rbc.read(byteBuffer);
		String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
		byteBuffer.clear();
		return serverAnswer;
	}
	
	private void write(String s) {
		try {
			out.write(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setUpUser(String name) {
		User user = new User(name);
		Static.setUser(user);
	}
	
}
