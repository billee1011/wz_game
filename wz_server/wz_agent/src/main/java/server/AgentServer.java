package server;


import net.NetServer;
import net.ProtoCreator;
import net.codec.GameMessageDecoder;
import net.handler.ServerHandler;
import net.request.RequestMessage;
import server.handler.AgentClientHandler;

/**
 * Created by WZ on 2016/8/26.
 */
public class AgentServer extends ServerBase {
	private static AgentServer instance = new AgentServer();

	private NetServer server = null;

	private AgentServer() {

	}

	public static AgentServer getInst() {
		return instance;
	}


	@Override
	public void startAfter() {
		serverTimer.register(10000, 10000, () -> {
			RequestMessage messasge = RequestMessage.createRequestMessage(getServerType().getValue()
					, (byte) 1, (byte) 1, EServerType.ENTITY.getValue(), ProtoCreator.createDispatchMsg());
			client.sendRequest(messasge);
		}, actor, "hahaha");

		server = new NetServer(props.getShort("agent.server.port", (short) 15001));
		server.setHandler(new ServerHandler());
		server.setDecoder(new GameMessageDecoder());
		server.start();
	}

	@Override
	public boolean shutDown() {
		return false;
	}

	@Override
	public void initNetClientHandler() {
		client.setDecoder(new GameMessageDecoder());
		client.setHandler(new AgentClientHandler());
	}

	@Override
	public EServerType getServerType() {
		return EServerType.AGENT;
	}

	public static void main(String[] args) {
		AgentServer.getInst().start();
	}
}
