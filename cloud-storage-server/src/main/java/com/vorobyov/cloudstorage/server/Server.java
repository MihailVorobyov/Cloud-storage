package com.vorobyov.cloudstorage.server;

import com.vorobyov.cloudstorage.server.handlers.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.logging.Logger;

public class Server {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public Server() {
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
							new OutboundHandler(),
							new ByteBufToByteArrayHandler(),
//							new UploadFileHandler(),
//							new DownloadHandler(),
							new ByteArrayToStringHandler(),
							new AuthHandler(),
							new CommandsHandler(),
							new StringToByteArrayHandler()

							

							);
					}
				});
			ChannelFuture future = bootstrap.bind(5000).sync();
			logger.info("Server started.");
			future.channel().closeFuture().sync();
			logger.info("Server finished.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		} finally {
			auth.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		new Server();
	}
}
