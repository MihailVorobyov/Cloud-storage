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
			
			System.out.println("---start---");
			
			File file = new File(Paths.get("cloud-storage-client", "src", "client", "test.txt").toString());
			if (!file.exists()) {
				throw new FileNotFoundException();
			}
			
			long fileLength = file.length();

			// ------------------------------------------------------------
			
			out.write("signup user1:pass1".getBytes(StandardCharsets.UTF_8));
			out.flush();
			
			System.out.println("---wait response---");
			
			String command = readChannel();
//			String command = in.readUTF();

			System.out.println(command);
			
			String s = "upload " + Paths.get("user1", "test.txt") + " " + fileLength;
			System.out.println(s);
			
			out.write(s.getBytes(StandardCharsets.UTF_8));
			out.flush();
			
//			while (!"ready to download".equals(readChannel())) {
//
//			}
			sendFile(file);
			
			
			System.out.println("End");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void sendFile(File file) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
//		FileOutputStream fis = new FileInputStream(file);
		
		int read = 0;
		byte[] buffer = new byte[8 * 1024];
		while ((read = raf.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		System.out.println("File sended");
		out.flush();
	}
	
	private static String readChannel() throws IOException {
		
//		byte[] buffer = new byte[1024];
		int length = in.readInt();
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		
//		int i;
//		int read = 0;
//		while ((i = in.read()) != -1) {
//			buffer[read++] = (byte) i;
//			System.out.println(buffer[read - 1]);
//		}
		return new String(buffer, StandardCharsets.UTF_8);
//		return new String(buffer, 0, read, StandardCharsets.UTF_8);
	}
}
