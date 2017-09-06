package logic.room;

import chr.Player;
import define.GameType;

/**
 * Created by Administrator on 2017/3/15.
 */
public class DdzRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return 3;
	}

	@Override
	protected GameType getGameType() {
		return GameType.DDZ;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}

}
