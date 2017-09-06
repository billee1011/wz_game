package network.handler.module;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;

import com.google.protobuf.MessageLite;
import common.LogHelper;
import config.DynamicInfoProvider;
import config.provider.ConfPlayerExceptionProvider;
import config.provider.DynamicPropertiesPublicProvider;
import data.MoneySubAction;
import database.DBUtil;
import database.DataQueryResult;
import define.AppId;
import define.constant.DynamicPublicConst;
import define.constant.MessageConst;
import io.netty.channel.ChannelHandlerContext;
import mail.MailEntity;
import network.AbstractHandlers;
import network.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.AccountCreator;
import protobuf.creator.CommonCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/11.
 */
public class ExchangeModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(ExchangeModule.class);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.ACCOUNT_EXCHANGE.getValue(), this::actionExchange, Common.PBInt32.getDefaultInstance());
	}

	public void actionExchange(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBInt32 request = message.get();
		int value = request.getValue();
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		// 总开关
		if (!DynamicPropertiesPublicProvider.getInst().isOpen(DynamicPublicConst.EXCHANGE_KEY)) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PROVINCE_CLOSE));
			logger.error("无法兑换 请检查 全局兑换开关为关闭状态");
			return;
		}
		if (!player.getProvinceData().getExchange()) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PROVINCE_CLOSE));
			logger.error("无法兑换 请检查  {}区域兑换开关为关闭状态", player.getProvince());
			return;
		}
		if (player.getDeskInfo() != null) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.AT_GAME_ROOM_ING));
			return;
		}
		if (value <= 0) {
			logger.warn("player {} want exchange the money value <= 0", player.getPlayerId());
			return;
		}
		if (value < DynamicInfoProvider.getInst().getExchange1Min() * 100) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_NO_5000_MULTIPLE));
			return;
		}
		if (value % (DynamicInfoProvider.getInst().getExchange_lowExch() * 100) != 0) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_NO_5000_MULTIPLE));
			return;
		}
		if (player.getCoin() < value + CenterServer.getInst().getExchangeRetain() * 100) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_COIN_NOT_ENOUGH));
			return;
		}

		/// 玩家是否异常
		int danger_leve = ConfPlayerExceptionProvider.getInst().isPlayerException(player);
		long tmp_value = 0;
		if (0 != danger_leve) {
			tmp_value = ConfPlayerExceptionProvider.getInst().insertException(player, danger_leve);
		}
		long danger_leve_flag = tmp_value;

//		CenterActorManager.getLogicActor(player.getPlayerId()).put(() -> {

			/// 黑名单不能兑换
			Map<String, Object> where = new HashMap<>();
			where.put("player_id", player.getPlayerId());
			List<ASObject> list = DataQueryResult.load("exchange_black", where);
			if (0 < list.size()) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_BLACKLIST));
				return;
			}

			int needTax = 0;
			if (DynamicInfoProvider.getInst().getExchange1Min() * 100 <= value && value <= DynamicInfoProvider.getInst().getExchange1Max() * 100) {
				needTax = DynamicInfoProvider.getInst().getExchange1Need() * 100;
			} else {
				needTax = value * DynamicInfoProvider.getInst().getExchange2Need() / 100;
			}

			int needPay = value - needTax;
			Map<String, Object> data = new HashMap<>();
			data.put("phone_num", player.getPhone_num());
			data.put("ali_account", player.getAlipayAccount());
			data.put("exchange_count", value);
			data.put("tax_count", needTax);
			data.put("ali_name", player.getAlipayName());
			data.put("need_pay", needPay);
			data.put("exchange_time", MiscUtil.getCurrentSeconds());
			data.put("operation_time", 0);
			data.put("player_id", player.getPlayerId());
			data.put("pre_coin", player.getCoin());
			data.put("pre_bank_coin", player.getBankMoney());
			data.put("channel_id", player.getChannelId());
			data.put("package_id", player.getPackageId());
			data.put("device", player.getDevice());
			data.put("ip", player.getIp());
			data.put("url", player.getProvinceData().getExchangeUrl());
			int orderId = 0;
			try {
				orderId = (int) DBUtil.executeInsert("exchange", data);
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}

			long pre_coin = player.getCoin();
			player.updateCoin(value, false);
			PlayerSaver.savePlayerBase(player);
			logger.info("金币更新 :玩家 {} 兑换， 金币减少 {}，当前总金币数为 {}", player.getPlayerId(), value, player.getCoin());
			player.write(ResponseCode.ACCOUNT_EXCHANGE_SUCC, AccountCreator.createPBLoginSucc(player));
			MailEntity mail = MailEntity.createMail(player.getPlayerId(), player.getAvailableMailId(), 2, 0
					, String.valueOf(value / 100), String.valueOf(needTax / 100));
			player.addMail(mail);
			CenterActorManager.getDbActor(player.getPlayerId()).put(() -> {
				MailEntity.insertMailIntoDataBase(mail);
				return null;
			});

			player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
			StringBuilder builder = new StringBuilder();
			String sign = MiscUtil.getMD5(String.valueOf(player.getPlayerId()) + orderId + needPay + needTax + CenterServer.getInst().getExchangeKey());
			builder.append(CenterServer.getInst().getExchangeUrl());
			builder.append("?mid=");
			builder.append(player.getPlayerId());
			builder.append("&agent_id=");
			builder.append(orderId);
			builder.append("&amount=");
			builder.append(needPay);
			builder.append("&needTax=");
			builder.append(needTax);
			builder.append("&alipay_name=");
			builder.append(player.getAlipayName());
			builder.append("&alipay_account=");
			builder.append(player.getAlipayAccount());
			builder.append("&sign=");
			builder.append(sign);
			builder.append("&danger_leve_flag=");
			builder.append(danger_leve_flag);
			builder.append("&ip=");
			builder.append(player.getIp());
			builder.append("&device=");
			builder.append(player.getDevice());
			builder.append("&provinceId=");
			builder.append(player.getProvinceData().getId());
			// 检查异常参数
			builder.append("&pay_total="); 
			builder.append(player.getPayAll());
			builder.append("&exchange_total="); 
			builder.append(player.getExchangeAll());
			builder.append("&action_money_total="); // 累计投注金额 = 输钱金额 + 赢钱金额	
			builder.append(player.getWin_money() + Math.abs(player.getLose_money()));
			builder.append("&win_round=");
			builder.append(player.getWin_round());
			builder.append("&lose_round=");
			builder.append(player.getLose_round());
			
			logger.info("兑换 ##### 发送给后台的数据:{}", builder.toString());
			CenterActorManager.getHttpActor().put(() -> http.HttpUtil.sendGet(builder.toString()));
			ServerManager.getInst().getMinLoadSession(AppId.LOG)
					.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
							, LogHelper.logLoseMoney(player.getPlayerId(), MoneySubAction.EXCHANGE_LOSE.getValue(), 0, value, pre_coin, pre_coin-value, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice(), 0)));

			recordExchangeLog(orderId, player.getPlayerId(), 4, builder.toString());

			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_SUC));

			return;
//		});
	}

	public static void recordExchangeLog(int order_id, int player_id, int status, String content) {
		Map<String, Object> data = new HashMap<>();
		data.put("order_id", order_id);
		data.put("player_id", player_id);
		data.put("op", status);
		data.put("content", content);
		data.put("time", MiscUtil.getCurrentSeconds());

		try {
			DBUtil.executeInsert("exchange_log", data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
