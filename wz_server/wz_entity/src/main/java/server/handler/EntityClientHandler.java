package server.handler;

import com.sun.security.ntlm.Client;
import io.netty.channel.ChannelHandlerContext;
import net.handler.ClientHandler;
import server.handler.msg.MsgOneHandler;

/**
 * Created by WZ on 2016/8/26.
 */
public class EntityClientHandler extends ClientHandler{
	@Override
	public void registerMsgHandler() {
		registerModuleHandler( (byte) 1 , new MsgOneHandler());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
}
