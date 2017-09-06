package logic.room;

import chr.Player;
import define.GameType;

/**
 * Created by think on 2017/4/15.
 */
public class CoupleDdzRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return 2;
	}

	@Override
	protected GameType getGameType() {
		return GameType.COUPLE_DDZ;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}
}
