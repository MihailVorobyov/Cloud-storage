package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.ByteArray;
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

public class UploadFileHandler extends SimpleChannelInboundHandler<ByteArray> {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Path fileToWrite;
	private long fileSize;
	private long bytesRead;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteArray msg) throws Exception {
		logger.info("Starting read channel...");
		
		if (Files.exists(fileToWrite)) {
			Files.delete(fileToWrite);
		}
		Files.createFile(fileToWrite);
		writeBytesToFile(msg, ctx);
	}
	
	private void writeBytesToFile(ByteArray msg, ChannelHandlerContext ctx) {
		try (RandomAccessFile raf = new RandomAccessFile(fileToWrite.toString(), "rw");
		     FileChannel fileChannel = raf.getChannel()) {
			
//			ByteBuffer[] byteBuffers = byteBuf.nioBuffers();
			ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getData());

//			for (int i = 0; i < byteBuf.nioBufferCount(); i++) {
//				while (byteBuffers[i].hasRemaining()) {
//					fileChannel.position(file.length());
//					fileChannel.write(byteBuffers[i]);
//				}
//			}
			
			bytesRead += fileChannel.write(byteBuffer, bytesRead);
			
			logger.info("read " + bytesRead + " bytes");
			logger.info("File size " + Files.size(fileToWrite) + " bytes");
			
			if (fileSize == bytesRead) {
				logger.info("/upload complete");
				fileSize = 0;
				fileToWrite = null;
				bytesRead = 0;
				ctx.pipeline().get(ByteBufToByteArrayHandler.class).expectCommand();
//				ctx.pipeline().lastContext().fireChannelRead("/upload complete".getBytes(StandardCharsets.UTF_8));
				ctx.fireChannelRead(new ByteArray("/upload complete".getBytes(StandardCharsets.UTF_8)));

			}
				//			} else {
//				logger.info("/upload failed");
//				ctx.pipeline().write("/upload failed");
//			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
//		} finally {
//			byteBuf.release();
//		}
	}
	
	public void setFileToWrite(Path fileToWrite) {
		this.fileToWrite = fileToWrite;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
