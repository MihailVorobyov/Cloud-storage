package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class NewLineHandler extends ChannelOutboundHandlerAdapter {
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		String message = (String)msg;
		String finalMessage;
		if (message.startsWith("!newline!")) {
			finalMessage = message.replaceFirst("!newline!", "");
		} else {
			finalMessage = message.concat("\n\r");
		}
		super.write(ctx, finalMessage, promise);
	}
}
