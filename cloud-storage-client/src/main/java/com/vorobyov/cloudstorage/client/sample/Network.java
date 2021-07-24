package com.vorobyov.cloudstorage.client.sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
	public final String ADDRESS = "localhost";
	public final int PORT = 5000;
	
	private Socket socket;
	
	public Network() {
		try {
			socket = new Socket(ADDRESS, PORT);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
}
