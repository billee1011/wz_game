package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import actor.ICallback;
import define.AppId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import network.codec.MessageDecoder;
import network.codec.MessageEncoder;
import network.handler.ClientHandler;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import util.MiscUtil;

/**
 * Created by Administrator on 2017/2/4.
 */
public class NetClient {
	private static Logger logger = LoggerFactory.getLogger(NetClient.class);
	private static final int REQUEST_FLAG = 0x10000000;
	/** 多久后重连 */
	private static final int RECONNECT_SECOND = 5;
	private String host;
	private int port;
	private List<String> registParam;
	private ChannelHandlerContext ioSession;
	private Bootstrap boot;
	private Map<Integer, TaskNode> nodeMap = new HashMap<>();
	private AppId appId;
	//request handler  =============================================
	private int appDynamicId;
	private long connectServerStartTime;
	private AbstractHandlers handlers = null;
	private ICallback onSessionCloseCallBack = null;

	public NetClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void startConnect(ICallback callback) {
		boot = new Bootstrap();
		boot.group(new NioEventLoopGroup());
		boot.channel(NioSocketChannel.class);
		boot.option(ChannelOption.SO_KEEPALIVE, true);
		boot.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(new MessageDecoder());
				socketChannel.pipeline().addLast(new MessageEncoder());
				socketChannel.pipeline().addLast(new ClientHandler(NetClient.this));
			}
		});
		connectToServer(callback);
	}

	public void sessionCreated(ChannelHandlerContext ctx) {
		this.ioSession = ctx;
		
		List<String> param = new ArrayList<String>();
		param.add(appId.getId()+"");
		param.add(appDynamicId+"");
		param.add(connectServerStartTime+"");
		if(registParam != null){
			param.addAll(registParam);
		}
		
		sendRequest(new CocoPacket(RequestCode.CENTER_REGISTER_SERVER
				, CommonCreator.createPBStringList(param)), e -> {
			if (e instanceof CocoPacket == false) {
				return;
			}
			CocoPacket packet = (CocoPacket) e;
			try {
				Common.PBStringList req = Common.PBStringList.parseFrom(packet.getBytes());
				appDynamicId = Integer.valueOf(req.getList(0));
				connectServerStartTime = Long.valueOf(req.getList(1));
				logger.info("receive the callback message and the result is {} , connectServerStartTime {}", appDynamicId ,MiscUtil.getDateStrB(connectServerStartTime));
			} catch (InvalidProtocolBufferException e1) {
				e1.printStackTrace();
			}
		});
	}


	public void sessionInActive(ChannelHandlerContext ctx) {
		if(onSessionCloseCallBack != null){
			onSessionCloseCallBack.onResult(null);
		}
		// don't need reconnect is current version
		if(appId == AppId.GATE || appId == AppId.LOGIN || appId == AppId.LOG){
			logger.error("{},serverId={}与center连接断开,{}秒后准备重连." ,appId,appDynamicId,RECONNECT_SECOND);
			ctx.channel().eventLoop().schedule(() -> reconnect(ctx), RECONNECT_SECOND, TimeUnit.SECONDS);
		}else{
			logger.error("{},serverid={}与center连接断开,自动结束进程" ,appId,appDynamicId);
			System.exit(0);
		}
	}

	private void reconnect(ChannelHandlerContext ctx) {
		connectToServer(null);
	}

	public void messageReceived(ChannelHandlerContext ctx, CocoPacket packet) {
		int seqId = packet.getSeqId();
		if (isRequestMessage(packet)) {
			handlers.handPacket(ctx, packet);
		} else {
			TaskNode node = nodeMap.get(seqId);
			if (node != null) {
				node.getCallback().onResult(packet);
			}
		}
	}

	private boolean isRequestMessage(CocoPacket packet) {
		if (packet.getReqId() > REQUEST_FLAG) {
			packet.setReqId(packet.getReqId() - REQUEST_FLAG);
			return true;
		}
		return false;
	}

	public void sendRequest(CocoPacket packet) {
		sendRequest(packet, null);
	}

	public void sendRequest(CocoPacket packet, ICallback callback) {
		if (callback != null) {
			TaskNode node = new TaskNode(ioSession, packet, callback);
			nodeMap.put(node.getSeqId(), node);
		}
		packet.setReqId(packet.getReqId() + REQUEST_FLAG);
		ioSession.writeAndFlush(packet);
	}

	//同步发送request
	public void sendRequestSync(CocoPacket packet, ICallback callback) {
		SyncResponse res = new SyncResponse(callback);
		sendRequest(packet, res);
		res.blockUntilResponse();
	}

	public void connectToServer(ICallback callback) {
		ChannelFuture future = boot.connect(host, port);
		future.addListener(e -> {
			if (!e.isSuccess()) {
				logger.info(" connect to the server un success and reconnect {} sec later ",RECONNECT_SECOND);
				future.channel().eventLoop().schedule(() -> connectToServer(callback), RECONNECT_SECOND, TimeUnit.SECONDS);
			} else {
				if (callback != null) {
					callback.onResult(null);
				}
			}
		});
	}

	public void setAppId(AppId appId) {
		this.appId = appId;
	}

	public void setRequestHandlers(AbstractHandlers handlers) {
		this.handlers = handlers;
	}

	public void setIoSession(ChannelHandlerContext ioSession) {
		this.ioSession = ioSession;
	}

	public int getServerId() {
		return appDynamicId;
	}

	public void setRegistParam(List<String> registParam) {
		this.registParam = registParam;
	}

	public void setOnSessionCloseCallBack(ICallback onSessionCloseCallBack) {
		this.onSessionCloseCallBack = onSessionCloseCallBack;
	}
}
