package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.ByteArray;
import com.vorobyov.cloudstorage.server.utils.UserRegistration;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;
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

		ByteBuffer byteBuffer = buf.nioBuffer();
		
		if ("COMMAND".equals(expectFromChannel)) {
			ctx.pipeline().get(ByteArrayToStringHandler.class).channelRead0(ctx, byteBuffer);
			
		} else if ("DATA".equals(expectFromChannel)) {
			ctx.fireChannelRead(byteBuffer);
		}
		
		buf.release();
		byteBuffer.clear();
	}
}