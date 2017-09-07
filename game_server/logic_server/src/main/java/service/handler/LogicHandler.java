package service.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.bean.CoupleRoom;
import config.provider.ConfNiuProvider;
import config.provider.PersonalConfRoomProvider;
import define.AppId;
import define.DealZimo;
import io.netty.channel.ChannelHandlerContext;
import logic.AbstractDesk;
import logic.Desk;
import logic.DeskMgr;
import logic.debug.ArrayPai;
import logic.debug.CheckFanType;
import logic.define.BonusType;
import logic.define.GameType;
import logic.majiong.CoupleMJDesk;
import logic.majiong.MJDesk;
import logic.majiong.PlayerInfo;
import logic.majiong.SCMJDesk;
import logic.majiong.XueNiuDesk;
import logic.majiong.XuezhanDesk;
import logic.majiong.define.Gender;
import logic.poker.PokerDesk;
import logic.poker.cpddz.CoupleDdzDesk;
import logic.poker.ddz.DdzDesk;
import logic.poker.lzddz.LzDdzDesk;
import logic.poker.niuniu.NiuNiuDesk;
import logic.poker.niuniu.zhuang.ClassZhuangNiuDesk;
import logic.poker.niuniu.zhuang.GrabZhuangNiuDesk;
import logic.poker.niuniu.zhuang.ZhuangNiuDesk;
import logic.poker.zjh.ZjhDesk;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import proto.Common;
import proto.CoupleMajiang;
import proto.Lobby;
import proto.Login;
import proto.creator.CommonCreator;
import proto.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import service.LogicApp;
import sun.misc.Request;
import util.LogUtil;
import util.Pair;

/**
 * Created by Administrator on 2017/2/7.
 */
public class LogicHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LogicHandler.class);

	@Override
	protected void registerAction() {
		registerAction(RequestCode.LOGIC_PLAYER_LOGIN.getValue(), this::actionPlayerLogin, Login.PBLoginReq.getDefaultInstance());
	}

	private void actionPlayerLogin(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Login.PBLoginReq req = message.get();

		int userId = req.getUserId();
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			getCenterClient().sendRequest(packet);
		} else {
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
						LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
					} catch (InvalidProtocolBufferException e) {
						logger.error("exception; {}", e);
					}
					handler.doAction(client, packet, new MessageHolder<>(message));
				}
			}
		}
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
