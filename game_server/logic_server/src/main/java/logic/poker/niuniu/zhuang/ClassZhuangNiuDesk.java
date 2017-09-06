package logic.poker.niuniu.zhuang;

import java.util.List;

import actor.LogicActorManager;
import logic.define.GameType;
import logic.majiong.GameConst;
import logic.majiong.PlayerInfo;

public class ClassZhuangNiuDesk extends ZhuangNiuDesk {

	public ClassZhuangNiuDesk(int deskId, int roomId, List<PlayerInfo> playerList) {
		super(deskId, roomId, playerList,1,NiuNiuZhuangType.GrabZhuang);
	}
	
	public ClassZhuangNiuDesk(int model,int creatorId, int maxRounds, int deskId, List<PlayerInfo> playerList, int baseScore, int enterTimes,int rule,int multi) {
		/// 传递的confId要修改
		super(deskId, creatorId, maxRounds, baseScore, true, playerList, enterTimes, rule, multi,model);
		// 私房一天后解散自己
		overMyself = LogicActorManager.registerOneTimeTask(GameConst.PERSONEL_OVER_MYSELF_TIME, () -> disBankDeskTimeOver(), getDeskId());
	}

    @Override
    public GameType getGameType() {
        return GameType.CLASS_NIU;
    }

	@Override
	public String getGameName() {
		return "经典牛牛";
	}
}
