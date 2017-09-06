package logic.room;

import chr.Player;
import define.GameType;

/**
 * Created by think on 2017/4/15.
 */
public class LzDdzRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return 3;
	}

	@Override
	protected GameType getGameType() {
		return GameType.LZ_DDZ;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}
}
