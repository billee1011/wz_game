package service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import util.Pair;

/**
 * Created by think on 2017/3/21.
 */
public class LogHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LogHandler.class);

	private static int packetCount = 0;

	@Override
	protected void registerAction() {

	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		packetCount++;
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() == getAppId()) {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				logger.debug(" the mssage hand is null and the req code is {}", reqCode);
			} else {
				IActionHandler handler = messageAndHandler.getRight();
				MessageLite protoType = messageAndHandler.getLeft();
				if (handler != null) {
					MessageLite message = null;
					try {
						message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
					} catch (InvalidProtocolBufferException e) {
						logger.debug("exception; {}", e);
					}
					handler.doAction(client, packet, new MessageHolder<>(message));
				}
			}
		}
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOG;
	}

	@Override
	protected NetClient getCenterClient() {
		return null;
	}
}
