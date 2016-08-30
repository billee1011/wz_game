package net;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import net.proto.Server;
import net.request.RequestMessage;
import server.EServerType;
import server.ServerManager;
import server.ServerSession;

/**
 * Created by WZ on 2016/8/25.
 */
public class CenterMsgHandler {

	public static void handlerRegisterServer(ChannelHandlerContext session, RequestMessage message) {
		Server.RegisterServer msg = null;
		try {
			msg = Server.RegisterServer.parseFrom(message.getMessage());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		ServerManager.getInst().registerServerSession(session, msg);
		System.out.println(" new server register and the server is " + EServerType.getByValue(msg.getServerId()).name());
	}

}
