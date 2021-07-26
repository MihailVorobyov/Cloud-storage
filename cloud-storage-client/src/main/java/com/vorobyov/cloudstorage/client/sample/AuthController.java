package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.Static;
import com.vorobyov.cloudstorage.client.utils.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AuthController {
	
	static Socket socket;
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
		loginField.setEditable(false);
		passwordField.setEditable(false);
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					Network.connect();
					
					socket = Network.getSocket();
					out = new DataOutputStream(Network.getOutputStream());
					in = new DataInputStream(Network.getInputStream());
					rbc = Channels.newChannel(in);
					byteBuffer = ByteBuffer.allocate(8 * 1024);
		
					write("signup " + loginField.getText() + ":" + passwordField.getText());
					
					result = read();
					
					if ("OK".equals(result.replace("\n", "").replace("\r", "").trim())) {
						setUpUser(loginField.getText());
						this.loginField.getScene().getWindow().hide();
						Main main = new Main();
						main.showWindow();
					} else {
						message.setText(result);
						loginField.setEditable(true);
						passwordField.setEditable(true);
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
			} else {
				message.setText("Password must consists of letters, numbers or symbols, without spaces.");
				loginField.setEditable(true);
				passwordField.setEditable(true);
			}
		} else {
			message.setText("Login must consists of letters, numbers and _ ");
		}
	}
	
	public void signIn(ActionEvent actionEvent) {
		loginField.setEditable(false);
		passwordField.setEditable(false);
		
		if (loginField.getText().matches("\\w+")) {
			if (passwordField.getText().matches("^[.\\S]+")) {
				String result;
				
				try {
					Network.connect();
					socket = Network.getSocket();
					out = new DataOutputStream(Network.getOutputStream());
					in = new DataInputStream(Network.getInputStream());
					rbc = Channels.newChannel(in);
					byteBuffer = ByteBuffer.allocate(8 * 1024);
					
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
					try {
						rbc.close();
						in.close();
						out.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
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
