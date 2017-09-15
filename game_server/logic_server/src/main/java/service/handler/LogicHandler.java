package service.handler;

import base.EntityCreator;
import chr.PlayerLoader;
import chr.PlayerManager;
import chr.PlayerSaver;
import chr.RyCharacter;
import database.DataQueryResult;
import db.CharData;
import db.DataManager;
import manager.EquipManager;
import manager.FormationManager;
import manager.IMessageHandler;
import network.MessageHolder;
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
import proto.Login;
import proto.creator.CommonCreator;
import protobuf.LoginPbCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.MapObject;
import util.LogUtil;
import util.Pair;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/7.
 */
public class LogicHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LogicHandler.class);


	private Map<Integer, IMessageHandler> messageHandlerMap;

	@Override
	protected void registerAction() {
		messageHandlerMap = new HashMap<>();
		registerAction(RequestCode.LOGIC_PLAYER_LOGIN.getValue(), this::actionPlayerLogin, Login.PBLoginReq.getDefaultInstance());
		registerAction(RequestCode.ACCOUNT_TEST.getValue(), this::actionTest, Common.PBStringList.getDefaultInstance());
		registerAction(RequestCode.FORMATION_EQUIP.getValue(), null, Common.PBStringList.getDefaultInstance(), FormationManager.getInst()::formationEquip);
		registerAction(RequestCode.EQUIP_STRENGTHEN.getValue(), null, Common.PBInt64.getDefaultInstance(), EquipManager.getInst()::strengthenEquip);
	}

	public void registerAction(int action, IActionHandler handler, MessageLite message, IMessageHandler msgHandler) {
		super.registerAction(action, handler, message);
		messageHandlerMap.put(action, msgHandler);
	}

	private void actionTest(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		RyCharacter ch = PlayerManager.getInst().getEntity(packet.getPlayerId());
		if (ch == null) {
			logger.warn(" ch is null and the player id is {}", packet.getPlayerId());
			return;
		}
		ch.write(ResponseCode.ACCOUNT_TEST_REPLY, CommonCreator.stringList("hello", " moto ", "is", "my", "life"));
	}

	private void actionPlayerLogin(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Login.PBLoginReq req = message.get();
		int userId = req.getUserId();
		Map<String, Object> where = new HashMap<>();
		where.put("user_id", userId);
		MapObject player = DataQueryResult.loadSingleResult("player", where);
		long playerId = 0;
		if (player == null) {
			RyCharacter newChar = EntityCreator.createChar("user_id", userId);                                                //插入一些必须的数据吧
			try {
				PlayerSaver.insertPlayer(newChar);
				playerId = newChar.getEntityId();
			} catch (SQLException e) {
				throw new RuntimeException("insert player failed");
			}
		} else {
			playerId = player.getLong("player_id");
		}
		DataManager.getInst().loadChar(playerId, (e, f) -> {
			if (e == null || e instanceof Exception) {
				logger.error(f);
				return;
			}
			CharData data = (CharData) e;
			RyCharacter ch = PlayerLoader.loadFromCharData(data);
			onPlayerLoginSuccess(client, ch);
		});
	}


	private void onPlayerLoginSuccess(ChannelHandlerContext ctx, RyCharacter ch) {
		ch.checkInit();
		PlayerManager.getInst().addEntity(ch);
		ch.setIoSession(ctx);
		ch.write(RequestCode.ACCOUNT_LOGIN_RESULT, LoginPbCreator.loginSucc(ch));
		logger.info("player login success ");
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			getCenterClient().sendRequest(packet);
		} else {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				logger.debug(" the message hand is null and the req code is {}", reqCode);
			} else {
				IActionHandler handler = messageAndHandler.getRight();
				MessageLite protoType = messageAndHandler.getLeft();
				if (handler != null) {
					MessageLite message = getMessageLite(packet, reqCode, protoType);
					handler.doAction(client, packet, new MessageHolder<>(message));
				} else {
					IMessageHandler msgHandler = messageHandlerMap.get(packet.getReqId());
					if (msgHandler != null) {
						RyCharacter ch = PlayerManager.getInst().getEntity(packet.getPlayerId());
						MessageLite message = getMessageLite(packet, reqCode, protoType);
						msgHandler.actionHandle(ch, new MessageHolder<>(message));
					}
				}
			}
		}
	}

	private MessageLite getMessageLite(CocoPacket packet, RequestCode reqCode, MessageLite protoType) {
		MessageLite message = null;
		try {
			message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
			LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
		} catch (InvalidProtocolBufferException e) {
			logger.error("exception; ", e);
		}
		return message;
	}

	private void sendLogicDeskIsRemove(int playerId) {
		getCenterClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_DESK_IS_REMOVE, null, playerId));
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOGIC;
	}

	@Override
	protected NetClient getCenterClient() {
		return LogicApp.getInst().getClient();
	}
}
