package logic.record;

import actor.LogicActorManager;
import config.JsonUtil;
import database.DBUtil;
import logic.Desk;
import logic.define.GameType;
import logic.majiong.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import service.LogicApp;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/1/4.
 */
public class TaxRecordUtil {
	private static Logger logger = LoggerFactory.getLogger(TaxRecordUtil.class);

	public static void sendGamePlayerStatus(PlayerInfo player_info, long amount) {
		/// 把当前这一局中玩家的ID过去进行记录
		if(null != player_info) {
			LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_GMAME_PLAYERS, CommonCreator.createPBPairString(String.valueOf(player_info.getPlayerId()), String.valueOf(amount)),player_info.getPlayerId()));
		}
	}

	public static void recordGameTaxInfo(int startTime, int playerCount, long gameNo, GameType game
			, int roomId, int bannerId, int money, int tax, Object detail, Desk desk) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("game_no", gameNo);
		data.put("game_type", game.name());
		data.put("room_id", roomId);
		data.put("banner_id", bannerId);
		data.put("flow_count", money);
		data.put("tax_count", tax);
		data.put("start_time", startTime);
		data.put("player_count", playerCount);
		data.put("time", MiscUtil.getCurrentSeconds());
		data.put("detail", JsonUtil.getGson().toJson(detail));
		LogicActorManager.getLogicActor().put(() -> {
			try {
				DBUtil.executeInsert("tax_info", data);
				recordPlayerTaxInfo(game, detail, desk, roomId);
			} catch (SQLException f) {
				f.printStackTrace();
			}
			return null;
		});
	}

	public static void recordPlayerTaxInfo(GameType game, Object detail, Desk desk, int roomId) {
		if(null == desk || null == game || null == detail) {
			logger.error("desk is null or game is null or detail is null");
			return ;
		}
		desk.recordPlayerTaxInfo(detail, roomId);
	}

	public static void recordPlayerTaxInfoToDB(int type, int player_id, int roomId, int tax, int channel_id, int package_id, String device) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("type", type);
			data.put("player_id", player_id);
			data.put("room_id", roomId);
			data.put("tax", tax);
			data.put("channel_id", channel_id);
			data.put("package_id", package_id);
			data.put("device", device);
			data.put("time", MiscUtil.getCurrentSeconds());
			DBUtil.executeInsert("player_tax", data);
		} catch (SQLException f) {
			f.printStackTrace();
		}
	}

	public static int recordGamReply(String json, int startTime, Collection<PlayerInfo> playerList, GameType type, String game_no) {
		Map<String, Object> data = new HashMap<>();
		data.put("game_text", json);
		data.put("time", MiscUtil.getCurrentSeconds());
		data.put("start_time", startTime);
		data.put("game_no", game_no);
		int count = 0;
		for (PlayerInfo player : playerList) {
			count++;
			data.put("player_" + count, player.getPlayerId());
		}
		try {
			return (int) DBUtil.executeInsert("game_record", data);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	private static AtomicInteger count = new AtomicInteger(1);

	public static void recordGameTimes(int deskId) {
		int num = count.getAndIncrement();
		logger.info(" new game round start desk id {} ,and the round is {}", deskId, num);
	}


}
