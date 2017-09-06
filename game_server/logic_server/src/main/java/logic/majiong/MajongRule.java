package logic.majiong;


import util.Pair;
import util.Three;

import java.util.*;

/**
 * Created by Administrator on 2016/12/20.
 */
public class MajongRule {

	public static boolean checkHupai(List<Integer> cards) {
		return checkHupai(cards, null);
	}


	public static boolean checkHandCardHuPai(List<Integer> cards) {
		if (checkQidui(new ArrayList<>(cards))) {
			return true;
		}

		// 若只剩下一对将一定胡牌
		if(cards.size() == 2){
			return cards.get(0) == cards.get(1);
		}
		Set<Integer> pairsList = spiltIntoPairs(cards);     // 超过2张就记录
		for (Integer value : pairsList) {
			List<Integer> list = new ArrayList<>(cards);
			list.remove(value);
			list.remove(value);
			Pair<List<Integer>, List<Integer>> chiKeList = getChiKeList(list);
			if (chiKeList != null) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkHupai(List<Integer> cards, MajongType ignoreType) {
		if (ignoreType != null) {
			for (Integer card : cards) {
				if (GameUtil.getMajongTypeByValue(card) == ignoreType) {
					return false;
				}
			}
		}
		if (checkQidui(new ArrayList<>(cards))) {
			return true;
		}
		Set<Integer> pairsList = spiltIntoPairs(cards);     // 超过2张就记录
		for (Integer value : pairsList) {
			List<Integer> list = new ArrayList<>(cards);
			list.remove(value);
			list.remove(value);
			Pair<List<Integer>, List<Integer>> chiKeList = getChiKeList(list);
			if (chiKeList != null) {
				return true;
			}
		}
		return false;
	}

	public static List<Three<List<Integer>, List<Integer>, List<Integer>>> getHuPaiResult(List<Integer> cardList) {
		List<Three<List<Integer>, List<Integer>, List<Integer>>> result = new ArrayList<>();
		Set<Integer> pairsList = spiltIntoPairs(cardList);
		if (pairsList.size() == 7) {
			Three<List<Integer>, List<Integer>, List<Integer>> res = new Three<>(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(pairsList));
			result.add(res);
			return result;
		}
		for (Integer value : pairsList) {
			List<Integer> list = new ArrayList<>(cardList);
			list.remove(value);
			list.remove(value);
			Pair<List<Integer>, List<Integer>> chiKeList = getChiKeList(list);
			if (chiKeList != null) {
				List<Integer> jiangList = new ArrayList<>();
				jiangList.add(value);
				result.add(new Three<>(chiKeList.getLeft(), chiKeList.getRight(), jiangList));
			}
		}
		return result;
	}


	public static boolean checkQidui(List<Integer> cards) {
		if (cards.size() < 14) {
			return false;
		}
		while (cards.size() > 0) {
			if (GameUtil.getCountOfValue(cards, cards.get(0)) < 2) {
				return false;
			}
			GameUtil.removeCard(cards, cards.get(0), 2);
		}
		return true;
	}

	public static boolean checkTingpai(List<Integer> cardList, MajongType ignoreType) {
		for (Integer value : cardList) {
			if (GameUtil.getMajongTypeByValue(value) == ignoreType) {
				return false;
			}
		}
		return checkTingpai(cardList);
	}

	public static boolean checkCoupleTingpai(List<Integer> cardList,boolean isZhuang){
		if (isZhuang) {
			List<Integer> copyList = new ArrayList<>(cardList);
			return checkHandCardHuPai(copyList);
		} else {
			for (int i = GameConst.WAN_1; i < GameConst.WAN_9 + 1; i++) {
				List<Integer> copyList = new ArrayList<>(cardList);
				copyList.add(i);
				if (checkHandCardHuPai(copyList)) {
					return true;
				}
			}

			for (int i = GameConst.DONG_FENG; i < GameConst.BEI_FENG + 1; i++) {
				List<Integer> copyList = new ArrayList<>(cardList);
				copyList.add(i);
				if (checkHandCardHuPai(copyList)) {
					return true;
				}
			}

			for (int i = GameConst.ZHONG; i < GameConst.BAIBAN + 1; i++) {
				List<Integer> copyList = new ArrayList<>(cardList);
				copyList.add(i);
				if (checkHandCardHuPai(copyList)) {
					return true;
				}
			}

			return false;
		}
	}

	public static boolean checkTingpai(List<Integer> cardList) {
		for (int i = GameConst.WAN_1; i < GameConst.WAN_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				return true;
			}
		}
		for (int i = GameConst.TIAO_1; i < GameConst.TIAO_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				return true;
			}
		}
		for (int i = GameConst.TONG_1; i < GameConst.TONG_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				return true;
			}
		}
		return false;
	}

	public static List<Integer> getTingpaiResult(List<Integer> cardList) {
		List<Integer> result = new ArrayList<>();
		for (int i = GameConst.WAN_1; i < GameConst.WAN_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				result.add(i);
			}
		}
		for (int i = GameConst.TIAO_1; i < GameConst.TIAO_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				result.add(i);
			}
		}
		for (int i = GameConst.TONG_1; i < GameConst.TONG_9 + 1; i++) {
			List<Integer> copyList = new ArrayList<>(cardList);
			copyList.add(i);
			if (checkHandCardHuPai(copyList)) {
				result.add(i);
			}
		}
		return result;
	}

	private static Pair<List<Integer>, List<Integer>> getChiKeList(List<Integer> list) {
		List<Integer> chiList = new ArrayList<>();
		List<Integer> keList = new ArrayList<>();
		List<Integer> cardKinds = new ArrayList<>();
		while (list.size() > 0) {
			cardKinds.clear();
			list.forEach(e -> {
				if (!cardKinds.contains(e)) {
					cardKinds.add(e);
				}
			});
			Collections.sort(cardKinds);
			for (int i = 0, size = cardKinds.size(); i < size; i++) {
				int card = cardKinds.get(i);
				int cardCount = GameUtil.getCountOfValue(list, card);
				if (cardCount == 3) {
					keList.add(card);
					remove(list, card);
					remove(list, card);
					remove(list, card);
					break;
				} else {
					int chiValue = filterChi(list, card);
					if (chiValue < 0)
						return null;
					else
						chiList.add(chiValue);
					break;
				}
			}
		}
		return new Pair<>(chiList, keList);
	}

	private static void remove(List<Integer> cardList, Integer value) {
		cardList.remove(value);
	}

	private static int filterChi(List<Integer> cardList, int chiValue) {
		if (chiValue > GameConst.TIAO_9) {
			return -1;
		}
		if (cardList.contains(chiValue + 1) && cardList.contains(chiValue + 2)) {
			remove(cardList, chiValue);
			remove(cardList, chiValue + 1);
			remove(cardList, chiValue + 2);
			return chiValue;
		}
		if (cardList.contains(chiValue - 1) && cardList.contains(chiValue - 2)) {
			remove(cardList, chiValue);
			remove(cardList, chiValue - 1);
			remove(cardList, chiValue - 2);
			return chiValue - 2;
		}
		if (cardList.contains(chiValue - 1) && cardList.contains(chiValue + 1)) {
			remove(cardList, chiValue);
			remove(cardList, chiValue + 1);
			remove(cardList, chiValue - 1);
			return chiValue - 1;
		}
		return -1;
	}

	private static Set<Integer> spiltIntoPairs(List<Integer> cards) {
		Set<Integer> result = new HashSet<>();
		int size = cards.size();
		for (int i = 0; i < size; i++) {
			if (GameUtil.getCountOfValue(cards, cards.get(i)) < 2) {
				continue;
			}
			result.add(cards.get(i));
		}
		return result;
	}

}
