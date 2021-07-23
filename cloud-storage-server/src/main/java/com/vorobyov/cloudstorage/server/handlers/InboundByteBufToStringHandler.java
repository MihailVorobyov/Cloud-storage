package com.vorobyov.cloudstorage.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class InboundByteBufToStringHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//TODO заменить на логгер
		System.out.println(ctx.channel().remoteAddress() + " connected");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(this.getClass().getSimpleName() + ".channelRead");
		
		ByteBuf buf = (ByteBuf) msg;
		
		byte[] b = new byte[buf.readableBytes()];
		while (buf.isReadable()) {
			buf.readBytes(b);
		}
		buf.release();
		ctx.fireChannelRead(new String(b, StandardCharsets.UTF_8));
	}
}
