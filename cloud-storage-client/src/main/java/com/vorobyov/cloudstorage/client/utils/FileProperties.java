package com.vorobyov.cloudstorage.client.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileProperties {
	private String name;
	private String type;
	private Long size;
	private Date date;
	SimpleDateFormat formatter;
	String lmDate;
	
	public FileProperties(String name, String type, Long size, Date date) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.date = date;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		lmDate = formatter.format(date);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setSize(Long size) {
		this.size = size;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Long getSize() {
		return size;
	}
	
	public String getLmDate() {
		return lmDate;
	}
}
