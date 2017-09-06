package logic.poker.ddz;

import logic.poker.PokerCard;

import java.util.List;

/**
 * Created by Administrator on 2017/3/14.
 */
public class DdzGroup {
	private DdzGroupType type;
	private List<PokerCard> cardList;
	private List<Integer> cardDesc;            //牌的简写 对应type不同字段不同含义

	public DdzGroup(DdzGroupType type, List<PokerCard> cardList, List<Integer> descList) {
		this.type = type;
		this.cardList = cardList;
		this.cardDesc = descList;
	}

	public DdzGroupType getType() {
		return type;
	}


	public List<Integer> getCardDesc() {
		return cardDesc;
	}

	public void setCardDesc(List<Integer> cardDesc) {
		this.cardDesc = cardDesc;
	}

	public void setType(DdzGroupType type) {
		this.type = type;
	}

	public List<PokerCard> getCardList() {
		return cardList;
	}

	public void setCardList(List<PokerCard> cardList) {
		this.cardList = cardList;
	}

}
