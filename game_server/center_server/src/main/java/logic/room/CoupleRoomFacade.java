package logic.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import define.GameType;
import logic.CenterConst;

/**
 * Created by Administrator on 2016/12/13.
 */
public class CoupleRoomFacade extends RoomFacade {

	private static Logger logger = LoggerFactory.getLogger(CoupleRoomFacade.class);

	@Override
	protected int getGameNeedPeople() {
		return CenterConst.COUPLE_MJ_NEED_PEOPLE;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {
		// do nothing
	}


	@Override
	protected GameType getGameType() {
		return GameType.COUPLE_MJ;
	}

}
