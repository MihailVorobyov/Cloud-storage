package com.vorobyov.cloudstorage.client.sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class MainController {
	
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	
	static final String ADDRESS = "localhost";
	static final int PORT = 5000;
	
	private boolean isAuthorized;
	
}
