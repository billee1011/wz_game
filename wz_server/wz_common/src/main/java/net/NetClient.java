package net;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.handler.ClientHandler;
import net.request.RequestMessage;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by WZ on 2016/8/25.
 */
public class NetClient {
	private Bootstrap boot = new Bootstrap();

	private String remoteAddress;

	private short port;

	private ClientHandler handler;

	private ByteToMessageDecoder decoder;

	public NetClient(String remote, short port) {
		this.remoteAddress = remote;
		this.port = port;
	}

	public ChannelFuture connect() {
		NioEventLoopGroup group = new NioEventLoopGroup();
		boot.group(group);
		boot.channel(NioSocketChannel.class);
		boot.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				handler = handler.getClass().newInstance();
				decoder = decoder.getClass().newInstance();
				ch.pipeline().addLast(decoder);
				ch.pipeline().addLast(handler);
			}
		});
		boot.remoteAddress(new InetSocketAddress("127.0.0.1", 11111));
		return boot.connect();
	}

	public void setDecoder(ByteToMessageDecoder decoder) {
		this.decoder = decoder;
	}


	public void setHandler(ClientHandler handler) {
		this.handler = handler;
	}

	public void sendRequest(RequestMessage message, IResponseHandler response) {
		this.handler.sendRequest(message, response);
	}

	public void sendRequest(RequestMessage message) {
		handler.sendRequest(message);
	}
}
