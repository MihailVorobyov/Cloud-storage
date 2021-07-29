package com.vorobyov.cloudstorage.client.utils;

import java.nio.file.Paths;

public class User {
	private String userName;
	private String currentServerPath;
	private String currentLocalPath;
	private String sortBy;
	
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public String getSortBy() {
		return sortBy;
	}
	
	public User(String userName) {
		this.userName = userName;
		this.currentLocalPath = Paths.get(".").toAbsolutePath().getRoot().toString();
		this.currentServerPath = userName;
		this.sortBy = "name";
	}
	
	public void setCurrentServerPath(String currentServerPath) {
		this.currentServerPath = currentServerPath;
	}
	
	public void setCurrentLocalPath(String currentLocalPath) {
		this.currentLocalPath = currentLocalPath;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getCurrentServerPath() {
		return currentServerPath;
	}
	
	public String getCurrentLocalPath() {
		return currentLocalPath;
	}
}
