package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public class OutboundHandler extends ChannelOutboundHandlerAdapter {
	Logger logger = Logger.getLogger("com.vorobyov.cloudstorage.server.handlers.OutboundHandler");
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		String message = String.valueOf(msg);
		logger.info("output message: " + message);
		ByteBuf buf = Unpooled.buffer().writeBytes(message.concat("\n\r").getBytes(StandardCharsets.UTF_8));

		ctx.writeAndFlush(buf);
	}
}
