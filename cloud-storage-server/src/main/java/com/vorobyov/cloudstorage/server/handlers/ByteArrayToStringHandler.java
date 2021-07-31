package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.ByteArray;
import io.netty.channel.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class ByteArrayToStringHandler extends SimpleChannelInboundHandler<ByteBuffer> {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuffer byteArray) throws Exception {
		logger.info("Starting read channel...");
		
		byte[] bytes = new byte[byteArray.capacity()];
		int bytesPosition = -1;

		while (byteArray.hasRemaining()) {
			bytes[++bytesPosition] = byteArray.get();
		}
		
		String command = new String(bytes, StandardCharsets.UTF_8);
		logger.info(command);
		ctx.pipeline().get(CommandsHandler.class).channelRead0(ctx, command);
	}
}
