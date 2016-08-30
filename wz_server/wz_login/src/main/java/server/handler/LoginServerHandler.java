package server.handler;

import client.LoginClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import net.handler.ServerHandler;
import util.NettyUtil;

/**
 * Created by WZ on 2016/8/28.
 */
public class LoginServerHandler extends ServerHandler {

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

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}
}
