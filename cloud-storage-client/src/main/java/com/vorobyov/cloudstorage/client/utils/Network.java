package com.vorobyov.cloudstorage.client.utils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Network {
	public static final String ADDRESS = "localhost";
	public static final int PORT = 5000;
	
	private static Socket socket;
	private static InputStream inputStream;
	private static OutputStream outputStream;
	private static ReadableByteChannel rbc;
	private static DataInputStream dataInputStream;
	private static DataOutputStream dataOutputStream;
	private static ByteBuffer byteBuffer;
	
	public static void connect() {
		try {
			socket = new Socket(ADDRESS, PORT);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			dataInputStream = new DataInputStream(inputStream);
			dataOutputStream = new DataOutputStream(outputStream);
			rbc = Channels.newChannel(inputStream);
			byteBuffer = ByteBuffer.allocate(8 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ByteBuffer getByteBuffer() {
		return byteBuffer;
	}
	
	public static ReadableByteChannel getRbc() {
		return rbc;
	}
	
	public static DataInputStream getDataInputStream() {
		return dataInputStream;
	}
	
	public static DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}
	
	
	
	public static InputStream getInputStream() {
		return inputStream;
	}
	
	public static OutputStream getOutputStream() {
		return outputStream;
	}
	
	public static Socket getSocket() {
		return socket;
	}
	
	public static void closeSocket() {
		try {
			inputStream.close();
			outputStream.close();
			socket.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
