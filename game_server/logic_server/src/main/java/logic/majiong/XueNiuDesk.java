package logic.majiong;

import actor.LogicActorManager;
import define.DealZimo;
import logic.define.GameType;
import logic.majiong.define.MJPosition;

import java.util.List;

/**
 * Created by Administrator on 2016/12/13.
 */
public class XueNiuDesk extends SCMJDesk {
	//是否呼叫转移

	public XueNiuDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId, playerList);

	}

	public XueNiuDesk(int creatorId, int maxRounds, int deskId, List<PlayerInfo> playerList, boolean change, boolean trans
			, DealZimo zimoType, boolean diangangMo, int maxFan, List<Integer> extraList, int baseScore, int enterTimes) {
		/// 传递的confId要修改
		super(deskId,extraList, zimoType, trans, maxFan, creatorId, maxRounds, change, baseScore, true, playerList, enterTimes, diangangMo);
		this.deskId = deskId;
		// 私房一天后解散自己
		if (isPersonal()) {
			overMyself = LogicActorManager.registerOneTimeTask(GameConst.PERSONEL_OVER_MYSELF_TIME, () -> disBankDeskTimeOver(), getDeskId());
		}
	}


	@Override
	public int getDeskId() {
		return this.deskId;
	}

	@Override
	public GameType getGameType() {
		return GameType.XUELIU;
	}

}
