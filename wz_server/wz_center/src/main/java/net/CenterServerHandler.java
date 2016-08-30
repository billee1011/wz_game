package net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.handler.ServerHandler;
import net.message.CenterModule;
import net.request.RequestMessage;
import net.response.ResponseMessage;
import server.EServerType;
import server.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WZ on 2016/8/25.
 */
public class CenterServerHandler extends ServerHandler {

	private interface IRequestHandler {
		public void handler(ChannelHandlerContext session, RequestMessage message);
	}

	private Map<Byte, IRequestHandler> handlerMap = new HashMap<>();

	public CenterServerHandler() {
		resetHandler();
	}

	public void resetHandler() {
		handlerMap.clear();
		handlerMap.put(CenterModule.REGISTER_SERVER.getValue(), CenterMsgHandler::handlerRegisterServer);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channel active");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof RequestMessage) {
			RequestMessage request = (RequestMessage) msg;
			if (request.getServerId() == EServerType.CENTER.getValue()) {
				handlerMap.get(request.getModuleId()).handler(ctx, request);
			} else {
				ServerManager.getInst().handlerServerDispatch(request);
			}
		}
		if (msg instanceof ResponseMessage) {
			ResponseMessage message = (ResponseMessage) msg;
			if (message.getServerId() == EServerType.CENTER.getValue()) {
				//回复消息给center一般来说center不会给任何人发送request;
			} else {
				ServerManager.getInst().handlerServerDispatch(message);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

	}
}
