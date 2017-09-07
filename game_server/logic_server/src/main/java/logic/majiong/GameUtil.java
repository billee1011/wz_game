package logic.majiong;

import logic.debug.ArrayPai;
import logic.majiong.define.MJPosition;
import logic.majiong.define.MJType;
import proto.CoupleMajiang;
import util.Randomizer;

import java.util.*;

public class GameUtil {
	public static List<Integer> selectKe(List<Integer> cards) {
		List<Integer> result = new ArrayList<>();
		int size = cards.size();
		for (int i = 0; i < size; i++) {
			if (result.contains(cards.get(i))) {
				continue;
			}
			if (getCountOfValue(cards, cards.get(i)) != 3) {
				continue;
			}
			result.add(cards.get(i));
		}
		for (Integer resultValue : result) {
			removeCard(cards, resultValue, 3);
		}
		return result;
	}

	public static List<Integer> selectChi(List<Integer> cards) {
		List<Integer> result = new ArrayList<>();
		int chiValue = 0;
		while ((chiValue = selectOneChi(cards)) != -1) {
			result.add(chiValue);
		}
		return result;
	}

	public static int selectOneChi(List<Integer> cards) {
		int size = cards.size();
		int chiValue = 0;
		for (int i = 0; i < size; i++) {
			if (checkChi(cards, cards.get(i))) {
				chiValue = cards.get(i);
				break;
			}
		}
		if (chiValue != 0) {
			removeCard(cards, chiValue, 1);
			removeCard(cards, chiValue + 1, 1);
			removeCard(cards, chiValue + 2, 1);
			return chiValue;
		}
		return -1;
	}

	public static List<Integer> selectGang(List<Integer> cards) {
		List<Integer> result = new ArrayList<>();
		int size = cards.size();
		for (int i = 0; i < size; i++) {
			if (result.contains(cards.get(i))) {
				continue;
			}
			if (getCountOfValue(cards, cards.get(i)) != 4) {
				continue;
			}
			result.add(cards.get(i));
		}
		for (Integer resultValue : result) {
			removeCard(cards, resultValue, 4);
		}
		return result;
	}

	public static void removeCard(List<Integer> cards, int value, int count) {
		if (count <= 0) {
			return;
		}
		Iterator<Integer> iter = cards.iterator();
		int removeCount = 0;
		while (iter.hasNext()) {
			if (iter.next() == value) {
				iter.remove();
				removeCount++;
			}
			if (removeCount == count) {
				break;
			}
		}
	}

	public static int getCountOfValue(List<Integer> cards, int value) {
		value = getRealValue(value);
		int count = 0;
		for (Integer card : cards) {
			if (getRealValue(card.intValue()) == value) {
				count++;
			}
		}
		return count;
	}

	public static boolean isYaojiuCard(int value) {
		value = getRealValue(value);
		if (value == GameConst.WAN_1 || value == GameConst.WAN_9
				|| value == GameConst.TIAO_1 || value == GameConst.TIAO_9
				|| value == GameConst.TONG_1 || value == GameConst.TONG_9) {
			return true;
		}
		return false;
	}


	public static boolean isYaoJiuZiCard(int value) {
		value = getRealValue(value);
		if (value == GameConst.WAN_1 || value == GameConst.WAN_9 || value == GameConst.DONG_FENG
				|| value == GameConst.NAN_FENG || value == GameConst.XI_FENG || value == GameConst.BEI_FENG
				|| value == GameConst.ZHONG || value == GameConst.FACAI || value == GameConst.BAIBAN) {
			return true;
		}
		return false;
	}

	public static boolean isFengCard(int value) {
		value = getRealValue(value);
		if (value == GameConst.DONG_FENG || value == GameConst.NAN_FENG
				|| value == GameConst.XI_FENG || value == GameConst.BEI_FENG) {
			return true;
		}
		return false;
	}

	public static boolean isJianCard(int value) {
		value = getRealValue(value);
		if (value == GameConst.ZHONG || value == GameConst.FACAI
				|| value == GameConst.BAIBAN) {
			return true;
		}
		return false;
	}

	public static boolean gtWan5(int value) {
		if (!GameUtil.isWanCard(value)) {
			return false;
		}
		if (value <= GameConst.WAN_5) {
			return false;
		}
		return true;
	}

	public static boolean ltWan5(int value) {
		if (!GameUtil.isWanCard(value)) {
			return false;
		}
		if (value >= GameConst.WAN_5) {
			return false;
		}
		return true;
	}


	public static boolean isWanCard(int value) {
		value = getRealValue(value);
		if (value >= GameConst.WAN_1 && value <= GameConst.WAN_9) {
			return true;
		}
		return false;
	}

