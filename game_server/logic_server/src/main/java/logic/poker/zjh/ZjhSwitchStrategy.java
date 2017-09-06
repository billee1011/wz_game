package logic.poker.zjh;

import logic.poker.PokerCard;
import logic.poker.PokerUtil;

import java.util.*;

/**
 * Created by hhhh on 2017/3/24.
 */
public class ZjhSwitchStrategy {
	
	/**
	 * 获取比较值
	 * @param list
	 * @return
	 */
	private static List<Integer> getValueList(List<PokerCard> list) {
		return getList(list, true);
	}

	/**
	 * 获取比较类
	 * @param list
	 * @return
	 */
	private static List<Integer> getTypeList(List<PokerCard> list) {
		return getList(list, false);
	}

	private static List<Integer> getList(List<PokerCard> list, boolean isValue) {
		List<Integer> returnList = new ArrayList<>();
		for (int i = list.size() - 1; i >= 0; i--) {
			PokerCard card = list.get(i);
			if (isValue) {
				returnList.add(card.getZjhPokerValue());
			} else {
				returnList.add(card.getPokerType().getValue());
			}
		}
		return returnList;
	}
	
	/**
	 * 带A的顺子排序
	 * @param cardList
	 */
	private static void sortStralghtA(List<PokerCard> cardList) {
		boolean isBig = false;
		boolean hasA = false;
		for (PokerCard card : cardList){
			if (card.getPokerValue() == 1){
				hasA = true;
			}
			// Q K A 为大顺
			if (card.getPokerValue() == 12 || card.getPokerValue() == 13){
				isBig = true;
			}
		}
		if (!hasA){
			return;
		}
		if (isBig) {
			Collections.sort(cardList, PokerUtil.POKER_ZJH_A);
		} else {
			Collections.sort(cardList, PokerUtil.POKER_ZJH);
		}
	}
	
	//豹子
	public static ZjhCompareStrategy leopard(List<PokerCard> cardList){
		if (PokerUtil.getPokerCount(cardList, cardList.get(0).getPokerValue()) != 3) {
			return null;
		}
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.LEOPARD,Arrays.asList(cardList.get(2).getZjhPokerValue()), null);
		return strategy;
	}

	//顺金
	public static ZjhCompareStrategy straightGold(List<PokerCard> cardList) {
		Collections.sort(cardList, PokerUtil.POKER_ZJH);
		if (!PokerUtil.checkZjhStraight(cardList)) {
			return null;
		}
		if (!PokerUtil.checkPokerColorIsTheSame(cardList)) {
			return null;
		}
		sortStralghtA(cardList);
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.STRAIGHTGOLD, Arrays.asList(cardList.get(2).getZjhPokerValue()), getTypeList(cardList));
		return strategy;
	}
	
	//金花
	public static ZjhCompareStrategy goleFlower(List<PokerCard> cardList) {
		Collections.sort(cardList, PokerUtil.POKER_ZJH);
		if (!PokerUtil.checkPokerColorIsTheSame(cardList)) {
			return null;
		}
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.GOLDFLOWER, getValueList(cardList), getTypeList(cardList));
		return strategy;
	}
	
	//顺子
	public static ZjhCompareStrategy stralght(List<PokerCard> cardList){
		Collections.sort(cardList, PokerUtil.POKER_ZJH);
		if(!PokerUtil.checkZjhStraight(cardList)){
			return null;
		}
		sortStralghtA(cardList);
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.STRAIGHT, Arrays.asList(cardList.get(2).getZjhPokerValue()), getTypeList(cardList));
		return strategy;
	}
	
	// 对子
	public static ZjhCompareStrategy couple(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = PokerUtil.getZjhPokerCount(cardList);
		if (!cardCounts.containsValue(2)) {
			return null;
		}
		List<Integer> result = new ArrayList<>();
		List<Integer> type = new ArrayList<>();
		PokerUtil.addPokerValueByCount(cardCounts, result, 2);
		PokerUtil.addPokerValueByCount(cardCounts, result, 1);
		cardList.forEach(e -> {
			if (e.getPokerValue() == result.get(1)) {
				type.add(e.getPokerType().getValue());
			}
		});
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.COUPLE, result, type);
		return strategy;
	}

	//散牌
	public static ZjhCompareStrategy signle(List<PokerCard> cardList){
		Collections.sort(cardList, PokerUtil.POKER_ZJH_A);
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.SIGNLE, getValueList(cardList), getTypeList(cardList));
		return strategy;
	}

	//特殊牌型
	public static  ZjhCompareStrategy special(List<PokerCard> cardList) {
		if(!PokerUtil.checkSpeicialCard(cardList)){
			return null;
		}
		Collections.sort(cardList, PokerUtil.POKER_ZJH_A);
		ZjhCompareStrategy strategy = new ZjhCompareStrategy(ZjhGroupType.SPECIAL, getValueList(cardList), getTypeList(cardList));
		return strategy;
	}
}
