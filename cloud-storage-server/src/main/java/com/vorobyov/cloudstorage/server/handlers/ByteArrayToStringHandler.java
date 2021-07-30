package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.ByteArray;
import io.netty.channel.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ByteArrayToStringHandler extends SimpleChannelInboundHandler<ByteArray> {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteArray byteArray) throws Exception {
		logger.info("Starting read channel...");
		
		String command = new String(byteArray.getData(), StandardCharsets.UTF_8);
		logger.info(command);
		ctx.pipeline().get(CommandsHandler.class).channelRead0(ctx, command);
	}
}