	public static boolean isTongCard(int value) {
		value = getRealValue(value);
		if (value >= GameConst.TONG_1 && value <= GameConst.TONG_9) {
			return true;
		}
		return false;
	}

	public static boolean isTiaoCard(int value) {
		value = getRealValue(value);
		if (value >= GameConst.TIAO_1 && value <= GameConst.TIAO_9) {
			return true;
		}
		return false;
	}


	public static int getRealValue(int value) {
		return value > GameConst.AN_MASK ? value - GameConst.AN_MASK : value;
	}

	public static int getRealHuValue(int value) {
		return value > GameConst.Hu_MASK ? value - GameConst.Hu_MASK : value;
	}

	//发牌的流程是什么样的呢

//	//发牌给斗地主, 洗牌阶段,可能需要传入参数, 不同规则的斗地主可能牌不一样
//	public static Queue<PokerCard> mixAllPokerCard(GameType type) {
//		Queue<PokerCard> result = new LinkedList<>();
//		List<PokerCard> paiPool;
//		if (type == GameType.COUPLE_DDZ) {
//			paiPool = GameConst.getCouplePokerCards();
//		} else {
//			paiPool = GameConst.getOneNormalPokerCopy();
//		}
//		return mixCollections(result, paiPool);
//	}

	public static Queue<Integer> mixAllCard(MJType type) {
		Queue<Integer> result = new LinkedList<>();
		List<Integer> paiPool = null;
		switch (type) {
			case COUPLE_MJ:
				paiPool = GameConst.getMajiangPoolCopy();
				break;
			case XUELIU:
				paiPool = GameConst.getXueliuPoolCopy();
				break;
			default:
				paiPool = GameConst.getMajiangPoolCopy();
				break;
		}
//		result.addAll(Arrays.asList(1, 1, 12, 12, 3, 4, 5, 5, 5, 6, 7, 8, 11, 11));
//		result.addAll(Arrays.asList(3, 3, 2, 5, 4, 4, 6, 6, 8, 8, 9, 11, 12));
//		result.addAll(Arrays.asList(13, 13, 13, 14, 14, 14, 15, 15, 11, 16, 16, 5, 15));
//		result.addAll(Arrays.asList(21, 21, 21, 22, 22, 22, 23, 23, 23, 24, 24, 24, 25));
//		result.addAll(Arrays.asList(6, 8, 6, 8, 6, 8, 3, 3, 3, 4, 4, 5, 5));
//		return result;
		ArrayPai.getInst().arrayPaiForMj(result, paiPool, type);
		return mixCollections(result, paiPool);
	}
	
	public static <T> Queue<T> mixCollections(Queue<T> result, List<T> list) {
		while (list.size() > 0) {
			int size = list.size();
			int randomNum = Randomizer.nextInt(size);
			result.add(list.get(randomNum));
			list.remove(randomNum);
		}
		return result;
	}


