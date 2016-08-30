package server;

import net.NetServer;
import net.codec.GameMessageDecoder;
import net.handler.ServerHandler;
import server.handler.LoginClientHandler;
import server.handler.LoginServerHandler;
import server.handler.codec.LoginDecoder;

/**
 * Created by WZ on 2016/8/27.
 */
public class LoginServer extends ServerBase {
	private static LoginServer instance = new LoginServer();

	private NetServer server = null;

	private LoginServer() {

	}

	public static LoginServer getInst() {
		return instance;
	}

	@Override
	public void initNetClientHandler() {
		client.setDecoder(new GameMessageDecoder());
		client.setHandler(new LoginClientHandler());
	}

	@Override
	public void startAfter() {
		server = new NetServer(props.getShort("login.server.port"));
		server.setHandler(new LoginServerHandler());
		server.setDecoder(new LoginDecoder());
		server.start();
	}

	@Override
	public boolean shutDown() {
		return false;
	}

	@Override
	public EServerType getServerType() {
		return EServerType.LOGIN;
	}

	public static void main(String[] args) {

	}
}
