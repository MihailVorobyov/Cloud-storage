package com.vorobyov.cloudstorage.server.utils;

public class Message {
	private byte[] data;
	
	public Message(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
}
