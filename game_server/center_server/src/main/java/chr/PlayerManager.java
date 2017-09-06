package chr;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.data.DataUtil;
import define.constant.LoginStatusConst;

public class PlayerManager {
	private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);
	private static PlayerManager ourInstance = new PlayerManager();
	
	private Object lock = new Object();

	public static PlayerManager getInstance() {
		return ourInstance;
	}

	private PlayerManager() {
	}

	private Map<Integer, Player> playerMap = new ConcurrentHashMap<>();
	
	private Map<Integer, Player> onlinePlayerMap = new ConcurrentHashMap<>();

	private Map<String, Player> name2Player = new ConcurrentHashMap<>();

	public void registerPlayer(Player player) {
		synchronized (lock) {
			if (onlinePlayerMap.get(player.getPlayerId()) != null) {
				return;
			}
			if(!playerMap.containsKey(player.getPlayerId())){
				playerMap.put(player.getPlayerId(), player);
			}
			onlinePlayerMap.put(player.getPlayerId(), player);
			name2Player.put(player.getName(), player);
		}
	}

	public void removePlayer(Player player) {
		synchronized (lock) {
			onlinePlayerMap.remove(player.getPlayerId());
			name2Player.remove(player.getName());
		}
	}

	public Collection<Player> getAllPlayers() {
		synchronized (lock) {
			return new ArrayList<>(playerMap.values());
		}
	}

	public Collection<Player> getOnlinePlayers() {
		synchronized (lock) {
			return new ArrayList<>(onlinePlayerMap.values());
		}
	}

	public int getPlayerCount(){
		return this.onlinePlayerMap.size();
	}
	
	public boolean isOnline(int playerId){
		return onlinePlayerMap.containsKey(playerId);
	}


	public void saveAllPlayers(int internal,int removeTime) {
		long removeTimeMs = removeTime * 1000;
		Collection<Player> players = playerMap.values();
		long currentTime = System.currentTimeMillis();
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player player = iter.next();
			if (player == null) {
				continue;
			}
			
			long crc32 = player.crc32;
			player.crc32 = 0;
			if (DataUtil.crc32(player) == crc32) {
				player.crc32 = crc32;
				if (player.getLoginStatus() == LoginStatusConst.EXIT_GAME
						&& (currentTime - player.getLastSaveTime()) > removeTimeMs) {
					iter.remove();
					logger.info("玩家{}最后一次保存时间为{},{}秒后从缓存中移除", player.getPlayerId(), player.getLastSaveTime(),
							removeTime / 1000);
				}
				continue;
			}
			if (currentTime - player.getLastSaveTime() > internal) {
				PlayerSaver.savePlayer(player);
				player.setLastSaveTime(currentTime);
				player.crc32 = DataUtil.crc32(player);
			}
		}
	}

	public Player getPlayerById(int playerId) {
		synchronized (lock) {
			Player player = playerMap.get(playerId);
			if (player != null) {
				return player;
			}
			// 从db取，加载到map中
			logger.info("从数据库加载玩家{}", playerId);
			long startTime = System.currentTimeMillis();
			player = PlayerLoader.loadPlayer(playerId);
			if (player != null) {
				playerMap.put(playerId, player);
				logger.info("查询{}消耗{}ms", playerId, System.currentTimeMillis() - startTime);
			}
			return player;
		}
	}

	public Player getPlayerByName(String name) {
		return name2Player.get(name);
	}

//	public void loadAllPlayer() {
//		long startTime = System.currentTimeMillis();
//		String sql = "SELECT player_id FROM player";
//		List<ASObject> playerId_list = DataQueryResult.load(sql);
//		logger.info("准备从数据库加载{}个玩家",playerId_list.size());
//		CountDownLatch downLatch = new CountDownLatch(playerId_list.size());
//		for (ASObject obj : playerId_list) {
//			//从数据库加载
//			int playerId = obj.getInt("player_id");
//			DataManager.getInst().loadChar(playerId, (e, f) -> {
//				if (e != null) {
//					CharData charData = (CharData) e;
//					Player dbPlayer = Player.getDefault();
//					PlayerLoader.loadFromCharData(charData, dbPlayer);
//					cachePlayerMap.put(playerId, dbPlayer);
//					downLatch.countDown();
//				}
//			});
//		}
//		try {
//			downLatch.await();
//		} catch (InterruptedException e) {
//			LogUtil.error(logger, e);
//		}
//		logger.info("玩家{}加载完成,用时{}ms",playerId_list.size(),System.currentTimeMillis() - startTime);
//	}
}
