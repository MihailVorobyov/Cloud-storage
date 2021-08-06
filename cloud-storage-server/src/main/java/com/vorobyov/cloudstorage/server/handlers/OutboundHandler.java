package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.UserRegistration;
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
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		logger.info("Starting write to channel...");
		
		byte[] bytes = (byte[]) msg;
//		logger.info("output message: " + new String(bytes, StandardCharsets.UTF_8));
		ByteBuf buf = Unpooled.directBuffer();
		buf.writeBytes(bytes);
		ctx.writeAndFlush(buf);
	}
}
