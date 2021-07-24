package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.UserRegistration;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class InboundByteBufToStringHandler extends ChannelInboundHandlerAdapter {
	
	Logger logger = Logger.getLogger("server.handlers.InboundByteBufToStringHandler");
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
		ctx.fireChannelRead("disconnect " + ctx.channel().remoteAddress());
		ctx.pipeline().addBefore("CommandsHandler","AuthHandler", new InboundAuthHandler());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(this.getClass().getSimpleName() + ".channelRead");
		
		ByteBuf buf = (ByteBuf) msg;
		
		byte[] b = new byte[buf.readableBytes()];
		while (buf.isReadable()) {
			buf.readBytes(b);
		}
		buf.release();
		ctx.fireChannelRead(new String(b, StandardCharsets.UTF_8));
	}
}
