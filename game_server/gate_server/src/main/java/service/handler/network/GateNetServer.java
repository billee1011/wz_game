package service.handler.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import network.AbstractHandlers;
import network.NetServer;
import packet.CocoPacket;
import service.handler.GateServerHandler;

/**
 * Created by Administrator on 2017/2/7.
 */
public class GateNetServer {
	private ServerBootstrap boot = new ServerBootstrap();

	private AbstractHandlers handler;

	private ByteToMessageDecoder decoder;

	private MessageToByteEncoder<CocoPacket> encoder;

	private short port;

	public GateNetServer(int port) {
		this.port = (short) port;
	}

	public void start() {
		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		boot.group(parentGroup, childGroup);
		boot.localAddress(port);
		boot.channel(NioServerSocketChannel.class);
		boot.childHandler(new ChannelInitializer<SocketChannel>() {
			protected void initChannel(SocketChannel channel) throws Exception {
//				channel.pipeline().addLast(new IdleStateHandler(10, 0, 0));
				if (decoder != null) {
					channel.pipeline().addLast(decoder.getClass().newInstance());
				}
				if (encoder != null) {
					channel.pipeline().addLast(encoder.getClass().newInstance());
				}
				channel.pipeline().addLast(new GateNetHandler(new GateServerHandler()));
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

	public int getPort() {
		return this.port;
	}

	public void setDecoder(ByteToMessageDecoder decoder) {
		this.decoder = decoder;
	}

	public void setEncoder(MessageToByteEncoder<CocoPacket> encoder) {
		this.encoder = encoder;
	}

	public void setHandler(AbstractHandlers handler) {
		this.handler = handler;
	}
}
