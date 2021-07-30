package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UploadFileHandler implements ChannelInboundHandler {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Path fileToWrite;
	private long fileSize;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		ByteBuf byteBuf = (ByteBuf) msg;
		
		File file = new File(String.valueOf(fileToWrite));
		if (file.exists()) {
			Files.delete(fileToWrite);
		}
		Files.createFile(fileToWrite);
		writeBytesToFile(byteBuf, file, ctx);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
	
	}
	
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
	
	}
	
	private void writeBytesToFile(ByteBuf byteBuf, File file, ChannelHandlerContext ctx) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
		     FileChannel fileChannel = raf.getChannel()) {
			ByteBuffer[] byteBuffers = byteBuf.nioBuffers();
			
			for (int i = 0; i < byteBuf.nioBufferCount(); i++) {
				while (byteBuffers[i].hasRemaining()) {
					fileChannel.position(file.length());
					fileChannel.write(byteBuffers[i]);
				}
			}
			
			if (fileSize == Files.size(fileToWrite)) {
				logger.info("/upload complete");
				ctx.pipeline().write("/upload complete");
				ctx.pipeline().addFirst(new ByteArrayToStringHandler());
				ctx.pipeline().remove(UploadFileHandler.class);
			}
				//			} else {
//				logger.info("/upload failed");
//				ctx.pipeline().write("/upload failed");
//			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			byteBuf.release();
		}
	}
	
	public void setFileToWrite(Path fileToWrite) {
		this.fileToWrite = fileToWrite;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
