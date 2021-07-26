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
	private long bytesRead;
	
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
		
		if (!Files.exists(fileToWrite)) {
			Files.createDirectories(fileToWrite.getParent());
			Files.createFile(fileToWrite);
		}
		
		RandomAccessFile raf = new RandomAccessFile(fileToWrite.toString(), "rw");
		
		while (bytesRead != fileSize) {
			byte[] b = new byte[buf.readableBytes()];
			
			while (buf.isReadable()) {
				buf.readBytes(b);
			}
			buf.release();
			raf.write(b);
			bytesRead += b.length;
		}
		
		if (fileSize == Files.size(fileToWrite)) {
//			ctx.fireChannelRead("ls".getBytes(StandardCharsets.UTF_8));
			ctx.fireChannelRead("Upload completed".getBytes(StandardCharsets.UTF_8));
			
		} else {
			ctx.fireChannelRead("Upload failed!".getBytes(StandardCharsets.UTF_8));
		}
		
		try {
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ctx.pipeline().addFirst(new InboundByteBufToStringHandler());
		ctx.pipeline().remove(InboundUploadFileHandler.class);
	}
}
