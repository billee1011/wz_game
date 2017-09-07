package chr;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.CenterActorManager;
import actor.IActor;
import database.DBUtil;
import proto.CoupleMajiang;
import util.MiscUtil;

public class PlayerSaver {

	private static Logger logger = LoggerFactory.getLogger(PlayerSaver.class);

	public static void savePlayer(Player player) {
		savePlayerBase(player);
//		savePlayerScoreList(player);
		
//		savePlayerBase(player);
//		savePlayerScoreList(player);
	}

//	private static void savePlayerBase(Player player) {
//		ASObject data = new ASObject();
//		data.put("player_id", player.getPlayerId());
//		data.put("nickname", player.getName());
//		data.put("coin", player.getCoin());
//		data.put("tiyan_coin", player.getTiyanCoin());
//		data.put("headIcon", player.getIcon());
//		data.put("bank_password", player.getBankPassword());
//		data.put("bank_coin", player.getBankMoney());
//		data.put("score", player.getWinScore());
//		data.put("recharge_score", player.getRechargeScore());
//		data.put("user_id", player.getAccountId());
//		data.put("complaint_total", player.getComplaint_total());
//		data.put("complaint_total_time", player.getComplaint_total_time());
//		data.put("is_send_pay_mail", player.getIs_send_pay_mail());
//		data.put("pay_total", player.getPay_total());
//		data.put("game_total", player.getGame_total());
//		data.put("online_time_duration", player.getOnline_time_duration());
//		data.put("is_open_exchange", player.getIs_open_exchange());
//		data.put("lose_money", player.getLose_money());
//		data.put("win_money", player.getWin_money());
//		data.put("pay_money", player.getPay_money());
//		data.put("channel_id",player.getChannelId());
//		data.put("package_id",player.getPackageId());
//		data.put("only_show_agent", player.isOnly_show_agent());
//		data.put("exp_level",player.getExp_level());
//		data.put("agent_plan", player.getAgent_plan());
//		data.put("recharge", player.getRecharge());
//		data.put("current_game_count", player.getCurrent_game_count());
//		data.put("game_time", player.getGame_time());
//		data.put("ip", player.getIp());
//		data.put("exchange_total", player.getExchange_total());
//		data.put("machine_id", player.getMachine_id());
//		data.put("lose_round", player.getLose_round());
//		data.put("win_round", player.getWin_round());
//		data.put("game_version", player.getGame_version());
//		data.put("device", player.getDevice());
//		data.put("gender",player.getGender().getValue());
//		ASObject module = new ASObject();
//		
//		module.put("" + player.getPlayerId(), new ASObject[]{data});
//		DataManager.getInst().saveModule(player.getPlayerId(), DBAction.PLAYER, module);
//	}
//
	
//
//	private static void savePlayerScoreList(Player player) {
//		List<ASObject> dataList = new ArrayList<>();
//		if (player.getRecordList() != null) {
//			player.getRecordList().forEach(e -> {
//				ASObject data = new ASObject();
//				data.put("player_id", player.getPlayerId());
//				data.put("game_no", e.getGameNo());
//				data.put("record_data", e.toByteArray());
//				dataList.add(data);
//			});
//		}
//		ASObject module = new ASObject();
//		module.put("" + player.getPlayerId(), dataList.toArray());
//		DataManager.getInst().saveModule(player.getPlayerId(), DBAction.SCORE_LIST, module);
//	}
	
	public static boolean isComplaint(Player player) {
		/// 投述次数与时间的处理
		return MiscUtil.isSameDay(MiscUtil.getCurrentSeconds(), player.getComplaint_total_time());
	}
	
