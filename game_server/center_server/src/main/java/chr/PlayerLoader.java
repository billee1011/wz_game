package chr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;

import config.bean.TransferData;
import database.DBManager;
import database.DataQueryResult;
import define.Gender;
import mail.MailEntity;
import proto.CoupleMajiang;
import util.ASObject;
import util.MiscUtil;

public class PlayerLoader {

//	public static void loadFromCharData(CharData data, Player player) {
//		loadCharBase(data, player);
//		loadScoreHis(data, player);
//		loadAllMail(player);
//		loadAllAgentPay(player);
//	}
//
//	private static void loadScoreHis(CharData data, Player player) {
//		ASObject obj = data.getModuleData(DBAction.SCORE_LIST);
//		Object[] objArray = (Object[]) obj.get("" + data.getCharId());
//		for (Object o : objArray) {
//			ASObject baseData = (ASObject) o;
//			byte[] bytes = (byte[]) baseData.get("record_data");
//			try {
//				CoupleMajiang.PBPairGameNo2Record message = CoupleMajiang.PBPairGameNo2Record.parseFrom(bytes);
//				player.addRoomRecordList(message);
//			} catch (InvalidProtocolBufferException e) {
//				e.printStackTrace();
//			}
//		}
//	}

//	private static void loadCharBase(CharData data, Player player) {
//		ASObject obj = data.getModuleData(DBAction.PLAYER);
//		Object[] objArray = (Object[]) obj.get("" + data.getCharId());
//		ASObject baseData = (ASObject) objArray[0];
//		player.setPlayerId(baseData.getInt("player_id"));
//		player.setAccountId(baseData.getInt("user_id"));
//		player.setName(baseData.getString("nickname"));
//		player.setIcon(baseData.getString("headIcon"));
//		player.setCoin(baseData.getLong("coin"));
//		player.setTiyanCoin(baseData.getInt("tiyan_coin"));
//		player.setBankPassword(baseData.getString("bank_password"));
//		player.setBankMoney(baseData.getLong("bank_coin"));
//		player.setWinScore(baseData.getInt("score"));
//		player.setRechargeScore(baseData.getInt("recharge_score"));
//		player.setComplaint_total(baseData.getInt("complaint_total"));
//		player.setComplaint_total_time(baseData.getInt("complaint_total_time"));
//		player.setIs_send_pay_mail(baseData.getInt("is_send_pay_mail"));
//		player.setPay_total(baseData.getLong("pay_total"));
//		player.setGame_total(baseData.getInt("game_total"));
//		player.setOnline_time_duration(baseData.getLong("online_time_duration"));
//		player.setIs_open_exchange(baseData.getInt("is_open_exchange"));
//		player.setLose_money(baseData.getLong("lose_money"));
//		player.setWin_money(baseData.getLong("win_money"));
//		player.setPay_money(baseData.getLong("pay_money"));
//		player.setOnly_show_agent(baseData.getBoolean("only_show_agent"));
//        player.setExp_level(baseData.getInt("exp_level"));
//		player.setAgent_plan(baseData.getInt("agent_plan"));
//		player.setRecharge(baseData.getInt("recharge"));
//		player.setCurrent_game_count(baseData.getInt("current_game_count"));
//		player.setGame_time(baseData.getInt("game_time"));
//		player.setExchange_total(baseData.getInt("exchange_total"));
//		player.setLose_round(baseData.getInt("lose_round"));
//		player.setWin_round(baseData.getInt("win_round"));
//		player.setDevice(baseData.getString("device"));
//		player.setGender(Gender.getByValue(baseData.getInt("gender")));
//		player.setLastSaveTime(System.currentTimeMillis());
//
//		loadAccount(player);
//	}

	public static Player loadPlayer(int playerId) {
		Player player = loadPlayerBase(playerId);
		if(player == null){
			return null;
		}
		loadAccount(player);
		loadScoreHis(player);
		loadAllMail(player);
		loadAllAgentPay(player);
		return player;
	}
	