	public static boolean containHuapai(List<Integer> cards) {
		for (Integer card : cards) {
			if (card == null) {
				continue;
			}
			if (isHuapai(card)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isHuapai(int card) {
		if (card == GameConst.CHUN || card == GameConst.XIA || card == GameConst.QIU || card == GameConst.DONG
				|| card == GameConst.MEI || card == GameConst.LAN || card == GameConst.ZHU || card == GameConst.JU) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否拥有此牌
	 * @param cards
	 * @param card
	 * @return
	 */
	public static boolean isHaveCard(List<Integer> cards, int card) {
		return cards.stream().allMatch(e -> e.intValue() == card);
	}

	public static List<Integer> selectAllHuapai(List<Integer> cards) {
		List<Integer> result = new ArrayList<>();
		Iterator<Integer> iter = cards.iterator();
		while (iter.hasNext()) {
			int card = iter.next();
			if (card == GameConst.CHUN || card == GameConst.XIA || card == GameConst.QIU || card == GameConst.DONG
					|| card == GameConst.MEI || card == GameConst.LAN || card == GameConst.ZHU || card == GameConst.JU) {
				result.add(card);
				iter.remove();
			}
		}
		return result;
	}
	
	public static boolean checkHave(PlayerDeskInfo info, List<Integer> list) {
		Map<Integer, Integer> switchMap = new HashMap<Integer, Integer>();
		for (Integer card : list) {
			if (switchMap.containsKey(card)) {
				switchMap.put(card, switchMap.get(card) + 1);
			} else {
				switchMap.put(card, 1);
			}
		}
		for (Map.Entry<Integer, Integer> entry : switchMap.entrySet()) {
			if (getCountOfValue(info.getHandCards(), entry.getKey()) < entry.getValue()) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean checkPeng(PlayerDeskInfo info, int value) {
		return getCountOfValue(info.getHandCards(), value) >= 2;
	}


	public static boolean checkGang(PlayerDeskInfo info, int value, boolean selfTurn) {
		if (selfTurn) {
			if (info.getKeQueue().contains(value) && getCountOfValue(info.getHandCards(), value) == 1) {
				return true;
			}
			return getCountOfValue(info.getHandCards(), value) >= 4;
		} else {
			return getCountOfValue(info.getHandCards(), value) >= 3;
		}
	}


	public static boolean checkChi(PlayerDeskInfo info, int value, ChiType type) {
		switch (type) {
			case LEFT:
				return info.getHandCards().contains(value + 1) && info.getHandCards().contains(value + 2);
			case MIDDLE:
				return info.getHandCards().contains(value - 1) && info.getHandCards().contains(value + 1);
			case RIGHT:
				return info.getHandCards().contains(value - 1) && info.getHandCards().contains(value - 2);
			default:
				return false;
		}
	}

	public static List<Integer> fromCoupleFanList(List<CoupleFanType> typeList) {
		List<Integer> result = new ArrayList<>();
		typeList.forEach(e -> result.add(e.getValue()));
		return result;
	}

	public static boolean checkChi(List<Integer> info, int value) {
		if (isFengCard(value) || isJianCard(value)) {
			return false;
		}
		if ((info.contains(value + 1) && info.contains(value + 2))
				|| (info.contains(value + 1) && info.contains(value - 1))
				|| (info.contains(value - 1) && info.contains(value - 2))) {
			return true;
		}
		return false;
	}


	public static int getTotalFanshu(List<CoupleFanType> typeList) {
		int total = 0;
		for (CoupleFanType type : typeList) {
			total += (type.getValue() >> 8);
		}
		return total;
	}

	public static List<Integer> mergeChiKeJiang(Collection<Integer> chi, Collection<Integer> ke, Collection<Integer> jiang) {
		List<Integer> result = new ArrayList<>();
		addChiToCardList(result, chi);
		addKeToCardList(result, ke);
		addJiangToCardList(result, jiang);
		return result;
	}


	public static void addChiToCardList(List<Integer> list, Collection<Integer> valueList) {
		for (Integer value : valueList) {
			addChiToCardList(list, value);
		}
	}

	public static void addChiToCardList(List<Integer> list, int value) {
		list.add(value);
		list.add(value + 1);
		list.add(value + 2);
	}

	public static void addKeToCardList(List<Integer> list, int value) {
		for (int i = 0; i < 3; i++) {
			list.add(value);
		}
	}

	public static void addJiangToCardList(List<Integer> list, int value) {
		for (int i = 0; i < 2; i++) {
			list.add(value);
		}
	}

	public static void addJiangToCardList(List<Integer> list, Collection<Integer> valueList) {
		for (Integer value : valueList) {
			addJiangToCardList(list, value);
		}
	}

	public static void addKeToCardList(List<Integer> list, Collection<Integer> valueList) {
		for (Integer value : valueList) {
			addKeToCardList(list, value);
		}
	}

	public static void addGangToCardList(List<Integer> list, Collection<Integer> valueList) {
		for (Integer value : valueList) {
			addGangToCardList(list, value);
		}
	}

	public static void addGangToCardList(List<Integer> list, int value) {
		for (int i = 0; i < 4; i++) {
			list.add(value);
		}
	}

	public static void filterIgnoreFanType(List<CoupleFanType> fanList) {
		Set<CoupleFanType> ignoreList = new HashSet<>();
		fanList.forEach(e -> {
			List<CoupleFanType> list = e.getIgnoreList();
			if (list != null) {
				for (CoupleFanType fanType : list) {
					ignoreList.add(fanType);
				}
			}
		});
		ignoreList.forEach(e -> {
			fanList.remove(e);
		});
	}

	public static boolean containOperation(List<Integer> operationList, MajiangOperationType type) {
		for (Integer value : operationList) {
			if (value.intValue() == type.getValue()) {
				return true;
			}
		}
		return false;
	}

	public static MajiangOperationType getMaxPriorityType(List<Integer> operationList) {
		MajiangOperationType result = null;
		for (Integer value : operationList) {
			MajiangOperationType type = MajiangOperationType.getByValue(value);
			if (result == null || result.getPriority() < type.getPriority()) {
				result = type;
			}
		}
		return result;
	}

	public static MJPosition switchPositionToOperation(MJPosition currPosition, Map<MJPosition, MajiangOperationType> operationMap) {
		MJPosition result = null;
		for (Map.Entry<MJPosition, MajiangOperationType> entry : operationMap.entrySet()) {
			if (result == null) {
				result = entry.getKey();
			}
			if (operationMap.get(result).getPriority() < entry.getValue().getPriority()) {
				result = entry.getKey();
			}
			if (operationMap.get(result).getPriority() == entry.getValue().getPriority()) {
				if (currPosition.minusPosition(result) > currPosition.minusPosition(entry.getKey())) {
					result = entry.getKey();
				}
			}
		}
		return result;
	}

	public static MajongType getMajongTypeByValue(int value) {
		if (isWanCard(value)) {
			return MajongType.WAN;
		}
		if (isTiaoCard(value)) {
			return MajongType.TIAO;
		}
		if (isTongCard(value)) {
			return MajongType.TONG;
		}
		if (isFengCard(value)) {
			return MajongType.FENG;
		}
		if (isJianCard(value)) {
			return MajongType.ZI;
		}
		return null;
	}

	public static boolean isJiang(int cardValue) {
		if (cardValue > GameConst.TIAO_9) {
			return false;
		}
		int cardRemain = cardValue % 10;
		if (cardRemain == 2 || cardRemain == 5 || cardRemain == 8) {
			return true;
		}
		return false;
	}

	public static List<Integer> getOperationList(PlayerDeskInfo info, int value, boolean checkChi, MajongType ignoreType, boolean hupai) {
		List<Integer> result = null;
		if (info.isDefeat()) {
			return null;
		}
		if (getMajongTypeByValue(value) == ignoreType) {
			return null;
		}
		List<Integer> copyList = new ArrayList<>(info.getHandCards());
		copyList.add(value);
		if (MajongRule.checkHupai(copyList, ignoreType)) {
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(MajiangOperationType.HU.getValue());
		}
		if (checkGang(info, value, false)) {
			if (info.isHuPai() || info.isBaoTing()) {
				if (tingpaiAfterGang(info.getHandCards(), value)) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(MajiangOperationType.GANG.getValue());
				}
			} else {
				if (result == null) {
					result = new ArrayList<>();
				}
				result.add(MajiangOperationType.GANG.getValue());
			}
		}
		if (!hupai) {
			if (checkPeng(info, value)) {
				if (result == null) {
					result = new ArrayList<>();
				}
				result.add(MajiangOperationType.KE.getValue());
			}
			if (checkChi && checkChi(info.getHandCards(), value)) {
				if (result == null) {
					result = new ArrayList<>();
				}
				result.add(MajiangOperationType.CHI.getValue());
			}
		}
		return result;
	}


	public static void main(String[] args) {
		List<Integer> cardList = new ArrayList<>();
		cardList.add(2);
		cardList.add(3);
		cardList.add(4);
		cardList.add(4);
		cardList.add(5);
		cardList.add(5);
		cardList.add(5);
		tingpaiAfterGang(cardList, 5);
	}


	private static boolean tingpaiAfterGang(List<Integer> cardList, int gangValue) {
		List<Integer> listCopy = new ArrayList<>(cardList);
		List<Integer> originalList = MajongRule.getTingpaiResult(listCopy);
		removeCard(listCopy, gangValue, 4);
		List<Integer> currentList = MajongRule.getTingpaiResult(listCopy);
		if (currentList.size() != originalList.size()) {
			return false;
		}
		currentList.removeAll(originalList);
		return currentList.size() == 0;
	}


	//检查血流成河玩家是否花猪
	public static boolean isHuaZhu(List<Integer> cardList, MajongType ignoreType) {
		for (Integer card : cardList) {
			if (GameUtil.getMajongTypeByValue(card) == ignoreType) {
				return true;
			}
		}
		return false;
	}

	public static int getHuValue(CoupleMajiang.PBHuReq hu, List<Integer> handCopy) {
		List<Integer> chiList = new ArrayList<>(hu.getChiListList());
		List<Integer> keList = new ArrayList<>(hu.getKeListList());
		List<Integer> jiangList = new ArrayList<>(hu.getJiangValueList());
		List<Integer> allCards = new ArrayList<>();
		GameUtil.addChiToCardList(allCards, chiList);
		GameUtil.addKeToCardList(allCards, keList);
		GameUtil.addJiangToCardList(allCards, jiangList);
		handCopy.forEach(e -> allCards.remove(e));
		if (allCards.size() != 1) {
			return -1;
		}
		return allCards.get(0);
	}
}
