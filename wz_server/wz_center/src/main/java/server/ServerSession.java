package server;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by WZ on 2016/8/26.
 */
public class ServerSession {
	private ChannelHandlerContext session;
	private int serverId;
	private String serverAddress;
	private short port;
	private EServerType serverType;


	public ServerSession(ChannelHandlerContext session) {
		this.session = session;
	}

	public ChannelHandlerContext getSession() {
		return session;
	}

	public void setSession(ChannelHandlerContext session) {
		this.session = session;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public EServerType getServerType() {
		return serverType;
	}

	public void setServerType(EServerType serverType) {
		this.serverType = serverType;
	}

	public short getPort() {
		return port;
	}

	public void setPort(short port) {
		this.port = port;
	}
}
