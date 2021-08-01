package com.vorobyov.cloudstorage.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileProperties {
	private final String name;
	private final String type;
	private final Long size;
	private final Date date;
	private SimpleDateFormat formatter;
	private String lmDate;
	
	public FileProperties(String name, String type, Long size, Date date) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.date = date;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		lmDate = formatter.format(date);
	}
	
	/**
	 * Возвращает параметры файла, разделённые последовательностью ";;", в виде строки
	 * @return  возвращает строку
	 */
	@Override
	public String toString() {
		
		return String.join(";;",name, type, String.valueOf(size), String.valueOf(date.getTime()));
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
