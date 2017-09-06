package logic.poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import logic.debug.ArrayPai;
import logic.define.GameType;
import logic.majiong.GameConst;
import logic.majiong.GameUtil;
import logic.poker.ddz.DdzCardCompare;
import logic.poker.ddz.DdzGroupType;
import logic.poker.niuniu.NiuResult;
import util.Pair;

/**
 * Created by Administrator on 2016/12/13.
 */
public class PokerUtil {

	public static Comparator<PokerCard> POKER_COMPARATOR = Comparator.comparingInt(e -> e.getCompareValue());
	public static Comparator<PokerCard> POKER_ZJH = Comparator.comparingInt(e -> e.getPokerValue());
	public static Comparator<PokerCard> POKER_ZJH_A = (e, f) -> e.getZjhPokerValue() - f.getZjhPokerValue();
	public static Comparator<PokerCard> POKER_ZJH_TWO = (e, f) -> f.getZjhPokerValue() - e.getZjhPokerValue();

	public static boolean isBigger(DdzGroupType type, List<Integer> cardList, List<Integer> group) {
		return DdzCardCompare.isBigger(type, cardList, group);
	}

	public static void main(String[] args) {
		List<PokerCard> cardList = new ArrayList<>();
		cardList.add(PokerCard.FANG_7);
		cardList.add(PokerCard.FANG_7);
		cardList.add(PokerCard.FANG_7);
		cardList.add(PokerCard.FANG_8);
		cardList.add(PokerCard.FANG_8);
		cardList.add(PokerCard.FANG_8);
		cardList.add(PokerCard.FANG_8);
		cardList.add(PokerCard.FANG_9);
		System.out.println(filterDdzGroupType(DdzGroupType.getByCardNum(8), cardList, 0));
	}

	/** 找出牌类型和具体的牌数据 */
	public static Pair<DdzGroupType, List<Integer>> filterDdzGroupType(List<DdzGroupType> typeList, List<PokerCard> cardList, int lzValue) {
		if (lzValue > 0) {
			PokerCard card = PokerCard.getByValue(lzValue);
			if (card == null) {
				return null;
			}
			return filterDdzGroupTypeWithLz(typeList, cardList, card.getPokerValue());
		} else {
			for (DdzGroupType cardType : typeList) {
				List<Integer> card = DdzCardCompare.checkType(cardType, cardList);
				if (card != null) {
					return new Pair<>(cardType, card);
				}
			}
			return null;
		}
	}

	/** 找出所有类型中比2类型>=类型集合 */
	public static List<DdzGroupType> getDiscardGroupList(List<DdzGroupType> srcType, DdzGroupType type) {
		List<DdzGroupType> result = new ArrayList<>();
		if (srcType.contains(DdzGroupType.FOUR)) {
			result.add(DdzGroupType.FOUR);
		}
		if (srcType.contains(DdzGroupType.JOKER_BONUS)) {
			result.add(DdzGroupType.JOKER_BONUS);
		}
		if (srcType.contains(type) && !result.contains(type)) {
			result.add(type);
		}
		return result;
	}

	// return 1  if is ruan zha  return 2 if is ying zha
	public static int getFourBonusYingOrRuan(List<PokerCard> cardList, int lzValue) {
		PokerCard lzCard = PokerCard.getByValue(lzValue);
		if (lzCard == null) {
			return 1;
		}
		for (PokerCard card : cardList) {
			if (card.getPokerValue() == lzCard.getPokerValue()) {
				return 1;
			}
		}
		return 2;
	}

	public static boolean isLzBonus(List<PokerCard> cardList, int lzValue) {
		int lzCount = 0;
		for (PokerCard card : cardList) {
			if (card.getKey() == lzValue) {
				lzCount++;
			}
		}
		return lzCount == 4;
	}

	//当有多个癞子的时候简直就是日了狗啊
	public static Pair<DdzGroupType, List<Integer>> filterDdzGroupTypeWithLz(List<DdzGroupType> typeList, List<PokerCard> cardList, int lzValue) {
		Collections.sort(typeList, (e, f) -> e.getWeight() - f.getWeight());
		List<List<PokerCard>> compositeList = findAllCardComposite(cardList, lzValue);
		for (int i = 0, size = typeList.size(); i < size; i++) {
			if (compositeList == null) {
				//没有 癞子的情况下这样就完事了
				List<Integer> card = DdzCardCompare.checkType(typeList.get(i), cardList);
				if (card != null) {
					return new Pair<>(typeList.get(i), card);
				}
			} else {
				List<Pair<DdzGroupType, List<Integer>>> resultList = null;
				for (List<PokerCard> pokerCards : compositeList) {
					List<PokerCard> tempList = new ArrayList<>(cardList);
					tempList.addAll(pokerCards);
					List<Integer> card = DdzCardCompare.checkType(typeList.get(i), tempList);
					if (card != null) {
						if (resultList == null) {
							resultList = new ArrayList<>();
						}
						resultList.add(new Pair<>(typeList.get(i), card));
					}
				}
				if (resultList != null) {
					return resultList.stream().max(Comparator.comparingInt(e -> e.getRight().get(0))).get();
				}
			}
		}
		return null;
	}

