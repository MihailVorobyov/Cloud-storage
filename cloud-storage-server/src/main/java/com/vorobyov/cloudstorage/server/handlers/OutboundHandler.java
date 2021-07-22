package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
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

public class OutboundHandler extends ChannelOutboundHandlerAdapter {
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		String message = (String) msg;
		ByteBuf buf = ctx.alloc().directBuffer();
		System.out.println("out: " + message);
		buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
		ctx.writeAndFlush(buf);
	}
}
