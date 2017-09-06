package logic.room;

import chr.Player;
import define.GameType;
import logic.CenterConst;

/**
 * Created by Administrator on 2016/12/13.
 */
public class XueNiuRoomFacade extends RoomFacade {

	@Override
	protected int getGameNeedPeople() {
		return CenterConst.FOUR_MJ_NEED_PEOPLE;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {
		// do nothing
	}


	@Override
	protected GameType getGameType() {
		return GameType.XUENIU;
	}

}
