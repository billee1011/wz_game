package server.handler;

import client.LoginClient;
import define.LoginCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import net.handler.ServerHandler;
import proto.Server;
import server.handler.action.IMessageAction;
import server.handler.action.LoginPasswordAction;
import util.NettyUtil;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by WZ on 2016/8/28.
 */
public class LoginServerHandler extends ServerHandler {
	private Map<LoginCode, IMessageAction> actionMap = new HashMap<>();

	public LoginServerHandler() {
		reset();
	}

	private void reset() {
		actionMap.clear();
		actionMap.put(LoginCode.LOGIN_PASSWORD, new LoginPasswordAction());
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LoginClient client = new LoginClient(ctx);
		NettyUtil.setAttribute(ctx, LoginClient.CLIENT_KEY, client);
		ctx.pipeline().addLast(new IdleStateHandler(30, 30, 30));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		LoginClient client = NettyUtil.getAttribute(ctx, LoginClient.CLIENT_KEY);
		ByteBuf buffer = (ByteBuf) msg;
		byte moduleId = buffer.readByte();
		byte actionId = buffer.readByte();
		int length = buffer.readableBytes();
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes, 0, length);
		Server.AccountLogin request = Server.AccountLogin.parseFrom(bytes);
		IMessageAction action = actionMap.get(LoginCode.getByValue(moduleId));
		if (action != null) {
			action.handler(client, request);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}
}
