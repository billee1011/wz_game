package network;

import define.AppId;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import packet.CocoPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/2/4.
 */
public class ServerSession {
	public static final int NIU_NIU_LOAD_FACTOR = 1000;
	private static AtomicInteger IDGEN = new AtomicInteger(100000);
	public static final String KEY = "SERVER";
	private ChannelHandlerContext ioSession;
	private String remoteAddress;
	private int remotePort;
	private int localPort;
	private int serverId;
	private AppId appId;
	private AtomicInteger loadFactor = new AtomicInteger();
	private AtomicBoolean stop = new AtomicBoolean();

	public ServerSession(ChannelHandlerContext ctx) {
		this.ioSession = ctx;
		this.serverId = IDGEN.getAndIncrement();
		SocketAddress address = ctx.channel().remoteAddress();
		if (address instanceof InetSocketAddress) {
			this.remoteAddress = ((InetSocketAddress) address).getHostString();
			this.remotePort = ((InetSocketAddress) address).getPort();
		}
	}

	public void sendResponse(CocoPacket packet) {
		ioSession.writeAndFlush(packet);
	}

	public ChannelFuture sendRequest(CocoPacket packet) {
		packet.resetReqIdToRequest();
		return ioSession.writeAndFlush(packet);
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getServerId() {
		return serverId;
	}

	public int getLoadFactor() {
		return loadFactor.get();
	}

	public void addLoadFactor() {
		this.loadFactor.incrementAndGet();
	}

	public void addLoadFactor(int value) {
		this.loadFactor.addAndGet(value);
	}

	public void reduceFactor(int value) {
		addLoadFactor(-value);
	}
	
	public void reduceFactor() {
		this.loadFactor.decrementAndGet();
	}
	
	public void setFactor(int loadFactor) {
		this.loadFactor.set(loadFactor);
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public AppId getAppId() {
		return appId;
	}

	public void setAppId(AppId appId) {
		this.appId = appId;
	}

	public ChannelHandlerContext getIoSession() {
		return ioSession;
	}

	public AtomicBoolean getStop() {
		return stop;
	}
}