	public static Player loadPlayerBase(int playerId) {
		Map<String, Object> where = MiscUtil.newArrayMap();
		where.put("player_id", playerId);
		List<ASObject> dataList = DataQueryResult.load("player", where);
		if(dataList.size() <= 0){
			return null;
		}
		Player player = Player.getDefault();
		ASObject baseData = dataList.get(0);
		player.setPlayerId(baseData.getInt("player_id"));
		player.setAccountId(baseData.getInt("user_id"));
		player.setName(baseData.getString("nickname"));
		player.setIcon(baseData.getString("headIcon"));
		player.setCoin(baseData.getLong("coin"));
		player.setTiyanCoin(baseData.getInt("tiyan_coin"));
		player.setBankPassword(baseData.getString("bank_password"));
		player.setBankMoney(baseData.getLong("bank_coin"));
		player.setWinScore(baseData.getInt("score"));
		player.setRechargeScore(baseData.getInt("recharge_score"));
		player.setComplaint_total(baseData.getInt("complaint_total"));
		player.setComplaint_total_time(baseData.getInt("complaint_total_time"));
		player.setIs_send_pay_mail(baseData.getInt("is_send_pay_mail"));
		player.setPay_total(baseData.getLong("pay_total"));
		player.setGame_total(baseData.getInt("game_total"));
		player.setOnline_time_duration(baseData.getLong("online_time_duration"));
		player.setIs_open_exchange(baseData.getInt("is_open_exchange"));
		player.setLose_money(baseData.getLong("lose_money"));
		player.setWin_money(baseData.getLong("win_money"));
		player.setPay_money(baseData.getLong("pay_money"));
		player.setPay_money_agent(baseData.getLong("pay_money_agent"));
		player.setOnly_show_agent(baseData.getBoolean("only_show_agent"));
        player.setExp_level(baseData.getInt("exp_level"));
		player.setAgent_plan(baseData.getInt("agent_plan"));
		player.setRecharge(baseData.getInt("recharge"));
		player.setCurrent_game_count(baseData.getInt("current_game_count"));
		player.setGame_time(baseData.getInt("game_time"));
		player.setExchange_total(baseData.getInt("exchange_total"));
		player.setExchange_total_agent(baseData.getInt("exchange_total_agent"));
		player.setLose_round(baseData.getInt("lose_round"));
		player.setWin_round(baseData.getInt("win_round"));
		player.setDevice(baseData.getString("device"));
		player.setGender(Gender.getByValue(baseData.getInt("gender")));
		player.setLastSaveTime(System.currentTimeMillis());
		player.setShowBankMark(baseData.getInt("show_bank_mark"));
		return player;
	}
	
	private static void loadScoreHis(Player player) {
		Map<String, Object> where = MiscUtil.newArrayMap();
		where.put("player_id", player.getPlayerId());
		List<ASObject> dataList = DataQueryResult.load("player_score_list", where);
		for (ASObject baseData : dataList) {
			byte[] bytes = (byte[]) baseData.get("record_data");
			try {
				CoupleMajiang.PBPairGameNo2Record message = CoupleMajiang.PBPairGameNo2Record.parseFrom(bytes);
				player.addRoomRecordList(message);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void loadAccount(Player player) {
		if (null == player) {
			return;
		}

		Map<String, Object> where = new HashMap<>();
		where.put("user_id", player.getAccountId());
		List<ASObject> resultList = DataQueryResult.load("accounts", where);

		if (0 < resultList.size()) {
			player.setAlipayAccount(resultList.get(0).getString("alipay_account"));
			player.setAlipayName(resultList.get(0).getString("alipay_name"));
			player.setPhone_num(resultList.get(0).getString("phone_num"));
			player.setPlatform_id(resultList.get(0).getString("platform_id"));
		}
	}

	public static void loadAllMail(Player player) {
		Map<String, Object> where = new HashMap<>();
		where.put("player_id", player.getPlayerId());
		List<ASObject> mailList = DataQueryResult.load("mail", where);
		mailList.forEach(e -> player.addMail(MailEntity.createMail(e)));
	}

	public static void loadAllAgentPay(Player player) {

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getConnection();
			stat = conn.prepareStatement("SELECT * FROM agent_pay WHERE (player_out_id = " + player.getPlayerId() + " or player_in_id = " + player.getPlayerId() + ") and (type = 1 or type = 2)");
			rs = stat.executeQuery();
			while (rs.next()) {
				player.addTransferList(TransferData.createTransferData(rs.getInt("id"), rs.getInt("player_out_id"), rs.getInt("player_in_id"), rs.getString("player_in_name"), rs.getInt("amount"), rs.getInt("type"), rs.getInt("time")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.close(conn, stat, rs);
		}
	}

//	public static Player getOfflineInfo(int player_id) {
//		Map<String, Object> where = new HashMap<>();
//		where.put("player_id", player_id);
//		List<ASObject> player_list = DataQueryResult.load("player", where);
//		if(1 == player_list.size()) {
//			Player player = new Player();
//			player.setName(player_list.get(0).getString("nickname"));
//			player.setGameChannel(player_list.get(0).get("channel_id").toString());
//			player.setPackageId(player_list.get(0).getString("package_id"));
//			player.setCoin(player_list.get(0).getLong("coin"));
//			player.setBankMoney(player_list.get(0).getLong("bank_coin"));
//			player.setDevice(player_list.get(0).getString("device"));
//			player.setIp(player_list.get(0).getString("ip"));
//			player.setGender(Gender.getByValue(player_list.get(0).getInt("gender")));
//			return player;
//		}
//		return null;
//	}

//	public static ASObject getOfflineValue(int player_id) {
//		Map<String, Object> where = new HashMap<>();
//		where.put("player_id", player_id);
//		List<ASObject> player_list = DataQueryResult.load("player", where);
//		if(1 == player_list.size()) {
//			return player_list.get(0);
//		}
//		return null;
//	}

//	/// 离线处理数据保存
//	public static ASObject getOfflinePlayerData(int player_id) {
//		//查看缓存里面是否有玩家数据
//		Object data = DataManager.getInst().getCache().query(player_id);
//		if (data == null) {
//			return getOfflineValue(player_id);
//		} else {
//			//如果在缓存就要修改缓存数据了
//			CharData charData = (CharData) data;
//			ASObject obj = charData.getModuleData(DBAction.PLAYER);
//			Object[] objArray = (Object[]) obj.get("" + player_id);
//			ASObject baseData = (ASObject) objArray[0];
//			return baseData;
//		}
//	}
}
