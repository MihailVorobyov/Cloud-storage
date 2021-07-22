package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InboundUploadFileHandler extends ChannelInboundHandlerAdapter {
	private final Path fileToWrite;
	private final long fileSize;
	
	public InboundUploadFileHandler(String file, long size) {
		this.fileToWrite = Paths.get(file);
		this.fileSize = size;

	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.pipeline().remove(InboundByteBufToStringHandler.class);
		ctx.fireChannelRead("ready to download");
//		ctx.writeAndFlush("sending accepted");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] b = new byte[buf.readableBytes()];
		while (buf.isReadable()) {
			buf.readBytes(b);
		}
		
		if (!Files.exists(fileToWrite)) {
			Files.createDirectories(fileToWrite.getParent());
			Files.createFile(fileToWrite);
		}

		RandomAccessFile raf = new RandomAccessFile(fileToWrite.toString(), "rw");
		
		raf.write(b);
		try {
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		buf.release();
		ctx.fireChannelRead("ls".getBytes(StandardCharsets.UTF_8));
		ctx.pipeline().remove(InboundUploadFileHandler.class);
		ctx.pipeline().addFirst(new InboundByteBufToStringHandler());
	}
}
