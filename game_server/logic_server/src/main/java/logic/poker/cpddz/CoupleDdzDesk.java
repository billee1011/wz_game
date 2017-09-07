package logic.poker.cpddz;

import logic.define.GameType;
import logic.majiong.PlayerInfo;
import logic.poker.ddz.DdzDesk;
import logic.poker.ddz.DdzDeskInfo;
import logic.poker.ddz.DdzPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.creator.CommonCreator;
import protocol.s2c.ResponseCode;

import java.util.List;

/**
 * Created by think on 2017/4/15.
 */
public class CoupleDdzDesk extends DdzDesk {
	private static final Logger logger = LoggerFactory.getLogger(CoupleDdzDesk.class);
	public CoupleDdzDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId, playerList);
	}


	@Override
	protected DdzPos nextPos() {
		if (currentPos == DdzPos.ONE) {
			return DdzPos.TWO;
		} else {
			return DdzPos.ONE;
		}
	}

	@Override
	protected boolean isDiscardAll(DdzDeskInfo info) {
		if (info.isLord()) {
			return info.getHandCards().size() == 0;
		} else {
			return info.getHandCards().size() <= getRobLordTimes();
		}
	}

	//覆盖一些二人斗地主抢地主的规则
	@Override
	protected void doRobLord(PlayerInfo player, boolean rob) {
		logger.info("{} | {} | 玩家 {} 抢地主 {}", this.deskId, this.getGameId(), player.getPlayerId(), rob);
		if (rob) {
			getDeskInfo(player).addRobLordTimes();
			msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), 1, callLord ? 1 : 0));
			if (getPlayerPos(player) == callPos && getDeskInfo(player).getRobTimes() == 2) {
				dealLordCard(player);
			} else {
				currentPos = nextPos();
				beginChoseLord();
			}
		} else {
			msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), 0, callLord ? 1 : 0));
			//二人你不强就直接开始了啊
			currentPos = nextPos();
			dealLordCard(getPlayerInfoByPos(currentPos));
		}
	}

	@Override
	public GameType getGameType() {
		return GameType.COUPLE_DDZ;
	}
}
