package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import packet.CocoPacket;
import protocol.c2s.RequestCode;
import service.handler.GateServerHandler;

public class ShowInfo {
	private static Logger logger = LoggerFactory.getLogger(GateServerHandler.class);

	public static void showPacketToLog(int flag, CocoPacket packet, MessageLite messageLite) {
		if (null == packet) {
			logger.warn("packet is null!");
			return;
		}
		LogUtil.msgLogger.info("Svr appId:{}", packet.getReqCode().getSendTo());
		switch (flag) {
			case 1:
				LogUtil.msgLogger.info("playerId:{} reqId:{} seqId:{} message:{}", packet.getPlayerId(), packet.getReqId(), packet.getSeqId(),messageLite);
				break;

			case 2:
				if (null == messageLite) {
					LogUtil.msgLogger.info("messageLite is null!");
					return;
				}
				LogUtil.msgLogger.info("playerId:{} reqId:{} seqId:{} message:{}", packet.getPlayerId(), packet.getReqId(), packet.getSeqId(), messageLite);
				break;
		}
	}

	public static void showIsNullToLog(String str, RequestCode reqCode) {
		LogUtil.msgLogger.info(" the req code is null in {} {}", str, reqCode);
	}
}
