package service.handler.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import service.codec.GateCodec;
import service.handler.GateNetHandler;

/**
 * Created by Administrator on 2017/2/7.
 */
public class GateNetServer {
    private ServerBootstrap boot = new ServerBootstrap();


    private ByteToMessageDecoder decoder;


    private short port;

    public GateNetServer(int port) {
        this.port = (short) port;
    }

    public void start() {
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup();
        boot.group(parentGroup, childGroup);
        boot.channel(NioServerSocketChannel.class);
        boot.localAddress(port);
        boot.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(new IdleStateHandler(45, 45, 45));
                channel.pipeline().addLast(new GateCodec());
                channel.pipeline().addLast(new GateNetHandler());
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

//	public void setEncoder(MessageToByteEncoder<CocoPacket> encoder) {
//		this.encoder = encoder;
//	}
//
//	public void setHandler(AbstractHandlers handler) {
//		this.handler = handler;
//	}
}
