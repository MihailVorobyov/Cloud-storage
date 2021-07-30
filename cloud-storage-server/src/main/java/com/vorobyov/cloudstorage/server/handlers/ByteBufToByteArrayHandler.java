package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.ByteArray;
import com.vorobyov.cloudstorage.server.utils.UserRegistration;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.logging.Logger;

public class ByteBufToByteArrayHandler extends ChannelInboundHandlerAdapter {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private String expectFromChannel = "COMMAND";
	
	/**
	 * Для перенаправления входящих байтов в CommandsHandler
	 */
	public void expectCommand() {
		expectFromChannel = "COMMAND";
	}
	
	/**
	 * Для перенаправления входящих байтов в UploadHandler
	 */
	public void expectData() {
		expectFromChannel = "DATA";
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Client " + ctx.channel().remoteAddress() + " connected.");
		
		//TODO удалить после проверки
		UserRegistration.authData.put("user1", "pass1");
		UserRegistration.authData.put("user2", "pass2");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		logger.info("Client " + ctx.channel().remoteAddress() + " disconnected.");
		UserRegistration.usersOnline.remove(UserRegistration.addressUser.get(ctx.channel().remoteAddress()));
		UserRegistration.addressUser.remove(ctx.channel().remoteAddress());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		logger.info("ByteBuf length = " + buf.readableBytes());
		
		byte[] b = new byte[buf.readableBytes()];
		while (buf.isReadable()) {
			buf.readBytes(b);
		}
		
		if ("COMMAND".equals(expectFromChannel)) {
			ctx.pipeline().get(ByteArrayToStringHandler.class).channelRead0(ctx, new ByteArray(b));
		} else if ("DATA".equals(expectFromChannel)) {
			ctx.fireChannelRead(new ByteArray(b));
		}
	}
}