	public static void savePlayerBase(Player player) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("player_id", player.getPlayerId());
		data.put("nickname", player.getName());
		data.put("coin", player.getCoin());
		data.put("tiyan_coin", player.getTiyanCoin());
		data.put("headIcon", player.getIcon());
		data.put("bank_password", player.getBankPassword());
		data.put("bank_coin", player.getBankMoney());
		data.put("score", player.getWinScore());
		data.put("recharge_score", player.getRechargeScore());
		data.put("user_id", player.getAccountId());
		data.put("complaint_total", player.getComplaint_total());
		data.put("complaint_total_time", player.getComplaint_total_time());
		data.put("is_send_pay_mail", player.getIs_send_pay_mail());
		data.put("pay_total", player.getPay_total());
		data.put("game_total", player.getGame_total());
		data.put("online_time_duration", player.getOnline_time_duration());
		data.put("is_open_exchange", player.getIs_open_exchange());
		data.put("lose_money", player.getLose_money());
		data.put("win_money", player.getWin_money());
		data.put("pay_money", player.getPay_money());
		data.put("pay_money_agent", player.getPay_money_agent());
		data.put("channel_id", player.getChannelId());
		data.put("package_id", player.getPackageId());
		data.put("only_show_agent", player.isOnly_show_agent());
		data.put("exp_level", player.getExp_level());
		data.put("agent_plan", player.getAgent_plan());
		data.put("recharge", player.getRecharge());
		data.put("current_game_count", player.getCurrent_game_count());
		data.put("game_time", player.getGame_time());
		data.put("ip", player.getIp());
		data.put("exchange_total", player.getExchange_total());
		data.put("exchange_total_agent", player.getExchange_total_agent());
		data.put("machine_id", player.getMachine_id());
		data.put("lose_round", player.getLose_round());
		data.put("win_round", player.getWin_round());
		data.put("game_version", player.getGame_version());
		data.put("device", player.getDevice());
		data.put("gender", player.getGender().getValue());
		data.put("show_bank_mark", player.getShowBankMark());

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("player_id", player.getPlayerId());

		put(new SaveObject(player.getPlayerId(), "player", data, where));
	}
	
//	public static void savePlayerScoreList(Player player) {
//		if (player.getRecordList() != null) {
//			player.getRecordList().forEach(e -> {
//				savePlayerScore(player, e);
//			});
//		}
//	}
	
	public static void savePlayerAlipay(Player player) {
		Map<String, Object> data = new HashMap<>();
		data.put("alipay_account", player.getAlipayAccount());
		data.put("alipay_name", player.getAlipayName());

		Map<String, Object> where = new HashMap<>();
		where.put("user_id", player.getAccountId());

		put(new SaveObject(player.getPlayerId(), "accounts", data, where));
	}
	
	public static void savePlayerScore(Player player,CoupleMajiang.PBPairGameNo2Record e){
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("player_id", player.getPlayerId());
		data.put("game_no",  e.getGameNo());
		data.put("record_data", e.toByteArray());

		put(new SaveObject(player.getPlayerId(), "player_score_list", data));
	}
	
	public static void savePlayerLoginStatus(Player player){
		Map<String, Object> data = new HashMap<>();
		data.put("login", player.getLoginStatus());
		
		Map<String, Object> where = new HashMap<>();
		where.put("player_id", player.getPlayerId());
		
		put(new SaveObject(player.getPlayerId(), "player", data, where));
	}
	
	public static void put(SaveObject saveObject) {
		IActor actor = CenterActorManager.getDbActor(saveObject.playerId);
		actor.put(() -> {
			try {
				if(saveObject.where == null){
					DBUtil.executeInsert(saveObject.tabName, saveObject.data);
				}else{
					DBUtil.executeUpdate(saveObject.tabName, saveObject.where, saveObject.data);
				}
			} catch (Exception e) {
				logger.error("保存数据库线程出错", e);
			}
			return null;
		});
	}

	public static int insertPlayer(Player player) {
		Map<String, Object> data = new HashMap<>();
		data.put("player_id", player.getPlayerId());
		data.put("nickname", player.getName());
		data.put("coin", player.getCoin());
		data.put("tiyan_coin", player.getTiyanCoin());
		data.put("headIcon", player.getIcon());
		data.put("bank_password", player.getBankPassword());
		data.put("bank_coin", player.getBankMoney());
		data.put("score", player.getWinScore());
		data.put("recharge_score", player.getRechargeScore());
		data.put("user_id", player.getAccountId());
		data.put("complaint_total", player.getComplaint_total());
        data.put("complaint_total_time", player.getComplaint_total_time());
		data.put("is_send_pay_mail", player.getIs_send_pay_mail());
		data.put("pay_total", player.getPay_total());
		data.put("game_total", player.getGame_total());
		data.put("online_time_duration", player.getOnline_time_duration());
		data.put("is_open_exchange", player.getIs_open_exchange());
		data.put("lose_money", player.getLose_money());
		data.put("win_money", player.getWin_money());
		data.put("pay_money", player.getPay_money());
		data.put("pay_money_agent", player.getPay_money_agent());
		data.put("channel_id",player.getChannelId());
		data.put("package_id",player.getPackageId());
		data.put("only_show_agent", player.isOnly_show_agent());
		data.put("exp_level",player.getExp_level());
		data.put("agent_plan", player.getAgent_plan());
		data.put("ip", player.getIp());
		data.put("exchange_total", player.getExchange_total());
		data.put("exchange_total_agent", player.getExchange_total_agent());
		data.put("machine_id", player.getMachine_id());
		data.put("lose_round", player.getLose_round());
		data.put("win_round", player.getWin_round());
		data.put("game_version", player.getGame_version());
		data.put("device", player.getDevice());
		data.put("gender",player.getGender().getValue());
		data.put("show_bank_mark", player.getShowBankMark());
		try {
			return (int) DBUtil.executeInsert("player", data);
		} catch (SQLException e) {
			logger.error("{}", e);
			return 0;
		}
	}

