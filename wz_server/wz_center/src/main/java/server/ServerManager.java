package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.ITargetServerMessage;
import net.proto.Server;
import net.request.RequestMessage;
import net.response.ResponseMessage;
import org.omg.CORBA.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WZ on 2016/8/26.
 */
public class ServerManager {
	Logger logger = LoggerFactory.getLogger(ServerManager.class);
	private static ServerManager instance = new ServerManager();

	private Map<EServerType, Map<Integer, ServerSession>> servers = new HashMap<>();

	private ServerManager() {

	}

	public static ServerManager getInst() {
		return instance;
	}

	public void handlerServerDispatch(ITargetServerMessage message) {
		EServerType type = EServerType.getByValue(message.getServerId());
		if (type == null) {
			logger.error("the server type is null ");
			return;
		}
		Map<Integer, ServerSession> subServers = servers.get(type);
		if (subServers == null) {
			logger.error(" the sub server is null and the type is {}", type);
			return;
		}
		subServers.values().forEach(e -> {
			e.getSession().writeAndFlush(message.getBuffer());
		});
	}


	public void registerServerSession(ChannelHandlerContext session, Server.RegisterServer message) {
		EServerType type = EServerType.getByValue(message.getServerId());
		System.out.println(session.channel().remoteAddress());
		Map<Integer, ServerSession> sessions = servers.get(type);
		if (sessions == null) {
			sessions = new HashMap<>();
			sessions.put(1, new ServerSession(session));
			servers.put(type, sessions);
		}
	}
}
