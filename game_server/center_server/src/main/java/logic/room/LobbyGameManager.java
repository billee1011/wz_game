package logic.room;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import chr.PlayerManager;
import config.JsonUtil;
import config.provider.ConfServerStateProvider;
import database.DBUtil;
import define.GameType;
import define.constant.GameModuleConst;
import define.constant.LoginStatusConst;
import util.Pair;

public class LobbyGameManager {
	private static LobbyGameManager instance = new LobbyGameManager();
	private static Logger logger = LoggerFactory.getLogger(LobbyGameManager.class);

	private LobbyGameManager() {

	}

	public static LobbyGameManager getInst() {
		return instance;
	}

	private Map<GameType, GameRoomInterface> gameMap = new HashMap<>();

	private Map<Integer, Player> roomPlayerMap = new HashMap<>();

	private Map<Integer, Player> lobbyPlayerMap = new HashMap<>();
	
	/** 私房玩家人數 游戲類型，玩家個數 */
	private Map<GameType, Set<Integer>> priRoomToPlayers = new HashMap<>();

	public void init() {
		for (GameType type : GameType.values()) {
			GameRoomInterface facade = type.createFacade();
			if (facade == null) {
				continue;
			}
			facade.initGameRoom();
			gameMap.put(type, facade);
			priRoomToPlayers.put(type, new HashSet<>());
		}
	}
	
	public void addRoom() {
		for (GameType type : GameType.values()) {
			GameRoomInterface facade = gameMap.get(type);
			if (facade == null) {
				continue;
			}
			facade.addGameRoom();
		}
	}

	public List<Pair<Integer, Integer>> getRoomPlayerList(int game) {
		GameType gameType = GameType.getByValue(game);
		GameRoomInterface facade = gameMap.get(gameType);
		if (facade != null) {

			return facade.getRoomPlayerList();
		}
		return null;
	}

	public Map<Integer, Integer> getRoomPlayerList_ex(int game) {
		Map<Integer, Integer> mapState = new HashMap<>();
		GameType gameType = GameType.getByValue(game);
		GameRoomInterface facade = gameMap.get(gameType);
		if (facade != null) {
			List<Pair<Integer, Integer>> roomNumList = facade.getRoomPlayerList_ex();
			for(Pair<Integer, Integer> roomNum : roomNumList){
				mapState.put(roomNum.getLeft(), ConfServerStateProvider.getInst().getServerState(roomNum.getRight()));
			}
			return mapState;
		}
		return mapState;
	}

	public List<Pair<Integer, Integer>> getRoomStatusList(int game) {
		GameType gameType = GameType.getByValue(game);
		GameRoomInterface facade = gameMap.get(gameType);
		if (facade != null) {

			return facade.getRoomStatusList();
		}
		return null;
	}

//	public Map<Integer, Integer> getModulePlayerNum() {
//		Map<Integer, Integer> result = new HashMap<>();
//		for (GameType type : GameType.values()) {
//			GameRoomInterface facade = gameMap.get(type);
//			if (facade == null) {
//				continue;
//			}
//			result.put(type.getValue(), facade.getPlayerCount());
//		}
//		result.put(999, roomPlayerMap.size());
//		return result;
//	}
	
	public Map<Integer, Integer> getGameModelState(int moduleId){
		Map<Integer, Integer> mapState = new HashMap<>();
		if(moduleId == 0){
			int priNum = 0;
			Map<Integer, Integer> mapGameNum = new HashMap<>();
			for (GameType type : GameType.values()) {
				Integer curNum = mapGameNum.get(type.getModule());
				if(curNum == null){
					curNum = 0;
					mapGameNum.put(type.getModule(),curNum);
				}
				curNum += gameMap.get(type).getPlayerCount();
				mapGameNum.put(type.getModule(),curNum);
				
				priNum += getPriRoomPlayerNum(type);
			}
			for(Entry<Integer, Integer> entry : mapGameNum.entrySet()){
				mapState.put(entry.getKey(), ConfServerStateProvider.getInst().getServerState(entry.getValue()));
			}
			mapState.put(GameModuleConst.MODULE_PRI, ConfServerStateProvider.getInst().getServerState(priNum));
		} else {
			for (GameType type : GameType.values()) {
				if(moduleId == type.getModule()){
					int num = gameMap.get(type).getPlayerCount();
					mapState.put(type.getValue(), ConfServerStateProvider.getInst().getServerState(num));
				}
			}
		}
		return mapState;
	}