//	public static void saveOfflinePlayer(String sql) {
//		Connection conn = null;
//		PreparedStatement stat = null;
//		try {
//			conn = DBManager.getConnection();
//			stat = conn.prepareStatement(sql);
//			stat.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			DBManager.close(conn, stat);
//		}
//	}

//	/// 离线处理数据读取
//	public static ASObject offlineSavePlayerData(int player_id, Map<String, String> map_data, String sql) {
//		ASObject as_object = new ASObject();
//		//查看缓存里面是否有玩家数据
//		Object data = DataManager.getInst().getCache().query(player_id);
//		if (data == null) {
//			Player player = PlayerLoader.getOfflineInfo(player_id);
//			if(null == player) {
//				logger.error("handleExchangeRestore player is offline player_id:" + player_id);
//				return null;
//			}
//
//			as_object.put("coin", player.getCoin());
//			as_object.put("bank_coin", player.getBankMoney());
//			as_object.put("ip", player.getIp());
//			as_object.put("channel_id", player.getChannelId());
//			as_object.put("package_id", player.getPackageId());
//			as_object.put("device", player.getDevice());
//
//			ProcLogic.updateOfflineId(sql);
//		} else {
//			//如果在缓存就要修改缓存数据了
//			CharData charData = (CharData) data;
//			ASObject obj = charData.getModuleData(DBAction.PLAYER);
//			Object[] objArray = (Object[]) obj.get("" + player_id);
//			ASObject baseData = (ASObject) objArray[0];
//
//			as_object = baseData.clone();
//			for(Map.Entry<String, String> entry : map_data.entrySet()) {
//				if(true == isAdd(entry.getKey())) {
//					baseData.put(entry.getKey(), baseData.getInt(entry.getKey()) + Integer.valueOf(entry.getValue()));
//				} else {
//					baseData.put(entry.getKey(), entry.getValue());
//				}
//			}
//
//			//修改这个然后存盘了啊
//			DataManager.getInst().saveModule(player_id, DBAction.PLAYER, obj);
//		}
//
//		return as_object;
//	}

	public static boolean isAdd(String key) {
		if(true == key.equals("coin") ||
			true == key.equals("recharge") ||
			true == key.equals("pay_money") ||
			true == key.equals("recharge_score") ||
			true == key.equals("bank_coin")) {
			return true;
		}
		return false;
	}
}
