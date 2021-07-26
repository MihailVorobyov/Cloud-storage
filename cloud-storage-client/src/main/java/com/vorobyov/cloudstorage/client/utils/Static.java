package com.vorobyov.cloudstorage.client.utils;

public class Static {
	private static User user;
	
	public static void setUser(User u) {
		user = u;
	}
	
	public static User getUser() {
		return user;
	}
}
