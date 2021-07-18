package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.00 2021-07-09
 * Отвечает за регистрацию нового пользователя и аутентификацию при входе.
 */
public class AuthHandler extends SimpleChannelInboundHandler<String> {
	
	// TODO: удалить поля после создания БД
	static Map<Channel, String> users = new HashMap<>();
	static Map<String, String> authData = new HashMap<>();
	static List<String> usersOnline = new ArrayList<>();
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println("AuthHandler.channelRead0");
		
		if (msg.startsWith("signin ")) {
			signIn(ctx, msg);
		} else if (msg.startsWith("signup ")) {
			signUp(ctx, msg);
		}
	}
	
	/**
	 * Метод регистрирует нового пользователя. Если такое имя уже существует или недопустимо, метод информирует об этом клиента и
	 * прекращает работу.
	 * Регистрация происходит путём проверки базы данных "users" и добавления новой записи.
	 * @param ctx ChannelHandlerContext
	 * @param msg Сообщение от пользователя, которое начинается с символов "signup"
	 */
	private void signUp(ChannelHandlerContext ctx, String msg) throws IOException {
		System.out.println("AuthHandler.signUp");
		
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signup ", "")
			.replaceAll("\n", "")
			.replaceAll("\r", "")
			.trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		// TODO заменить на обращение к БД
		if (AuthHandler.authData.containsKey(userName)) {
			ctx.writeAndFlush("User already exists");
		} else {
			AuthHandler.users.put(ctx.channel(), userName);
			AuthHandler.authData.put(userName, password);
			if (!Files.exists(Paths.get("server" + File.separator + userName))) {
				Files.createDirectories(Paths.get("server" + File.separator + userName));
			}
			signIn(ctx, msg.replaceFirst("signup", "signin"));
		}
	}
	
	/**
	 * Метод проверяет имя пользователя и пароль на соответствие таковым в базе данных.
	 * @param ctx ChannelHandlerContext
	 * @param msg Сообщение от пользователя, которое начинается с символов "signin"
	 * @return
	 */
	private void signIn(ChannelHandlerContext ctx, String msg) {
		System.out.println("AuthHandler.signIn");
		
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signin ", "")
			.replaceAll("\n", "")
			.replaceAll("\r", "")
			.trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		// TODO заменить на обращение к БД
		if (AuthHandler.authData.containsKey(userName) && password.equals(AuthHandler.authData.get(userName))) {
			if (usersOnline.contains(userName)) {
				ctx.writeAndFlush("User already signed in");
			} else {
				usersOnline.add(userName);
				ctx.writeAndFlush("Hello " + userName + "!\n\r");
				ctx.fireChannelRead("set_user_name " + userName);
				ctx.pipeline().remove(AuthHandler.class);
			}
		} else {
			ctx.writeAndFlush("Wrong name or password");
		}
	}
}