	public static List<PokerCard> convert2PokerCard(List<Integer> pokerList) {
		List<PokerCard> result = new ArrayList<>();
		if (pokerList == null) {
			return null;
		}
		pokerList.forEach(e -> result.add(PokerCard.getByValue(e)));
		return result;
	}

	public static List<Integer> convert2IntList(List<PokerCard> cardList) {
		List<Integer> result = new ArrayList<>();
		if (cardList == null) {
			return null;
		}
		cardList.forEach(e -> result.add(e.getKey()));
		return result;
	}

	public static PokerCard choseTheSmallBiggerCard(List<PokerCard> cardList, int cardValue) {
		PokerCard result = null;
		for (PokerCard card : cardList) {
			if (card.getCompareValue() <= cardValue) {
				continue;
			}
			if (result == null) {
				result = card;
			}
			if (result.getCompareValue() > card.getCompareValue()) {
				result = card;
			}
		}
		return result;
	}


	public static boolean checkPlaneStraight(List<PokerCard> cardList) {
		int currentPokerValue = 0;
		for (int i = 0, size = cardList.size(); i < size; i++) {
			PokerCard card = cardList.get(i);
			if (card == null) {
				return false;
			}
			if (card.getPokerType() == PokerColorType.JOKER) {
				return false;
			}
			if (card.getPokerValue() == 2) {
				return false;
			}
			if (currentPokerValue == 0) {
				currentPokerValue = cardList.get(i).getCompareValue();
			}
			if (i % 2 != 0) {
				if (currentPokerValue != card.getPokerValue())
					return false;
				else
					continue;
			}
			if (card.getCompareValue() != currentPokerValue + 1)
				return false;
			else
				currentPokerValue++;
		}
		return true;
	}

	public static boolean checkNumStraight(List<Integer> nums) {
		if (nums.size() < 2) {
			return false;
		}
		int currentValue = 0;
		for (int i = 0, size = nums.size(); i < size; i++) {
			if (nums.get(i) == PokerCard.FANG_2.getPokerValue()) {
				return false;
			}
			if (currentValue == 0) {
				currentValue = nums.get(i);
			} else {
				if (nums.get(i) != currentValue + 1) {
					return false;
				}
				currentValue += 1;
			}
		}
		return true;
	}

	public static List<Integer> getThreeCardAll(List<PokerCard> cardList) {
		Map<Integer, Integer> cardCounts = getAllPokerCount(cardList);
		List<Integer> result = new ArrayList<>();
		cardCounts.entrySet().forEach(e -> {
			if (e.getValue() >= 3) {
				result.add(PokerUtil.getCompareValue(e.getKey()));
			}
		});
		return result;
	}


	//穷举所有可能的牌组
	private static List<List<PokerCard>> findAllCardComposite(List<PokerCard> srcList, int lzValue) {
		List<PokerCard> lzCardList = new ArrayList<>();
		for (int i = 0, size = srcList.size(); i < size; i++) {
			PokerCard currCard = srcList.get(i);
			if (isLzCard(currCard, lzValue)) {
				lzCardList.add(currCard);
			}
		}
		srcList.removeAll(lzCardList);                    // no src list is no longer have the lz card
		return getTotalLzComposite(lzCardList);
	}

	private static List<List<PokerCard>> getTotalLzComposite(List<PokerCard> lzList) {
		int lzCount = lzList.size();
		switch (lzCount) {
			case 1:
				return PokerCard.lzComposite1;
			case 2:
				return PokerCard.lzComposite2;
			case 3:
				return PokerCard.lzComposite3;
			case 4:
				return PokerCard.lzComposite4;
			default:
				return null;
		}
	}

	private static boolean isLzCard(PokerCard card, int lzValue) {
		return card.getPokerValue() == lzValue;
	}

