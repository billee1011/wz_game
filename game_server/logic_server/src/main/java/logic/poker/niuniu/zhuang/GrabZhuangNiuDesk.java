package logic.poker.niuniu.zhuang;

import java.util.List;

import logic.define.GameType;
import logic.majiong.PlayerInfo;

public class GrabZhuangNiuDesk  extends ZhuangNiuDesk{

	public GrabZhuangNiuDesk(int deskId, int roomId, List<PlayerInfo> playerList) {
		super(deskId, roomId, playerList,2,NiuNiuZhuangType.GrabZhuang);
	}
	
    @Override
    public GameType getGameType() {
        return GameType.GRAB_NIU;
    }
	
	@Override
	public String getGameName() {
		return "抢庄牛";
	}
}
