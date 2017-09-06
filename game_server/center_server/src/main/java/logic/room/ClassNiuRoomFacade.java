package logic.room;

import chr.Player;
import define.GameType;


public class ClassNiuRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return 2;
	}

	@Override
	protected GameType getGameType() {
		return GameType.CLASS_NIU;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {

	}

}
