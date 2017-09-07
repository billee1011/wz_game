package server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import proto.Common;
import protocol.c2s.RequestCode;
import server.LoginServer;
import util.Pair;


/**
 * Created by think on 2017/3/21.
 */
public class LoginServerHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LoginServerHandler.class);

	@Override
	protected void registerAction() {
		registerAction(RequestCode.LOGIN_REMOVE_SERVER.getValue(), this::removeServer, Common.PBInt32.getDefaultInstance());
	}
	
	/** 后台关闭服务器 */
	private void removeServer(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		if(packet.getPlayerId() != 0){
			return;
		}
		Common.PBInt32 endTime = message.get();
		LoginServer.getInst().beginStop(endTime.getValue());
	}


	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
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
		return AppId.LOGIN;
	}

	@Override
	protected NetClient getCenterClient() {
		return LoginServer.getInst().getClient();
	}
}
