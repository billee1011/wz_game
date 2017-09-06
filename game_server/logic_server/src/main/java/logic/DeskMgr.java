package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.majiong.PlayerInfo;
import packet.CocoPacket;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import service.LogicApp;

/**
 * Created by Administrator on 2017/2/7.
 */
public class DeskMgr {
	private static Logger logger = LoggerFactory.getLogger(DeskMgr.class);
	private static DeskMgr instance = new DeskMgr();

	private DeskMgr() {

	}

	public static DeskMgr getInst() {
		return instance;
	}

	private Lock lock = new ReentrantLock();// 锁

	private Map<Integer, Desk> deskMap = new HashMap<>();

//	private NiuNiuDesk niuNiuDesk;

	private Map<Integer, Desk> player2DeskMap = new HashMap<>();

	public void registerDesk(Desk desk) {
		lock.lock();
		try {
			List<PlayerInfo> playerList = desk.getPlayerList();
//			if (playerList == null) {
//				logger.debug(" the player list is null in register desk ");
//				return;
//			}
			playerList.forEach(e -> player2DeskMap.put(e.getPlayerId(), desk));
			deskMap.put(desk.getDeskId(), desk);
			logger.info("創建桌子{},当前存在桌子数:{}", desk.getDeskId(), deskMap.size());
		} finally {
			lock.unlock();
		}
	}

	public void registerDesk(int playerId, Desk desk) {
		lock.lock();
		try {
			player2DeskMap.put(playerId, desk);
			logger.info("玩家{}加入桌子{}", playerId, desk.getDeskId());
		} finally {
			lock.unlock();
		}
	}

	public void removeDesk(Desk desk) {
		lock.lock();
		try {
			List<PlayerInfo> playerList = desk.getPlayerList();
			if (playerList == null) {
				logger.debug(" the player list is null in remoce desk ");
				return;
			}
			playerList.forEach(e -> player2DeskMap.remove(e.getPlayerId(), desk));
            if (deskMap.containsKey(desk.getDeskId())) {
            	desk.destroy();
                deskMap.remove(desk.getDeskId());
                LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_ROOM_DESK_DISBAND, CommonCreator.createPBInt32(desk.getDeskId())));
                logger.info("删除桌子{},当前存在桌子数:{}", desk.getDeskId(), deskMap.size());
            }
        } finally {
			lock.unlock();
		}
	}

	public void removePlayer(PlayerInfo player,boolean afterRemoveDesk){
		lock.lock();
		try {
			Desk desk = getDeskByPlayerId(player.getPlayerId());
            if (desk != null) {
                player2DeskMap.remove(player.getPlayerId(), desk);
                if(!afterRemoveDesk){
                	LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, player.getPlayerId()));
                }
                logger.info("玩家{}从桌子{}移除", player.getPlayerId(), desk.getDeskId());
            }
        } finally {
			lock.unlock();
		}
	}

	public Desk getDeskByDeskId(int deskId) {
		return deskMap.get(deskId);
	}

	public Desk getDeskByPlayerId(int playerId) {
		return player2DeskMap.get(playerId);
	}
	
//	public NiuNiuDesk getNiuNiuDesk() {
//		return niuNiuDesk;
//	}
//
//	public void setNiuNiuDesk(NiuNiuDesk niuNiuDesk) {
//		this.niuNiuDesk = niuNiuDesk;
//	}

	/** 后台强制刪除桌子 */
	public void removeDeskByGm(Desk desk) {
		lock.lock();
		try{
			List<PlayerInfo> playerList = desk.getPlayerList();
			if (playerList != null) {
				playerList.forEach(e -> player2DeskMap.remove(e.getPlayerId()));
			}
			deskMap.remove(desk.getDeskId());
			desk.destroy();
//			if(niuNiuDesk != null && niuNiuDesk.getDeskId() == desk.getDeskId()){
//				niuNiuDesk = null;
//			}
			logger.info("GM删除桌子{},当前存在桌子数:{}", desk.getDeskId(), deskMap.size());
		}finally {
			lock.unlock();
		}
	}

	public void onStopServer() {
		lock.lock();
		List<Desk> deskList = new ArrayList<Desk>(deskMap.values());
		lock.unlock();
		
		for (Desk deskI : deskList) {
			try {
				AbstractDesk deskA = (AbstractDesk) deskI;
				if(deskA.isPersonal()){
					deskA.disbandDesk();
				}else{
					removeDesk(deskI);
				}
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}
	
	
}
