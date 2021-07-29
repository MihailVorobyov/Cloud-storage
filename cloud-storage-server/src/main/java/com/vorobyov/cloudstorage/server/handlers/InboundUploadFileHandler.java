package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class InboundUploadFileHandler extends ChannelInboundHandlerAdapter {
	Logger logger = Logger.getLogger("com.vorobyov.cloudstorage.server.handlers.InboundUploadFileHandler");
	
	private final Path fileToWrite;
	private final long fileSize;
	
	public InboundUploadFileHandler(String file, long size) {
		this.fileToWrite = Paths.get(file);
		this.fileSize = size;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.pipeline().remove(InboundByteBufToStringHandler.class);
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
				ctx.pipeline().addFirst(new InboundByteBufToStringHandler());
				ctx.pipeline().remove(InboundUploadFileHandler.class);
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
}
