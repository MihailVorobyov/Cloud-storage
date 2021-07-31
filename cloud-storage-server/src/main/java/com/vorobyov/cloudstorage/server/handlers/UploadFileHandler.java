package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UploadFileHandler extends SimpleChannelInboundHandler<ByteBuffer> {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Path fileToWrite;
	private long fileSize;
	private long bytesRead;
	private RandomAccessFile raf = null;
	private FileChannel fileChannel = null;
	private boolean isReadInProgress = false;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuffer msg) throws Exception {
		logger.info("Starting read channel...");
		if (!isReadInProgress) {
			if (Files.exists(fileToWrite)) {
				Files.delete(fileToWrite);
			}
			Files.createFile(fileToWrite);
			isReadInProgress = true;
		}
		writeBytesToFile(msg, ctx);
	}
	
	private void writeBytesToFile(ByteBuffer msg, ChannelHandlerContext ctx) {
		try {
			if (raf == null) {
				raf = new RandomAccessFile(fileToWrite.toString(), "rw");
				logger.info("raf created");
				if (fileChannel == null) {
					fileChannel = raf.getChannel();
					logger.info("fileChannel created");
				}
			}
			
//			ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getData());

//			for (ByteBuffer b : msg) {
				bytesRead += fileChannel.write(msg, bytesRead);
//			}
			
			
			logger.info("read " + bytesRead + " bytes");
			logger.info("File size " + Files.size(fileToWrite) + " bytes");
			
			if (fileSize == Files.size(fileToWrite)) {
				logger.info("/upload complete");
				
				fileSize = 0;
				fileToWrite = null;
				bytesRead = 0;
				isReadInProgress = false;
				
				msg.clear();
				ctx.pipeline().get(ByteBufToByteArrayHandler.class).expectCommand();
//				ctx.pipeline().lastContext().fireChannelRead("/upload complete".getBytes(StandardCharsets.UTF_8));
				ctx.fireChannelRead(ByteBuffer.wrap("/upload complete".getBytes(StandardCharsets.UTF_8)));

			}
				//			} else {
//				logger.info("/upload failed");
//				ctx.pipeline().write("/upload failed");
//			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (!isReadInProgress) {
					if (fileChannel != null) {
						fileChannel.close();
						fileChannel = null;
					}
					if (raf != null) {
						raf.close();
						raf = null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setFileToWrite(Path fileToWrite) {
		this.fileToWrite = fileToWrite;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