	public static boolean checkCoupleStraight(List<PokerCard> cardList) {
		if (cardList.size() < 2) {
			return false;
		}
		int currentPokerValue = 0;
		for (int i = 0, size = cardList.size(); i < size; i++) {
			PokerCard card = cardList.get(i);
			if (card == null) {
				return false;
			}
			if (card.getPokerType() == PokerColorType.JOKER) {
				return false;
			}
			if (card.getPokerValue() == 2) {
				return false;
			}
			if (currentPokerValue == 0) {
				currentPokerValue = cardList.get(i).getCompareValue();
				continue;
			}
			if (i % 2 == 1) {
				if (currentPokerValue != card.getCompareValue())
					return false;
				else
					continue;
			} else {
				if (card.getCompareValue() != currentPokerValue + 1)
					return false;
				else
					currentPokerValue++;
			}
		}
		return true;
	}

	//找出牌中最小的牌
	public static PokerCard getMinValueCard(List<PokerCard> cardList) {
		PokerCard result = null;
		for (PokerCard card : cardList) {
			if (result == null || result.getCompareValue() > card.getCompareValue())
				result = card;
		}
		return result;
	}


	/**检查牌是否是顺子*/
	public static boolean checkStraight(List<PokerCard> cardList) {
		int currentPokerValue = 0;
		for (int i = 0, size = cardList.size(); i < size; i++) {
			PokerCard card = cardList.get(i);
			if (card == null) {
				return false;
			}
			if (card.getPokerType() == PokerColorType.JOKER) {
				return false;
			}
			if (card.getPokerValue() == 2) {
				return false;
			}
			if (currentPokerValue == 0) {
				currentPokerValue = cardList.get(i).getCompareValue();
				continue;
			}
			if (card.getCompareValue() != currentPokerValue + 1) {
				return false;
			} else {
				currentPokerValue++;
			}
		}
		return true;
	}

	public static void addPokerValueByCount(Map<Integer, Integer> cardList, List<Integer> result, int count) {
		cardList.entrySet().forEach(e -> {
			if (e.getValue() == count) {
				result.add(e.getKey());
			}
		});
	}

	public static int getCompareValue(int value) {
		if (value == 1) {
			return 14;
		}
		if (value == 2) {
			return 15;
		}
		return value;
	}

	public static boolean checkPokerCount(Map<Integer, Integer> cardNums, int... counts) {
		List<Integer> list = new ArrayList<>(cardNums.values());
		if (list.size() != counts.length) {
			return false;
		}
		List<Integer> target = new ArrayList<>();
		for (int i = 0, count = counts.length; i < count; i++) {
			target.add(counts[i]);
		}
		Iterator<Integer> iter = target.iterator();
		while (iter.hasNext()) {
			Integer value = iter.next();
			if (!list.contains(value)) {
				return false;
			}
			iter.remove();
			list.remove(value);
		}
		return target.size() == 0;
	}


	public static boolean isBigger(PokerCard card1, PokerCard card2) {
		return card1.getCompareValue() > card2.getCompareValue();
	}

	public static int getPokerCount(List<PokerCard> cardList, int pokerValue) {
		int count = 0;
		for (PokerCard pokerCard : cardList) {
			if (pokerCard.getPokerValue() == pokerValue) {
				count++;
			}
		}
		return count;
	}

	public static Map<Integer, Integer> getAllPokerCount(List<PokerCard> cardList) {
		Map<Integer, Integer> result = new HashMap<>();
		cardList.forEach(e -> {
			Integer original = result.get(e.getPokerValue());
			if (original == null) {
				result.put(e.getPokerValue(), 1);
			} else {
				result.put(e.getPokerValue(), original + 1);
			}
		});
		return result;
	}

	public static Map<Integer, Integer> getZjhPokerCount(List<PokerCard> cardList) {
		Map<Integer, Integer> result = new HashMap<>();
		cardList.forEach(e -> {
			Integer original = result.get(e.getZjhPokerValue());
			if (original == null) {
				result.put(e.getZjhPokerValue(), 1);
			} else {
				result.put(e.getZjhPokerValue(), original + 1);
			}
		});
		return result;
	}

	public static Queue<PokerCard> mixAllCard(GameType type) {
		Queue<PokerCard> result = new LinkedList<>();
		List<PokerCard> paiPool = null;
		switch (type) {
			case NIUNIU:
				paiPool = GameConst.getOneCardPoolWithoutJoker();
				break;
			case ZJH:
				paiPool = GameConst.getOneCardPoolWithoutJoker();
				break;
			case DDZ:
			case LZ_DDZ:
				paiPool = GameConst.getOneNormalPokerCopy();
				break;
			case COUPLE_DDZ:
				paiPool = GameConst.getCouplePokerCards();
				break;
			default:
				paiPool = GameConst.getOneCardPoolWithoutJoker();
				break;
		}
		ArrayPai.getInst().arrayPaiForPoker(result, paiPool, type);
		return GameUtil.mixCollections(result, paiPool);
	}

