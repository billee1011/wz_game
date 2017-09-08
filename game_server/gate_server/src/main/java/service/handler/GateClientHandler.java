package service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import proto.Common;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.GateApp;
import service.handler.agent.AgentManager;
import util.LogUtil;
import util.Pair;
import util.ShowInfo;

/**
 * Created by Administrator on 2017/2/7.
 */
public class GateClientHandler extends AbstractHandlers {
	private static final Logger logger = LoggerFactory.getLogger(GateClientHandler.class);

	@Override
	protected void registerAction() {
		registerAction(RequestCode.GATE_KICK_PLAYER.getValue(), this::actionKickOutPlayer);
		registerAction(RequestCode.GATE_KICK_ALL_PLAYER.getValue(), this::actionKickAllPlayer);
	}

	private void actionKickAllPlayer(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		AgentManager.getInst().closeAll();
		System.exit(0);
		//直接关了吧 正常退出
	}

	private void actionKickOutPlayer(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		AgentManager.getInst().closeAgent(packet.getPlayerId());
	}

	@Override
	protected AppId getAppId() {
		return AppId.GATE;
	}

	@Override
	protected NetClient getCenterClient() {
		return GateApp.getInst().getClient();
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		ShowInfo.showPacketToLog(1, packet, null);
		if (reqCode == null) {
			AgentManager.getInst().writeMessage(packet);
		} else {
			if (reqCode.getSendTo() != getAppId()) {
				getCenterClient().sendRequest(packet);
			} else {
				Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
				if (messageAndHandler == null) {
					if (ResponseCode.ACCOUNT_LOGIN_OTHER_WHERE.getValue() == packet.getReqId()) {
						AgentManager.getInst().kickAgent(packet.getPlayerId());
					}else{
						AgentManager.getInst().writeMessage(packet);
					}
				} else {
					IActionHandler handler = messageAndHandler.getRight();
					MessageLite protoType = messageAndHandler.getLeft();
					if (handler != null) {
						MessageLite message = null;
						try {
							message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
							LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
						} catch (InvalidProtocolBufferException e) {
							logger.error("exception; {}", e);
						}
						handler.doAction(client, packet, new MessageHolder<>(message));
					}
				}
			}
		}
	}
}
