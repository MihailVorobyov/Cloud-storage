package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ByteBufInputHandler extends ChannelInboundHandlerAdapter {
	File fileToWrite;
	private int fileSize;
	
	public ByteBufInputHandler(Path file, int size) {

		this.fileToWrite = new File(file.toString());
		this.fileSize = size;

	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("send accepted");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		System.out.println("ByteBufInputHandler started");
//		ByteBuf buf = (ByteBuf) msg;
//
//		byte[] buffer;
//		int bufferSize = 1024 * 8;
//		int i;
//
//
//		if (!fileToWrite.exists()) {
//			Files.createDirectories(fileToWrite.toPath().getParent());
//			Files.createFile(fileToWrite.toPath());
//		}
//		RandomAccessFile targetFile = new RandomAccessFile(fileToWrite, "rw");
//		System.out.println("создали файл");
//
//		while (buf.isReadable()) {
//			if (buf.capacity() >= bufferSize) {
//				buffer = new byte[bufferSize];
//			} else {
//				buffer = new byte[buf.capacity()];
//			}
//
//			for (i = 0; i < buffer.length; i++ ) {
//				buffer[i] = buf.readByte();
//			}
//
//			targetFile.write(buffer);
//		}
//		System.out.println("Файл принят");
//		targetFile.close();
		
		//--------------------
		ByteBuf buf = (ByteBuf) msg;
		
		RandomAccessFile raf = new RandomAccessFile(fileToWrite, "rw");
		
		byte[] b = new byte[buf.readableBytes()];
		while (buf.isReadable()) {
			buf.readBytes(b);
		}
		System.out.println("Array b: " + Arrays.toString(b));
		
		raf.write(b);
		try {
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//------------------------
		buf.release();
		ctx.fireChannelRead("ls".getBytes(StandardCharsets.UTF_8));
		ctx.pipeline().remove("ByteBufInputHandler");
	}
}
