package logic.room;

import java.util.List;
import java.util.Set;

import chr.Player;
import util.Pair;

/**
 * Created by Administrator on 2016/12/12.
 */
public interface GameRoomInterface {

	public void enterGame(Player player);

	public void enterGameRoom(Player player, int roomId);

	public void leaveGame(Player player);

	public void leaveGameRoom(Player player);

	public void initGameRoom();
	
	public void addGameRoom();

	public int addRoomPlayerNum(int roomId,int playerId);

	public int reduceRoomPlayerNum(int roomId,int playerId);

	public void onPlayerEnterQueue(int roomId);

	public void onPlayerLogout(Player player);

	public void removeDesk(int deskId);

	int getPlayerCount();

	public List<Pair<Integer, Integer>> getRoomPlayerList();

	public List<Pair<Integer, Integer>> getRoomPlayerList_ex();

	public List<Pair<Integer, Integer>> getRoomStatusList();
	
	public Set<Integer> getPlayerIds(int roomId);

	/**
	 * 判断是否在房间中
	 * @param roomId
	 * @param playerId
	 * @return
	 */
	public boolean isInRoom(int roomId, int playerId);
}
