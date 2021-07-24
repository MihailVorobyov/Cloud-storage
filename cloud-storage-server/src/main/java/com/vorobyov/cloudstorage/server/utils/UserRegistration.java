package com.vorobyov.cloudstorage.server.utils;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRegistration {
	
	
	public static Map<SocketAddress, String> addressUser = new HashMap<>();
	public static Map<String, String> authData = new HashMap<>();
	public static List<String> usersOnline = new ArrayList<>();
	
	/**
	 * Загружает базу пользователей при старте сервера
	 */
	public static void loadUserListFromDB() {
		// TODO сделать обращение к БД
	}
	
	/**
	 * Добавляет пользователя в базу данных
	 */
	public static void addUserToDB(String name, String pass) {
		// TODO сделать обращение к БД
	}
	
}
