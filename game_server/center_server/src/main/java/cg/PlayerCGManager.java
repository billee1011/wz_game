package cg;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chr.Player;
import chr.PlayerManager;
import config.bean.PlayerCGConfigData;
import config.provider.PlayerCGConfigProvider;
import database.DBUtil;
import database.DataQueryResult;
import protobuf.creator.CommonCreator;
import protocol.s2c.ResponseCode;
import util.ASObject;
import util.MiscUtil;

/**
 * Created by hhhh on 2017/4/13.
 */
public class PlayerCGManager {
	private static PlayerCGManager inst = new PlayerCGManager();

	public static PlayerCGManager getInst(){
		return inst;
	}


	public void sendPlayerCgInfo(Player player){
		PlayerCGConfigData data = getTheMaxCG(player);
		if(data != null){
			sendPlayerCgInfo(player,data);
		}
	}

	public void sendPlayerCgInfo(Player player,PlayerCGConfigData data){
		if(player.getPlayerCgMap().get(data.getId()) == null){
			player.write(ResponseCode.PLAYRE_CG, CommonCreator.createPBPairString(data.getLimit_detail(),data.getForward_url()));
			player.putPlayerCg(data.getId());
			savePlayerCgInfo(player,data);
		}
	}

	public void savePlayerCgInfo(Player player,PlayerCGConfigData data){
		Map<String, Object> playerCgInfo
				= new HashMap<>();
		playerCgInfo.put("player_id", player.getPlayerId());
		playerCgInfo.put("player_cg", data.getId());
		playerCgInfo.put("ready_time", MiscUtil.getCurrentSeconds());
		try {
			DBUtil.executeInsert("player_cg_info", playerCgInfo);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	public void loadAlreadyCgInfo(Player player){
		List<ASObject> data_list = DataQueryResult.load("select player_cg from player_cg_info p where p.player_id="+player.getPlayerId()+" and p.ready_time > "+MiscUtil.getTodayZeroTime(System.currentTimeMillis())+"");
		for (ASObject data_info : data_list) {
			player.putPlayerCg(data_info.getInt("player_cg"));
		}
	}

	public PlayerCGConfigData getTheMaxCG(Player player){
		PlayerCGConfigData maxData = null;
		PlayerCGConfigProvider inst = PlayerCGConfigProvider.getInst();
		List<PlayerCGConfigData> list =  inst.getAll();
		for (PlayerCGConfigData data:list){
			if(checkCompletion(player,data)){
				maxData = data;
			}
		}
		return maxData;
	}

	private boolean checkCompletion(Player player,PlayerCGConfigData data){
		switch (data.getCompletion_time()){
			case 0:		//当日
				return checkTodayCompletion(player,data);
			case 1:		//累计
				return checkTotalCompletion(player,data);
			default:
				break;
		}
		return false;
	}
	private boolean checkTotalCompletion(Player player,PlayerCGConfigData data){
		switch (data.getCompletion_type()){
			case 0:	//充值
				return  player.getPay_total() >= data.getCompletion();
			case 1://游戏局数
				return player.getGame_total() >= data.getCompletion();
			default:
				break;
		}
		return false;
	}

	private boolean checkTodayCompletion(Player player,PlayerCGConfigData data){
		switch (data.getCompletion_type()){
			case 0:	//充值
				return  player.getRecharge() >= data.getCompletion();
			case 1://游戏局数
				return player.getCurrent_game_count() >= data.getCompletion();
			case 2://游戏时间
				return player.getGame_time() >= data.getCompletion();
			default:
				break;
		}
		return false;
	}

	public void clearPlayerCg(){
		Map<String, Object> updateData = new HashMap<>();
		updateData.put("recharge","0");
		updateData.put("current_game_count","0");
		updateData.put("game_time","0");
		Map<String, Object> where = new HashMap<>();
		try {
			DBUtil.executeUpdate("player", where, updateData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		PlayerManager.getInstance().getAllPlayers().forEach(e ->{
			e.clearTodayStatus();
		});
	}
}
