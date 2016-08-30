package server;

import net.CenterServerHandler;
import net.NetServer;
import net.codec.GameMessageDecoder;
import net.handler.ServerHandler;

/**
 * Created by WZ on 2016/8/25.
 */
public class CenterServer extends ServerBase {
	private static CenterServer instance = new CenterServer();

	private CenterServer() {
	}

	public static CenterServer getInst() {
		return instance;
	}

	private NetServer server;

	@Override
	public void startRegister() {
		//the main server, don't need to register other
	}

	@Override
	public void initNetClientHandler() {
		// because i am center server so ignore
	}

	@Override
	public void startAfter() {
		server = new NetServer((short) 11111);
		server.setHandler(new CenterServerHandler());
		server.setDecoder(new GameMessageDecoder());
		server.start();
	}

	@Override
	public boolean shutDown() {
		return false;
	}

	@Override
	public EServerType getServerType() {
		return EServerType.CENTER;
	}

	public static void main(String[] args) {
		CenterServer.getInst().start();
	}
}