	private static boolean checkFiveSmallNiu(List<PokerCard> pokerList) {
		int totalValue = 0;
		for (PokerCard card : pokerList) {
			if (card.getPokerValue() >= 5) {
				return false;
			}
			totalValue += card.getValue();
		}
		if (totalValue >= 10) {
			return false;
		}
		return true;
	}

	public static boolean checkBombNiu(List<PokerCard> pokerList) {
		if (pokerList.size() < 5) {
			return false;
		}
		for (int i = 0; i < 2; i++) {
			if (getCountOfValue(pokerList, pokerList.get(i).getPokerValue()) == 4) {
				return true;
			}
		}
		return false;
	}

	private static int getCountOfValue(List<PokerCard> list, int value) {
		int count = 0;
		for (PokerCard card : list) {
			if (card.getPokerValue() == value) {
				count++;
			}
		}
		return count;
	}

	public static boolean isOneGtTwo(Pair<NiuResult, List<PokerCard>> oneCardInfo, Pair<NiuResult, List<PokerCard>> twoCardInfo) {
		NiuResult oneResult = oneCardInfo.getLeft();
		NiuResult twoResult = twoCardInfo.getLeft();
		if (oneResult.getValue() > twoResult.getValue()) {
			return true;
		} else if (oneResult.getValue() < twoResult.getValue()) {
			return false;
		} else {
			PokerCard oneMaxCard = getMaxValueCard(oneCardInfo.getRight());
			PokerCard twoMaxCard = getMaxValueCard(twoCardInfo.getRight());
			return oneMaxCard.gtOther(twoMaxCard);
		}
	}

	public static PokerCard getMaxValueCard(List<PokerCard> cardList) {
		PokerCard result = null;
		for (PokerCard card : cardList) {
			if (result == null) {
				result = card;
			}
			if (result.getPokerValue() < card.getPokerValue()) {
				result = card;
			}
			if (result.getPokerValue() == card.getPokerValue() && result.getPokerType().getValue() < card.getPokerType().getValue()) {
				result = card;
			}
		}
		return result;
	}


	public static Pair<Pair<PokerCard, PokerCard>, NiuResult> calNiuResult(List<PokerCard> pokerList) {
		if (checkFiveSmallNiu(pokerList)) {
			return new Pair<>(null, NiuResult.FIVE_SMALL_NIU);
		}
		if (checkBombNiu(pokerList)) {
			return new Pair<>(null, NiuResult.BOMB_NIU);
		}
		Pair<PokerCard, PokerCard> twoCards = getNiuResult(pokerList);
		if (twoCards == null) {
			return new Pair<>(null, NiuResult.NO_NIU);
		}
		int value = twoCards.getLeft().getValue() + twoCards.getRight().getValue();
		if (value % 10 != 0) {
			return new Pair<>(twoCards, NiuResult.getByValue(value % 10));
		}
//		if (getCountOfFaceCard(pokerList) == 4) {
//			return new Pair<>(null, NiuResult.FOUR_NIU);
//		}
		if (getCountOfFaceCard(pokerList) == 5) {
			return new Pair<>(null, NiuResult.FIVE_NIU);
		}
		return new Pair<>(twoCards, NiuResult.NIU_NIU);
	}


	private static int getCountOfFaceCard(List<PokerCard> cardList) {
		int count = 0;
		for (PokerCard card : cardList) {
			if (card.getPokerValue() > 10) {
				count++;
			}
		}
		return count;
	}

	public static Pair<PokerCard, PokerCard> getNiuResult(List<PokerCard> list) {
		List<PokerCard> copyList = new ArrayList<>(list);
		int total = 0;
		for (PokerCard value : copyList) {
			total += value.getValue();
		}
		int remain = total % 10;
		Pair<PokerCard, PokerCard> result = selectTwoPokerAddEqualValue(copyList, remain);
		if (result == null) {
			remain += 10;
		}
		return selectTwoPokerAddEqualValue(copyList, remain);
	}

