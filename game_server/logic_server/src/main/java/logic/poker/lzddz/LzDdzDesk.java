package logic.poker.lzddz;

import java.util.List;

import logic.define.GameType;
import logic.majiong.PlayerInfo;
import logic.poker.PokerUtil;
import logic.poker.ddz.DdzDesk;
import logic.poker.ddz.DdzGroup;
import logic.poker.ddz.DdzGroupType;

/**
 * Created by think on 2017/4/15.
 */
public class LzDdzDesk extends DdzDesk {
	public LzDdzDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId, playerList);
	}

	@Override
	public GameType getGameType() {
		return GameType.LZ_DDZ;
	}


	//癞子斗地主， 当出的是炸弹额时候需要特别的处理
	@Override
	protected boolean isBiggerThanDesk(DdzGroup src, DdzGroup target) {
		if (target.getType() == DdzGroupType.FOUR && src.getType() == DdzGroupType.FOUR) {
			//这个位置还要处理一个癞子炸
			if (PokerUtil.isLzBonus(src.getCardList(), lzValue)) {
				return true;
			}
			if (PokerUtil.isLzBonus(target.getCardList(), lzValue)) {
				return false;
			}
			LzDdzBonusType bonusType = LzDdzBonusType.getByValue(PokerUtil.getFourBonusYingOrRuan(src.getCardList(), lzValue)
					, PokerUtil.getFourBonusYingOrRuan(target.getCardList(), lzValue));
			switch (bonusType) {
				case YING_RUAN:
					return true;
				case RUAN_YING:
					return false;
				case RUAN_RUAN:
				case YING_YING:
					return PokerUtil.isBigger(src.getType(), src.getCardDesc(), target.getCardDesc());
				default:
					return false;

			}
		} else {
			return super.isBiggerThanDesk(src, target);
		}
	}
}