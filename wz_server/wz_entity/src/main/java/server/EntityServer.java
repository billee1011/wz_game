package server;

import net.ProtoCreator;
import net.codec.GameMessageDecoder;
import net.request.RequestMessage;
import net.response.ResponseMessage;
import server.handler.EntityClientHandler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by WZ on 2016/8/25.
 */
public class EntityServer extends ServerBase {
	private static EntityServer instance = new EntityServer();

	private EntityServer() {
	}

	public static EntityServer getInst() {
		return instance;
	}

	public void startAfter() {

	}

	@Override
	public void initNetClientHandler() {
		client.setHandler(new EntityClientHandler());
		client.setDecoder(new GameMessageDecoder());
	}

	public boolean shutDown() {
		return false;
	}

	public EServerType getServerType() {
		return EServerType.ENTITY;
	}

	public static void main(String[] args) {
		EntityServer.getInst().start();
	}
}
