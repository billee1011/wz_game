package logic.room;

import chr.Player;
import define.GameType;


public class GrabNiuRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return 2;
	}

	@Override
	protected GameType getGameType() {
		return GameType.GRAD_NIU;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}

}
