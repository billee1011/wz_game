package server;

import actor.Actor;
import actor.IActor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import net.NetClient;
import net.ProtoCreator;
import net.codec.GameMessageDecoder;
import net.handler.ClientHandler;
import net.message.CenterModule;
import net.request.RequestMessage;
import timer.ActTimer;
import util.WzProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by WZ on 2016/8/25.
 */
public abstract class ServerBase {
	protected WzProperties props = null;

	protected NetClient client;

	protected ActTimer serverTimer = null;

	protected IActor actor = null;

	public boolean start() {
		serverTimer = new ActTimer(getServerType().name());
		serverTimer.start();
		actor = new Actor("haha");
		actor.start();
		if (getServerType() != EServerType.CENTER) {
			if (false == initServerConf()) {
				return false;
			}
			client = new NetClient(props.getString("center.server.host", "")
					, props.getShort("center.server.port", (short) 11111));
			initNetClientHandler();
			try {
				client.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			startRegister();
		}
		startAfter();
		return true;
	}

	public abstract void initNetClientHandler();

	public void startRegister() {
		if (getServerType() == EServerType.CENTER) {
			return;
		}
		serverTimer.register(5000, 5000, () -> {
			RequestMessage message = RequestMessage.createRequestMessage(getServerType().getValue() , CenterModule.REGISTER_SERVER.getValue()
					, (byte) 0, EServerType.CENTER.getValue(), ProtoCreator.createRegisterServer(getServerType().getValue()));
			client.sendRequest(message);
		}, actor, "hahaha");

	}

	public NetClient getCenterClient(){
		return client;
	}


	public boolean initServerConf() {
		System.out.println(ServerBase.class.getClassLoader().getResource("server.properties").getPath().toString());
		InputStream in = null;
		try {
			in = new FileInputStream(ServerBase.class.getClassLoader().getResource("server.properties").getPath().toString());
		} catch (Exception e) {

		}
		props = new WzProperties(in);
		return props != null;
	}


	public abstract void startAfter();

	public abstract boolean shutDown();

	public boolean connectToCenter() {
		return true;
	}

	public abstract EServerType getServerType();
}
