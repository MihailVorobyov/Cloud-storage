package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ByteArrayToStringHandler implements ChannelInboundHandler {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		byte[] bytes = (byte[]) msg;
		String command = new String(bytes, StandardCharsets.UTF_8);
		logger.info(command);
		ctx.fireChannelRead(command);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
	
	}
	
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
	
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
	
	}
	
}
