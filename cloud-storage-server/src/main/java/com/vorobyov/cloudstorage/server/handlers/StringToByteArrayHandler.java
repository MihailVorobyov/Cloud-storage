package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class StringToByteArrayHandler extends SimpleChannelInboundHandler<String> {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
		logger.info("Starting write to channel...");

		logger.info("output message: " + s);
		ctx.write(s.getBytes(StandardCharsets.UTF_8));
	}
}
