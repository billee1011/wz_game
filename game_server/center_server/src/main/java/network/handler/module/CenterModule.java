package network.handler.module;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import proto.Common;
import proto.Login;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import service.CenterServer;
import util.NettyUtil;

public class CenterModule implements IModuleMessageHandler {

	private static final Logger logger = LoggerFactory.getLogger(CenterModule.class);

	private Map<String, LoginInfo> tokenMap = new HashMap<>();

	private static CenterModule ins = new CenterModule();

	public static CenterModule getIns() {
		return ins;
	}

	private CenterModule() {
	}

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.CENTER_REGISTER_SERVER.getValue(), this::actionRegisterServer, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_DISPATCH_GATE.getValue(), this::actionDispatchGate, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_LOGIN.getValue(), this::actionValidLogin, Login.PBLoginReq.getDefaultInstance());

		handler.registerAction(RequestCode.CENTER_SERVER_PING.getValue(), this::actionServerPing);

	}


	private void actionServerPing(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		ServerSession session = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
		if (session == null) {
			logger.info(" server ping  and session is null");
			return;
		}
	}


	private void actionRegisterServer(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBStringList req = message.get();
		ServerSession session = NettyUtil.getAttribute(ioSession, ServerSession.KEY);

		int paramAppId = Integer.valueOf(req.getList(0));
		AppId appId = AppId.getByValue(paramAppId);
		if (appId == null) {
			logger.warn(" the app id is null and the id is {}", paramAppId);
			return;
		}
		session.setAppId(appId);
		long centerStartTime = CenterServer.getInst().getStartTime();
		ServerManager.getInst().registerServer(centerStartTime, session, req.getListList());
		int dynamicId = session.getServerId();
		CocoPacket packet = new CocoPacket(RequestCode.CENTER_REGISTER_SERVER.getValue(), CommonCreator.stringList(dynamicId + "", centerStartTime + ""));
		packet.setSeqId(srcPacket.getSeqId());
		ioSession.writeAndFlush(packet);
	}

	private void actionDispatchGate(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		ServerSession session = ServerManager.getInst().getMinLoadSession(AppId.GATE);
		if (session == null) {
			logger.warn("没有gate可以分配");
			return;
		}
		String gateIp = session.getRemoteAddress();
		int gatePort = session.getLocalPort();
		Common.PBStringList request = message.get();
		String[] strs = request.getList(0).split(":");
		int userId = Integer.parseInt(strs[0]);
		LoginInfo info = new LoginInfo();
		info.userId = userId;
		tokenMap.put(strs[1], info);
		logger.info(" the gate ip is {} and the port is {}", gateIp, gatePort);
		CocoPacket packet = new CocoPacket(RequestCode.CENTER_DISPATCH_GATE, CommonCreator.stringList(gateIp + ":" + gatePort));
		packet.setSeqId(srcPacket.getSeqId());
		ioSession.writeAndFlush(packet);
	}

	private void actionValidLogin(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		logger.info("player login the game ");
		Login.PBLoginReq request = message.get();
		if (request == null) {
			return;
		}
		String token = request.getToken();
		LoginInfo info = tokenMap.get(token);
		if (info == null) {
			logger.warn("the token is not success");
			return;
		}
		//登陆成功的就不是这边处理的问题了， 让logic去登陆，如果有角色直接返回， 没有角创建一个角色 ==
		ServerSession session = ServerManager.getInst().getServerSession(AppId.LOGIC, 1);
		session.sendRequest(new CocoPacket(RequestCode.LOGIC_PLAYER_LOGIN, request));
	}


	private class LoginInfo {
		public int userId;
		public String province;
		public String city;
		public String channel;
		public String device;
		public int packageId;
		public String ip;
		public String machine_id;
		public String game_version;
		public String app_version;
		public String platform_id;

	}
}
