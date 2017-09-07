package network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.NetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;

/**
 * Created by Administrator on 2017/2/4.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
	private NetClient client;
	private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);

	public ClientHandler(NetClient client) {
		this.client = client;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		client.sessionCreated(ctx);
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info(" channel in active ");
		client.sessionInActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof CocoPacket == false) {
			logger.debug(" the message is not  coco packet ");
			return;
		}
		client.messageReceived(ctx, (CocoPacket) msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(" error : {}", cause);
	}
}