	private static Pair<PokerCard, PokerCard> selectTwoPokerAddEqualValue(List<PokerCard> cardList, int value) {
		for (PokerCard card : cardList) {
			int needValue = value - card.getValue();
			if (needValue < 0) {
				continue;
			}
			PokerCard needCard = getNeedPokerCard(cardList, needValue, card);
			if (needCard == null) {
				continue;
			}
			if (getRemainCardTotal(cardList, card, needCard) % 10 == 0) {
				return new Pair<>(card, needCard);
			}
		}
		return null;
	}

	private static int getRemainCardTotal(List<PokerCard> list, PokerCard... ignoreCards) {
		int total = 0;
		for (PokerCard card : list) {
			boolean contain = false;
			for (int i = 0, length = ignoreCards.length; i < length; i++) {
				if (card == ignoreCards[i]) {
					contain = true;
					break;
				}
			}
			if (contain) {
				continue;
			}
			total += card.getValue();
		}
		return total;
	}

	private static PokerCard getNeedPokerCard(List<PokerCard> list, int value, PokerCard ignoreCard) {
		for (PokerCard card : list) {
			if (card == ignoreCard) {
				continue;
			}
			if (card.getValue() == value) {
				return card;
			}
		}
		return null;
	}


	private static PokerCard selectMinCard(List<PokerCard> list) {
		PokerCard result = null;
		for (PokerCard card : list) {
			if (result == null || result.getKey() > card.getKey()) {
				result = card;
			}
		}
		list.remove(result);
		return result;
	}

	//检查牌是否是顺子（扎金花）
	public static boolean checkZjhStraight(List<PokerCard> cardList) {
		int currentPokerValue = 0;
		for (int i = 0, size = cardList.size(); i < size; i++) {
			PokerCard card = cardList.get(i);
			if (card == null) {
				return false;
			}
			if (currentPokerValue == 0) {
				currentPokerValue = cardList.get(i).getPokerValue();
				continue;
			}
//			System.out.println(card.getPokerValue());
			if (card.getPokerValue() == 12) { //A Q或者2算同花顺
				if (card.getPokerValue() == currentPokerValue + 1) {
					currentPokerValue++;
				} else if (card.getPokerValue() == currentPokerValue + 11) {
					currentPokerValue += 11;
				} else {
					return false;
				}
			} else {
				if (card.getPokerValue() != currentPokerValue + 1) {
					return false;
				} else {
					currentPokerValue++;
				}
			}
		}
		return true;
	}

//	//检查牌是否是顺金 无大小王
//	public static boolean checkStraightAndTheSame(List<PokerCard> cardList) {
//		int currentPokerValue = 0;
//		int color = -1;
//		for (int i = 0, size = cardList.size(); i < size; i++) {
//			PokerCard card = cardList.get(i);
//			if (card == null) {
//				return false;
//			}
//			if (currentPokerValue == 0) {
//				currentPokerValue = cardList.get(i).getPokerValue();
//				color = cardList.get(i).getPokerType().getValue();
//				continue;
//			}
//			if (color != cardList.get(i).getPokerType().getValue()) {    //花色不同 out
//				return false;
//			}
//			if (card.getPokerValue() == 1) { //A 最后一张牌 前面是Q或者2算同花顺
//				if (card.getPokerValue() != currentPokerValue - 1 || card.getPokerValue() != currentPokerValue - 11) {
//					return false;
//				}
//			}
//			if (card.getPokerValue() != currentPokerValue - 1) {
//				return false;
//			} else {
//				currentPokerValue++;
//			}
//		}
//		return true;
//	}

	//检查花色是否相同
	public static boolean checkPokerColorIsTheSame(List<PokerCard> cardList) {
		int color = 0;
		for (int i = 0, size = cardList.size(); i < size; i++) {
			PokerCard card = cardList.get(i);
			if (card == null) {
				return false;
			}
			if (i == 0) {
				color = cardList.get(i).getPokerType().getValue();
				continue;
			}
			if (color != card.getPokerType().getValue()) {
				return false;
			}
		}
		return true;
	}

	//扎金花特殊牌型
	public static boolean checkSpeicialCard(List<PokerCard> cardList) {
		List<Integer> typeList = new ArrayList<>();
		List<Integer> valueList = new ArrayList<>();
		for (PokerCard card : cardList) {
			valueList.add(card.getPokerValue());
			if (!typeList.contains(card.getPokerType().getValue())) {
				typeList.add(card.getPokerType().getValue());
			}
		}
		if (!valueList.contains(2) || !valueList.contains(3) || !valueList.contains(5)) {
			return false;
		}
		if (typeList.size() == 1) {
			return false;
		}
		return true;
	}
}
