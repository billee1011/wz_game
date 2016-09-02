package net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.handler.ServerHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WZ on 2016/8/25.
 */
public class NetServer {
	private ServerBootstrap boot = new ServerBootstrap();

	private ServerHandler handler;

	private ByteToMessageDecoder decoder;

	private short port;

	public NetServer(short port) {
		this.port = port;
	}

	public void start() {
		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		boot.group(parentGroup, childGroup);
		boot.channel(NioServerSocketChannel.class);
		boot.localAddress(port);
		boot.childHandler(new ChannelInitializer<SocketChannel>() {
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(decoder.getClass().newInstance());
				channel.pipeline().addLast(handler.getClass().newInstance());
			}
		});
		boot.option(ChannelOption.SO_BACKLOG, 128);
		boot.childOption(ChannelOption.SO_KEEPALIVE, true);
		try {
			boot.bind().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDecoder(ByteToMessageDecoder decoder) {
		this.decoder = decoder;
	}

	public void setHandler(ServerHandler handler) {
		this.handler = handler;
	}


}
