package logic.room;

import chr.Player;
import define.GameType;

/**
 * Created by hhhh on 2017/3/24.
 */
public class ZjhRoomFacade extends RoomFacade {
	@Override
	protected int getGameNeedPeople() {
		return 2;
	}

	@Override
	protected GameType getGameType() {
		return GameType.ZJH;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}

}
