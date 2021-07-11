package com.vorobyov.cloudstorage.server.handlers;

import com.sun.javaws.JAuthenticator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @version 1.00 2021-07-09
 * Отвечает за регистрацию нового пользователя и аутентификацию при входе.
 */
public class AuthHandler extends SimpleChannelInboundHandler<String> {
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client connected: " + ctx.channel());
//		ctx.read().flush(); //TODO удалить
		ctx.writeAndFlush("Authentication"); //TODO удалить
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		if (msg.startsWith("signIn ")) {
			signIn(ctx, msg);
		} else if (msg.startsWith("signUp ")) {
			signUp(ctx, msg);
		} else {
			ctx.writeAndFlush(msg);
		}
	}
	
	/**
	 * Метод регистрирует нового пользователя. Если такое имя уже существует или недопустимо, метод информирует об этом клиента и
	 * прекращает работу.
	 * Регистрация происходит путём проверки базы данных "users" и добавления новой записи.
	 * @param ctx ChannelHandlerContext
	 * @param msg Сообщение от пользователя, которое начинается с символов "signUp"
	 * @return
	 */
	private void signUp(ChannelHandlerContext ctx, String msg) throws IOException {
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signUp", "").replaceAll("\n\r", "").trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		// TODO заменить на обращение к БД
		if ("user1".equalsIgnoreCase(userName)) {
			ctx.writeAndFlush("user1 already exists");
		} else {
			ctx.writeAndFlush("user1 registered");
			Files.createDirectories(Paths.get(userName));
		}
	}
	
	/**
	 * Метод проверяет имя пользователя и пароль на соответствие таковым в базе данных.
	 * @param ctx ChannelHandlerContext
	 * @param msg Сообщение от пользователя, которое начинается с символов "signIn"
	 * @return
	 */
	private void signIn(ChannelHandlerContext ctx, String msg) {
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signUp", "").replaceAll("\n\r", "").trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		// TODO заменить на обращение к БД
		if ("user1".equalsIgnoreCase(userName) && "pass".equals(password)) {
			ctx.writeAndFlush("Hello user1");
		} else {
			ctx.writeAndFlush("Wrong user name or password!");
		}
	}
}
