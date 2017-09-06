package logic.poker.ddz;

import config.bean.Poker;
import logic.poker.PokerCard;
import logic.poker.PokerUtil;

import java.util.*;

/**
 * Created by Administrator on 2017/3/14.
 */
public class DdzSwitchStrategy {
	public static List<Integer> single(List<PokerCard> cardList) {
		return Arrays.asList(cardList.get(0).getPokerValue());
	}

	public static List<Integer> couple(List<PokerCard> cardList) {
		if (PokerUtil.getPokerCount(cardList, cardList.get(0).getPokerValue()) != 2) {
			return null;
		}
		return Arrays.asList(cardList.get(0).getPokerValue());
	}

	public static List<Integer> triple(List<PokerCard> cardList) {
		if (PokerUtil.getPokerCount(cardList, cardList.get(0).getPokerValue()) != 3) {
			return null;
		}
		return Arrays.asList(cardList.get(0).getPokerValue());
	}

	public static List<Integer> four(List<PokerCard> cardList) {
		if (PokerUtil.getPokerCount(cardList, cardList.get(0).getPokerValue()) != 4) {
			return null;
		}
		return Arrays.asList(cardList.get(0).getPokerValue());
	}

	public static List<Integer> threeOne(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cardList);
		if (!cardCounts.containsValue(1) || !cardCounts.containsValue(3)) {
			return null;
		}
		List<Integer> result = new ArrayList<>();
		PokerUtil.addPokerValueByCount(cardCounts, result, 3);
		PokerUtil.addPokerValueByCount(cardCounts, result, 1);
		return result;
	}

	public static List<Integer> threeTwo(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cardList);
		if (!cardCounts.containsValue(2) || !cardCounts.containsValue(3)) {
			return null;
		}
		List<Integer> result = new ArrayList<>();
		PokerUtil.addPokerValueByCount(cardCounts, result, 3);
		PokerUtil.addPokerValueByCount(cardCounts, result, 2);
		return result;
	}

	//四个带两个
	public static List<Integer> fourTwoOne(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cardList);
		if(cardCounts.size() == 3){
			//四带两单
			if (!PokerUtil.checkPokerCount(cardCounts, 4, 1, 1)) {
				return null;
			}
			List<Integer> result = new ArrayList<>();
			PokerUtil.addPokerValueByCount(cardCounts, result, 4);
			PokerUtil.addPokerValueByCount(cardCounts, result, 1);
			PokerUtil.addPokerValueByCount(cardCounts, result, 1);
			return result;
		}else if(cardCounts.size() == 2){
			//四带一对
			if (!PokerUtil.checkPokerCount(cardCounts, 4, 2)) {
				return null;
			}
			List<Integer> result = new ArrayList<>();
			PokerUtil.addPokerValueByCount(cardCounts, result, 4);
			PokerUtil.addPokerValueByCount(cardCounts, result, 2);
			return result;
		}
		return null;
	}

	public static List<Integer> fourTwoTwo(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cardList);
		if (!PokerUtil.checkPokerCount(cardCounts, 4, 2, 2)) {
			return null;
		}
		cardList.forEach( e -> System.out.println(e));
		System.out.println("end");
		List<Integer> result = new ArrayList<>();
		PokerUtil.addPokerValueByCount(cardCounts, result, 4);
		PokerUtil.addPokerValueByCount(cardCounts, result, 2);
		PokerUtil.addPokerValueByCount(cardCounts, result, 2);
		return result;
	}

	public static List<Integer> straight5(List<PokerCard> cardList) {
		Collections.sort(cardList, PokerUtil.POKER_COMPARATOR);
		if (!PokerUtil.checkStraight(cardList)) {
			return null;
		}
		return Arrays.asList(cardList.get(0).getPokerValue());
	}

	//连队
	public static List<Integer> coupleThree(List<PokerCard> cardList) {
		Collections.sort(cardList, PokerUtil.POKER_COMPARATOR);
		if (!PokerUtil.checkCoupleStraight(cardList)) {
			return null;
		}
		return Arrays.asList(cardList.get(0).getPokerValue());
	}


	//王炸只需要标明王炸就好
	public static List<Integer> jokerBonus(List<PokerCard> cardList) {
		Collections.sort(cardList, PokerUtil.POKER_COMPARATOR);
		if (cardList.get(0) == PokerCard.JOKER_ONE && cardList.get(1) == PokerCard.JOKER_TWO) {
			return new ArrayList<>();
		}
		return null;
	}

	public static List<Integer> planeWithOne(List<PokerCard> cardList) {
		List<Integer> threeList = PokerUtil.getThreeCardAll(cardList);
		Collections.sort(threeList);
		if (!PokerUtil.checkNumStraight(threeList)) {
			return null;
		}
		return Arrays.asList(threeList.get(0));
	}


}
