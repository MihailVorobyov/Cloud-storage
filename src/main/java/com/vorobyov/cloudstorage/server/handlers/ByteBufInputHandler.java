package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ByteBufInputHandler extends ChannelInboundHandlerAdapter {
	Path filePath;
	private int fileSize;
	
	public ByteBufInputHandler(Path file, int size) {
		
		if (!Files.exists(file)) {
			this.filePath = file;
			this.fileSize = size;
		}
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] buffer = new byte[1024 * 8];
//		int bytesRead = 0;
//		int bytesToRead = buf.readableBytes();
		
		while (buf.isReadable()) {
			buf.readBytes(buffer);
			Files.write(filePath, buffer, StandardOpenOption.APPEND);
		}
		
		ctx.fireChannelRead("ls");
		ctx.pipeline().remove("ByteBufInputHandler");
	}
}
