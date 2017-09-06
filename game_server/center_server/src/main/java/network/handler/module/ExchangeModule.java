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
