package service.handler;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import acotr.LogActorManager;
import data.logdata.ILogInfo;
import data.logdata.LogAccount;
import data.logdata.LogBank;
import data.logdata.LogMoney;
import data.logdata.LogOnline;
import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.Log;
import protocol.c2s.RequestCode;
import service.LogServer;
import util.Pair;

/**
 * Created by think on 2017/3/21.
 */
public class LogHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LogHandler.class);

	private static int packetCount = 0;

	@Override
	protected void registerAction() {
		registerAction(RequestCode.LOG_ACCOUNT.getValue(), this::actionLogAccount, Log.PBLogAccount.getDefaultInstance());
		registerAction(RequestCode.LOG_MONEY.getValue(), this::actionLogMoney, Log.PBLogMoney.getDefaultInstance());
		registerAction(RequestCode.LOG_ONLINE.getValue(), this::actionLogOnline, Log.PBLogonline.getDefaultInstance());
		registerAction(RequestCode.LOG_BANK.getValue(), this::actionLogBank, Log.PBLogBank.getDefaultInstance());
		registerAction(RequestCode.LOG_REMOVE_SERVER.getValue(), this::removeServer, Common.PBInt32.getDefaultInstance());
	}
	
	/** 后台关闭服务器 */
	private void removeServer(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		if(packet.getPlayerId() != 0){
			return;
		}
		Common.PBInt32 endTime = message.get();
		LogServer.getInst().beginStop(endTime.getValue());
	}

	private void actionLogBank(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		ILogInfo logInfo = new LogBank();
		logInfo.read(message);
		saveLogInfo(logInfo);
	}

	private void saveLogInfo(ILogInfo logInfo) {
		LogActorManager.getSaveActor(packetCount).put(() -> {
			try {
				logInfo.save();
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			return null;
		});
	}

	private void actionLogOnline(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		ILogInfo logInfo = new LogOnline();
		logInfo.read(message);
		saveLogInfo(logInfo);
	}

	private void actionLogMoney(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		ILogInfo logInfo = new LogMoney();
		logInfo.read(message);
		saveLogInfo(logInfo);
	}

	private void actionLogAccount(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		ILogInfo logInfo = new LogAccount();
		logInfo.read(message);
		saveLogInfo(logInfo);
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
