package network.handler;

import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.handler.module.AccountModule;
import network.handler.module.BackendModule;
import network.handler.module.CenterModule;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import service.CenterServer;
import util.LogUtil;
import util.Pair;

/**
 * Created by Administrator on 2017/2/6.
 */
public class CenterMessageHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(CenterMessageHandler.class);


	@Override
	protected void registerAction() {
		new AccountModule().registerModuleHandler(this);
		CenterModule.getIns().registerModuleHandler(this);
		new BackendModule().registerModuleHandler(this);
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			if (reqCode.getSendTo() == AppId.LOGIC) {
				ServerManager.getInst().getServerSession(reqCode.getSendTo(), 1).sendRequest(packet);
			} else {
				ServerManager.getInst().getMinLoadSession(reqCode.getSendTo()).sendRequest(packet);
			}
		} else {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				logger.warn(" the req code is not handler code: {}, id {} ", reqCode, packet.getReqId());
			} else {
				IActionHandler handler = messageAndHandler.getRight();
				MessageLite protoType = messageAndHandler.getLeft();
				if (handler != null) {
					try {
						final MessageLite message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
						LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
						CenterActorManager.getLogicActor(packet.getPlayerId()).put(() -> {
							handler.doAction(client, packet, new MessageHolder<>(message));
							return null;
						});
					} catch (InvalidProtocolBufferException e) {
						logger.debug("exception; {}", e);
					}
				}
			}
		}
	}

	@Override
	protected AppId getAppId() {
		return AppId.CENTER;
	}

	@Override
	protected NetClient getCenterClient() {
		return CenterServer.getInst().getClient();
	}

	@Override
	public void handleSessionInActive(ServerSession session) {
	}
}
