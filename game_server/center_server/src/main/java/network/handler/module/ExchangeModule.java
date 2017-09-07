package network.handler.module;

import com.google.protobuf.MessageLite;
import database.DBUtil;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import proto.Common;
import protocol.c2s.RequestCode;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.HashMap;
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
