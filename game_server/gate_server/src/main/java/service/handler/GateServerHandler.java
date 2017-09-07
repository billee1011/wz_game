package service.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.GateApp;
import service.handler.agent.CocoAgent;
import util.LogUtil;
import util.NettyUtil;
import util.Pair;
import util.ShowInfo;

/**
 * Created by Administrator on 2017/2/6.
 */
public class GateServerHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(GateServerHandler.class);

	@Override
	protected void registerAction() {
		registerAction(RequestCode.PING.getValue(), this::actionPing);
	}

	private void actionPing(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CocoAgent agent = NettyUtil.getAttribute(client, "agent");
		if (agent != null) {
			agent.writeMessage(ResponseCode.PONG.getValue(), null);
		}
	}


	@Override
	protected AppId getAppId() {
		return AppId.GATE;
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			ShowInfo.showPacketToLog(1, packet, null);
			getCenterClient().sendRequest(packet);
		} else {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				if(reqCode != RequestCode.PING) {
					ShowInfo.showIsNullToLog("Gate", reqCode);
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
					ShowInfo.showPacketToLog(2, packet, message);
				}
			}
		}
	}

	@Override
	protected NetClient getCenterClient() {
		return GateApp.getInst().getClient();
	}
}