	public void playerEnterModule(Player player, int moduleId) {
		if (moduleId == 1) {
			lobbyPlayerMap.put(player.getPlayerId(), player);
		} else {
			roomPlayerMap.put(player.getPlayerId(), player);
		}
		player.setModuleId(moduleId);
	}

	public void playerExitModule(Player player) {
		if (player.getModuleId() == 1) {
			lobbyPlayerMap.remove(player.getPlayerId());
		} else {
			roomPlayerMap.remove(player.getPlayerId());
		}
		player.setModuleId(0);
	}

	public void playerEnterRoom(Player player,int gameId, int roomId) {
//		int gameId = player.getGameId();
		GameType gameType = GameType.getByValue(gameId);
		if (gameType == null) {
			return;
		}
		GameRoomInterface facade = gameMap.get(gameType);
		if (facade == null) {
			return;
		}

		facade.enterGameRoom(player, roomId);
	}

	public void playerLeaveGameRoom(Player player) {
		int gameId =  player.getDeskOrMatchGameId();
		if (gameId != 0) {
			GameType gameType = GameType.getByValue(gameId);
			GameRoomInterface facade = gameMap.get(gameType);
			if (facade != null) {
				facade.leaveGameRoom(player);
			}
		}
	}

	public void reduceRoomNum(Player player) {
//		int roomId = player.getRoomId();
//		CoupleRoom roomConf = CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
//		if (roomConf == null) {
//			return;
//		}
		int gameId =  player.getDeskOrMatchGameId();
		GameType type = GameType.getByValue(gameId);
		if (type == null) {
			return;
		}
		GameRoomInterface facade = gameMap.get(type);
		if (facade != null) {
			facade.reduceRoomPlayerNum(player.getDeskOrMatchRoomId(),player.getPlayerId());
		}
	}

	public void saveHistoryPlayerNum() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (GameType type : GameType.values()) {
			result.addAll(getRoomPlayerList(type.getValue()));
		}
		String resultStr = JsonUtil.getGson().toJson(result, List.class).toString();
		Map<String, Object> data = new HashMap<>();
		data.put("info", resultStr);
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		data.put("time", year + "-" + (month + 1) + "-" + day);
		try {
			DBUtil.executeInsert("history_info", data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


//	public void onPlayerLogout(Player player) {
//		int gameId = player.getGameId();
//		GameType type = GameType.getByValue(gameId);
//		if (type == null) {
//			return;
//		}
//		GameRoomInterface facade = gameMap.get(type);
//		if (facade != null) {
//			facade.onPlayerLogout(player);
//		}
//	}
	
	public List<Player> getRoomPlayerInfoList(int gameId) {
		List<Player> playerList = new ArrayList<Player>();
		GameType gameType = GameType.getByValue(gameId);
		GameRoomInterface facade = gameMap.get(gameType);
		if (facade != null) {
			Set<Integer> playerIds = facade.getPlayerIds(gameId);
			if(playerIds != null){
				for (Integer playerId : playerIds) {
					Player player = PlayerManager.getInstance().getPlayerById(playerId);
					if(player != null){
						playerList.add(player);
					}
				}
			}
		}
		return playerList;
	}
	
	public void playerLeavePriRoom(GameType gameType,Player player){
		if(gameType == null){
			return;
		}
		Set<Integer> set = priRoomToPlayers.get(gameType);
		if(set.remove(player.getPlayerId())){
			player.updateLoginStatus(LoginStatusConst.ENTER_HALL);
		}
	}
	
	public void playerEnterPriRoom(GameType gameType,Player player){
		if(gameType == null){
			return;
		}
		Set<Integer> set = priRoomToPlayers.get(gameType);
		set.add(player.getPlayerId());
		player.updateLoginStatus(LoginStatusConst.AT_PRI_GAME_ING + gameType.getValue());
	}
	
	public Integer getPriRoomPlayerNum(GameType gameType) {
		Set<Integer> set = priRoomToPlayers.get(gameType);
		return set.size();
	}
}
