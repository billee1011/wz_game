package network.handler.module;

import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import common.LogHelper;
import data.MoneySubAction;
import database.DBUtil;
import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import mail.MailEntity;
import network.AbstractHandlers;
import network.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.CommonCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.ASObject;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by think on 2017/4/11.
 */
public class MailModule implements IModuleMessageHandler {

	private static final Logger logger = LoggerFactory.getLogger(MailModule.class);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.MAIL_ALL.getValue(), this::actionGetMailList);
		handler.registerAction(RequestCode.MAIL_DELETE.getValue(), this::actionDeleteMail, Common.PBInt32List.getDefaultInstance());
		handler.registerAction(RequestCode.MAIL_GAIN_MAIL.getValue(), this::actionGainMailReward, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.MAIL_READ_MAIL.getValue(), this::actionReadMail, Common.PBInt32.getDefaultInstance());
	}

	private void actionReadMail(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		MailEntity mail = player.getMailEntity(request.getValue());
		if (mail == null) {
			return;
		}
		Map<String, Object> where = new HashMap<>();
		where.put("player_id", player.getPlayerId());
		where.put("id", request.getValue());
		Map<String, Object> data = new HashMap<>();
		data.put("readed", 1);
		try {
			DBUtil.executeUpdate("mail", where, data);
			mail.setRead(true);
			player.write(ResponseCode.MAIl_READ_SUCC, CommonCreator.createPBInt32(request.getValue()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void actionGainMailReward(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		MailEntity mail = player.getMailEntity(request.getValue());
		if (mail == null) {
			return;
		}
		if (mail.getMoney() <= 0) {
			return;
		}
		if (mail.isGain()){
			return;
		}
		
		Map<String, Object> where = new HashMap<>();
		where.put("player_id", player.getPlayerId());
		where.put("id", request.getValue());
		Map<String, Object> data = new HashMap<>();
		data.put("gain", 1);
		try {
			DBUtil.executeUpdate("mail", where, data);
			long pre_coin = player.getCoin();
			player.updateCoin(mail.getMoney() * 100, true);
			logger.info("金币更新 :玩家 {} 领取邮件， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), mail.getMoney() * 100, player.getCoin());
			int flag = 0;
			if (mail.getMailId() == MailEntity.rank_reward) {
				flag = MoneySubAction.RANK_GAIN.getValue();
			} else if (mail.getMailId() == MailEntity.upgrade) {
				flag = MoneySubAction.UPGRADE_GAIN.getValue();
			}
			if (0 != flag) {
				ServerManager.getInst().getMinLoadSession(AppId.LOG)
						.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
								, LogHelper.logGainMoney(player.getPlayerId(), flag, 0, mail.getMoney() * 100, pre_coin, pre_coin+mail.getMoney() * 100, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice(), 0)));
			}

			mail.setGain(true);
			PlayerSaver.savePlayerBase(player);
			player.write(ResponseCode.MAIl_GAIN_REWARD_SUCC, CommonCreator.createPBPair(request.getValue(), (int) player.getCoin()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void actionDeleteMail(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32List request = message.get();
		List<ASObject> data = new ArrayList<>();
		for (Integer value : request.getValueList()) {
			MailEntity entity = player.getMailEntity(value);
			if (entity == null) {
				return;
			}
			if (!entity.isGain() && entity.getMoney() > 0) {
				return;
			}
		}
		for (Integer value : request.getValueList()) {
			ASObject obj = new ASObject();
			obj.put("player_id", player.getPlayerId());
			obj.put("id", value);
			data.add(obj);
		}
		Object[] dataList = data.toArray();
		
		CenterActorManager.getDbActor(player.getPlayerId()).put(()->{
			try {
				DBUtil.batchDelete("mail", Arrays.asList("player_id", "id"), null, dataList);
			} catch (SQLException e) {
				logger.error(" exception {}", e);
				return null;
			}
			return null;
		});
		player.removeMail(request.getValueList());
		player.write(ResponseCode.MAIL_DELETE, request);
	}

	private void actionGetMailList(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.MAIL_ALL_MAIL, MailCreator.createPBAllMailRes(player.getMailList()));
	}
}
