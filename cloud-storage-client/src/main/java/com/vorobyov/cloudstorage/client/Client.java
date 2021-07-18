package com.vorobyov.cloudstorage.client;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

public class Client {
	private static Socket socket;
	private static DataInputStream in;
	private static DataOutputStream out;
	
	private static final String ADDRESS = "localhost";
	private static final int PORT = 5000;
	
	public static void main(String[] args) {
		
		
		try {
			
			socket = new Socket(ADDRESS, PORT);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			File file = new File("client" + File.separator + "test.txt");
			if (!file.exists()) {
				throw  new FileNotFoundException();
			}
			
			long fileLength = file.length();
			
			FileInputStream fis = new FileInputStream(file);
			System.out.println("start");
			
			// ------------------------------------------------------------
			out.write("signup user1:pass1".getBytes(StandardCharsets.UTF_8));
			out.flush();
			
			String command = readChannel();
			System.out.println(command);
			
			String s = "upload " + Paths.get("server", "user1", "test2.txt") + " " + fileLength;
			out.write(s.getBytes(StandardCharsets.UTF_8));
			out.flush();
			
			if ("send accepted".equals(readChannel())) {
				getFile(file, fis, out);
			}

			int inByte = -1;
			byte[] inByteBuffer = new byte[128];
	
			inByte = 0;
			
			while ((inByte = in.read(inByteBuffer)) != -1) {
				System.out.println(Arrays.toString(inByteBuffer));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void getFile(File file, FileInputStream fis, DataOutputStream out) throws IOException {
		int read;
		byte[] buffer = new byte[8 * 1024];
		while ((read = fis.read(buffer)) != -1) {
			out.write(buffer, 0, read);
			System.out.println("buffer: " + Arrays.toString(buffer));
		}
		out.flush();
	}
	
	private static String readChannel() throws IOException {
		int read;
		byte[] buffer = new byte[8 * 1024];

		while ((read = in.read(buffer)) != -1) {
			System.out.println("buffer: " + Arrays.toString(buffer));
		}
		StringBuffer sb = new StringBuffer();
		for (byte b : buffer) {
			sb.append(b);
		}
		return sb.toString();
	}
}
