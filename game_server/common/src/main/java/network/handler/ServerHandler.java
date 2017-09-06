package network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.AbstractHandlers;
import network.NetServer;
import network.ServerManager;
import network.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import util.NettyUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);
	private static final int REQUEST_FLAG = 0x10000000;
	protected AbstractHandlers handlers;

	public ServerHandler(AbstractHandlers handlers) {
		this.handlers = handlers;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ServerSession session = new ServerSession(ctx);
		NettyUtil.setAttribute(ctx, ServerSession.KEY, session);
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ServerSession serverSession = NettyUtil.getAttribute(ctx, ServerSession.KEY);
		if (serverSession != null) {
			logger.info(" the session close and the session id is {} id {}", serverSession.getAppId(),serverSession.getServerId());
			handlers.handleSessionInActive(serverSession);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof CocoPacket == false) {
			logger.warn(" the packet is not instance of coco packet ");
			return;
		}
		CocoPacket packet = (CocoPacket) msg;
		if (isRequestMessage(packet)) {
			handlers.handPacket(ctx, (CocoPacket) msg);
		} else {
			//response =====================================是这个样子的吗, 我了个擦
		}
	}

	protected boolean isRequestMessage(CocoPacket packet) {
		if (packet.getReqId() > REQUEST_FLAG) {
			packet.setReqId(packet.getReqId() - REQUEST_FLAG);
			return true;
		}
		return false;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();                    //打印一下异常吧, 暂时
	}
}
