package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.logging.Logger;

public class DownloadHandler implements ChannelOutboundHandler {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void bind(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void connect(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void disconnect(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void close(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void deregister(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void read(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
	
	}
	
	@Override
	public void flush(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
	
	}
}
