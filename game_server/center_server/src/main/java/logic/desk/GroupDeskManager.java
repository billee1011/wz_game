package logic.desk;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import define.GameType;
import util.Randomizer;

/**
 * 基于桌子管理 当桌子上未满的时候加入桌子
 * 
 * @author admin
 *
 */
public class GroupDeskManager {
	private static Logger logger = LoggerFactory.getLogger(GroupDeskManager.class);

	/*** roomId,deskId,Desk **/
	private Map<Integer, Map<Integer, DeskInfo>> roomFulldeskMap = new HashMap<>(); // 人数满了的桌子

	private Map<Integer, Map<Integer, DeskInfo>> roomNoFullDeskMap = new HashMap<>(); // 未满的桌子

	private static GroupDeskManager ins = new GroupDeskManager();

	public static GroupDeskManager getIns() {
		return ins;
	}

	private Map<Integer, DeskInfo> getNoFullDeskMap(int roomId) {
		Map<Integer, DeskInfo> map = roomNoFullDeskMap.get(roomId);
		if (map == null) {
			map = new HashMap<>();
			roomNoFullDeskMap.put(roomId, map);
		}
		return map;
	}

	private Map<Integer, DeskInfo> getFullDeskMap(int roomId) {
		Map<Integer, DeskInfo> map = roomFulldeskMap.get(roomId);
		if (map == null) {
			map = new HashMap<>();
			roomFulldeskMap.put(roomId, map);
		}
		return map;
	}

	/** 玩家寻找和匹配桌子 当桌子未满的时候加入桌子 */
	public void matchingDesk(Player player, GameType gameType, int roomId) {
		Map<Integer, DeskInfo> noFullDeskMap = getNoFullDeskMap(roomId);
		Map<Integer, DeskInfo> fullDeskMap = getFullDeskMap(roomId);

		// 从未满的列表中随机获取一个桌子加入
		DeskInfo desk = null;

		if (noFullDeskMap.size() == 0) {
			// 需要创建一个桌子
			desk = DeskManager.getInst().getNorPrivateDesk(gameType);
			desk.setGameId(gameType.getValue());
			desk.setRoomId(roomId);
			noFullDeskMap.put(desk.getDeskId(), desk);
		} else {
			// 先拿所有的键：
			Integer[] keys = noFullDeskMap.keySet().toArray(new Integer[0]);
			// 然後随机一个键，找出该值：
			Integer randomKey = keys[Randomizer.nextInt(keys.length)];
			desk = noFullDeskMap.get(randomKey);
		}

		if (desk != null) {
			((GroupDesk)desk).addPlayer(player,roomId);
			logger.info("玩家 {} 进入到 " + desk.getGameName() + " 房间 {} ,当前玩家个数 {}", player.getPlayerId(), desk.getDeskId(),
					desk.getSize());
			// 若桌子满了，则将桌子从list中移除,添加到
			if (desk.isFull()) {
				noFullDeskMap.remove(desk.getDeskId());
				fullDeskMap.put(desk.getDeskId(), desk);
			}
		}
	}
	
	/** 玩家退出清理桌子信息 */
	public void onPlayerLeaveDesk(Player p,DeskInfo desk,boolean isCreate) {
		if(desk instanceof GroupDesk){
			Map<Integer, DeskInfo> noFullDeskMap = getNoFullDeskMap(desk.getRoomId());
			Map<Integer, DeskInfo> fullDeskMap = getFullDeskMap(desk.getRoomId());
			
			synchronized (desk) {
				// 移除并加入到未满的桌子
				if(fullDeskMap.remove(desk.getDeskId()) != null){
					noFullDeskMap.put(desk.getDeskId(), desk);
				}
			
				logger.info("玩家 {} 离开 " + desk.getGameName() + " 房间 {} ,剩余玩家个数 {}", p.getPlayerId(), desk.getDeskId(),
						desk.getSize());
				
				// 若该桌子玩家个数为0,移除桌子
				if(!isCreate){
					if (desk.getSize() == 0) {
						logger.info("移除未满 " + desk.getGameName() + " 房间 {}", desk.getDeskId());
						noFullDeskMap.remove(desk.getDeskId());
						DeskManager.getInst().removeDesk(desk);
					}
				}
			}
		}
	}
	
	public void removeDesk(DeskInfo desk){
		if(desk instanceof GroupDesk){
			logger.info("移除 " + desk.getGameName() + " 房间 {}", desk.getDeskId());
			
			Map<Integer, DeskInfo> noFullDeskMap = getNoFullDeskMap(desk.getRoomId());
			Map<Integer, DeskInfo> fullDeskMap = getFullDeskMap(desk.getRoomId());
			
			noFullDeskMap.remove(desk.getDeskId());
			fullDeskMap.remove(desk.getDeskId());
		}
	}
	
	/** 此桌子所在服务器已经要关闭了 */
	public void deskStop(GroupDesk desk){
		Map<Integer, DeskInfo> noFullDeskMap = getNoFullDeskMap(desk.getRoomId());
		Map<Integer, DeskInfo> fullDeskMap = getFullDeskMap(desk.getRoomId());
		
		noFullDeskMap.remove(desk.getDeskId());
		fullDeskMap.remove(desk.getDeskId());
	}
}
