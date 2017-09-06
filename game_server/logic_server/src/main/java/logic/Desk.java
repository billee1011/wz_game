package logic;

import java.util.List;

import logic.define.GameType;
import logic.majiong.PlayerInfo;
import service.LogicApp;
import util.MiscUtil;

/**
 * Created by Administrator on 2016/12/12.
 */
public interface Desk {

	int getDeskId();

	List<PlayerInfo> getPlayerList();

	GameType getGameType();

	boolean isGameing();

	void playerLogout(PlayerInfo player);

	void playerLeave(PlayerInfo player);

	boolean isAllPlayerLeave();

	default PlayerInfo getPlayerInfo(int playerId) {
		for (PlayerInfo info : getPlayerList()) {
			if (info.getPlayerId() == playerId) {
				return info;
			}
		}
		return null;
	}

	void playerReady(PlayerInfo player);

	void playerReLogin(PlayerInfo player);

	boolean isPlayerLeave(PlayerInfo player);

	int getCreateTime();

	boolean isTiyan();

	void playerMoneyChange(int playerId, int coin, boolean add);

	default long geneGameNo() {
		long currentTime = MiscUtil.getCurrentSeconds();
		int gameType = getGameType().getValue();
		int gameCount = getGameType().getGameCount();
		int gameSever = gameType * 1000  + LogicApp.getInst().getClient().getServerId();
		long gameId =  gameSever * 10000000000000L + currentTime * 1000 + gameCount % 1000;
		//10000000000000  old
		//9223372036854775807  long 
		//40011498288886001 now 支持9999serverId 应该不可能一直不关center只增加logic
		return gameId;
	}

	void recordPlayerTaxInfo(Object detail, int roomId);

	boolean isPersonal(); // 是否私房
	
	/** 桌子销毁，定时器等，停止 */
	void destroy();
	
    int getConfId();

	void playerWantContinue(PlayerInfo playerInfo);
}
