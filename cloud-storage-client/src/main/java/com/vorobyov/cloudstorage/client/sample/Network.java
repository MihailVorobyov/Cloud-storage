package com.vorobyov.cloudstorage.client.sample;

import java.io.*;
import java.net.Socket;

public class Network {
	public static final String ADDRESS = "localhost";
	public static final int PORT = 5000;
	
	private static Socket socket;
	private static InputStream inputStream;
	private static OutputStream outputStream;
	
	public static Socket getSocket() {
		return socket;
	}
	
	public static void closeSocket() {
		try {
			socket.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	public static void connect() {
		try {
			socket = new Socket(ADDRESS, PORT);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static InputStream getInputStream() {
		return inputStream;
	}
	
	public static OutputStream getOutputStream() {
		return outputStream;
	}
}
