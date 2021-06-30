package com.vorobyov.cloudstorage.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {
	public NettyServer() {
		EventLoopGroup auth = new NioEventLoopGroup(1);
		EventLoopGroup worker = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			
			bootstrap.group(auth, worker)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer() {
					@Override
					protected void initChannel(Channel channel) throws Exception {
						channel.pipeline().addLast(
							new StringDecoder(), // in - 1
							new StringEncoder() // out - 1
						);
					}
				});
			ChannelFuture future = bootstrap.bind(5000).sync();
			System.out.println("Server started");
			future.channel().closeFuture().sync();
			System.out.println("Server finished");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			auth.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		new NettyServer();
	}
}
