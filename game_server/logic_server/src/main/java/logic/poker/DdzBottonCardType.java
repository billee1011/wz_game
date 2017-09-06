package logic.poker;

import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/15.
 */
public enum DdzBottonCardType {
	SANPAI(1, 1),
	DUI(2, 2),
	SINGLE_JOKER(3, 2),
	TONGHUA(4, 2),
	BAOZI(5, 3),
	SHUNZI(6, 3),
	DOUBLE_JOKER(7, 3);

	private int id;

	private int bonus;

	DdzBottonCardType(int id, int bonus) {
		this.id = id;
		this.bonus = bonus;
	}

	public int getId() {
		return id;
	}

	public int getBonus() {
		return bonus;
	}

	/** 底牌倍数 */
	public static int getBottomBonus(List<PokerCard> cardList) {
		cardList.sort(PokerUtil.POKER_COMPARATOR);
		if (check7(cardList)) {
			return DOUBLE_JOKER.getBonus();
		}
		if (check6(cardList)) {
			return SHUNZI.getBonus();
		}
		if (check5(cardList)) {
			return BAOZI.getBonus();
		}
		if (check4(cardList)) {
			return TONGHUA.getBonus();
		}
		if (check3(cardList)) {
			return SINGLE_JOKER.getBonus();
		}
		if (check2(cardList)) {
			return DUI.getBonus();
		}
		return SANPAI.getBonus();
	}

	/** 是否两张王 */
	private static boolean check7(List<PokerCard> cardList) {
		if (cardList.contains(PokerCard.JOKER_ONE) &&
				cardList.contains(PokerCard.JOKER_TWO)) {
			return true;
		}
		return false;
	}

	/** 是否顺子 */
	private static boolean check6(List<PokerCard> cardList) {
		return PokerUtil.checkStraight(cardList);
	}

	/** 是否豹子 三张数字一样 */
	private static boolean check5(List<PokerCard> cardList) {
		Map<Integer, Integer> countMap = PokerUtil.getAllPokerCount(cardList);
		return countMap.size() == 1;
	}

	/** 是否同花色 */
	private static boolean check4(List<PokerCard> cardList) {
		if (cardList.size() != 3) {
			return false;
		}
		return cardList.get(0).getPokerType() == cardList.get(1).getPokerType() &&
				cardList.get(0).getPokerType() == cardList.get(2).getPokerType();

	}

	/** 是否包含王 */
	private static boolean check3(List<PokerCard> cardList) {
		return cardList.contains(PokerCard.JOKER_TWO) || cardList.contains(PokerCard.JOKER_ONE);
	}
	
	/** 三张中是否有对子 */
	private static boolean check2(List<PokerCard> cardList) {
		Map<Integer, Integer> countMap = PokerUtil.getAllPokerCount(cardList);
		return countMap.size() == 2;
	}
}
