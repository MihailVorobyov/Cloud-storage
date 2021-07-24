package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.UserRegistration;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * @version 1.00 2021-07-09
 * Отвечает за регистрацию нового пользователя и аутентификацию при входе.
 */
public class InboundAuthHandler extends SimpleChannelInboundHandler<String> {
	Logger logger = Logger.getLogger("server.handlers.InboundAuthHandler");
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		if (msg.startsWith("signIn ")) {
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
		
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signup ", "")
			.replaceAll("\n", "")
			.replaceAll("\r", "")
			.trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		if (UserRegistration.authData.containsKey(userName)) {
			logger.info("User " + userName + " already exists.");
			ctx.writeAndFlush("User already exists");
		} else {
			UserRegistration.authData.put(userName, password);
			
			if (!Files.exists(Paths.get("server" + File.separator + userName))) {
				Files.createDirectories(Paths.get("server" + File.separator + userName));
			}
			
			logger.info("User " + userName + " registered.");
			signIn(ctx, msg.replaceFirst("signup", "signIn"));
		}
	}
	
	/**
	 * Метод проверяет имя пользователя и пароль на соответствие таковым в базе данных.
	 * @param ctx ChannelHandlerContext
	 * @param msg Сообщение от пользователя, которое начинается с символов "signin"
	 */
	private void signIn(ChannelHandlerContext ctx, String msg) {
		
		String userName;
		String password;
		
		String[] s = msg.replaceFirst("signIn ", "")
			.replaceAll("\n", "")
			.replaceAll("\r", "")
			.trim().split(":", 2);
		userName = s[0];
		password = s[1];
		
		UserRegistration.addressUser.put(ctx.channel().remoteAddress(), userName);
		
		if (UserRegistration.authData.containsKey(userName) && password.equals(UserRegistration.authData.get(userName))) {
			if (UserRegistration.usersOnline.contains(userName)) {
				logger.info("User " + userName + " try to sign in, but already signed in.");
//				ctx.pipeline().get(OutboundHandler.class).write(ctx, "User already signed in", ctx.newPromise());
				UserRegistration.usersOnline.add(userName);
				ctx.fireChannelRead("User already signed in");
				
			} else {
				UserRegistration.usersOnline.add(userName);
				ctx.pipeline().get(OutboundHandler.class).write(ctx, "OK", ctx.newPromise());
				ctx.fireChannelRead("set_user_name " + userName);
			}
		} else {
			logger.info("Wrong name or password: name = " + userName + ", password = " + password + ".");
			ctx.writeAndFlush("Wrong name or password");
		}
	}
}
