package com.vorobyov.cloudstorage.client.sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainController {
	
	static final String DELIMITER = " ";
	static Socket  socket;
	static DataInputStream in;
	static DataOutputStream out;
	static ReadableByteChannel rbc;
	static ByteBuffer byteBuffer;
	Network network = new Network();
	
	
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
			socket = network.getSocket();
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
	
	public String read() throws IOException {
		int readNumberBytes = rbc.read(byteBuffer);
		String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
		byteBuffer.clear();
		return serverAnswer;
	}
}
