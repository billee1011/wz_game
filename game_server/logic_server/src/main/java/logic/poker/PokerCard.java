package logic.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/12/13.
 */
public enum PokerCard {
	FANG_1(0x0101, 1),
	FANG_2(0x0102, 2),
	FANG_3(0x0103, 3),
	FANG_4(0x0104, 4),
	FANG_5(0x0105, 5),
	FANG_6(0x0106, 6),
	FANG_7(0x0107, 7),
	FANG_8(0x0108, 8),
	FANG_9(0x0109, 9),
	FANG_10(0x010a, 0),
	FANG_J(0x010b, 0),
	FANG_Q(0x010c, 0),
	FANG_K(0x010d, 0),
	MEI_1(0x0201, 1),
	MEI_2(0x0202, 2),
	MEI_3(0x0203, 3),
	MEI_4(0x0204, 4),
	MEI_5(0x0205, 5),
	MEI_6(0x0206, 6),
	MEI_7(0x0207, 7),
	MEI_8(0x0208, 8),
	MEI_9(0x0209, 9),
	MEI_10(0x020a, 0),
	MEI_J(0x020b, 0),
	MEI_Q(0x020c, 0),
	MEI_K(0x020d, 0),
	HONG_1(0X0301, 1),
	HONG_2(0X0302, 2),
	HONG_3(0X0303, 3),
	HONG_4(0X0304, 4),
	HONG_5(0X0305, 5),
	HONG_6(0X0306, 6),
	HONG_7(0X0307, 7),
	HONG_8(0X0308, 8),
	HONG_9(0X0309, 9),
	HONG_10(0X030a, 0),
	HONG_J(0X030b, 0),
	HONG_Q(0X030c, 0),
	HONG_K(0X030d, 0),
	HEI_1(0x0401, 1),
	HEI_2(0x0402, 2),
	HEI_3(0x0403, 3),
	HEI_4(0x0404, 4),
	HEI_5(0x0405, 5),
	HEI_6(0x0406, 6),
	HEI_7(0x0407, 7),
	HEI_8(0x0408, 8),
	HEI_9(0x0409, 9),
	HEI_10(0x040a, 0),
	HEI_J(0x040b, 0),
	HEI_Q(0x040c, 0),
	HEI_K(0x040d, 0),
	JOKER_ONE(0x0510, -1),                //小王
	JOKER_TWO(0x0511, -1);                //大王

	private int key;

	private int value;

	public PokerColorType getPokerType() {
		int type = this.key;
		type = type >> 8;
		return PokerColorType.getByValue(type);
	}

	public static List<PokerCard> lzConvertCards = new ArrayList<>();

	static {
		lzConvertCards.add(PokerCard.FANG_1);
		lzConvertCards.add(PokerCard.FANG_2);
		lzConvertCards.add(PokerCard.FANG_3);
		lzConvertCards.add(PokerCard.FANG_4);
		lzConvertCards.add(PokerCard.FANG_5);
		lzConvertCards.add(PokerCard.FANG_6);
		lzConvertCards.add(PokerCard.FANG_7);
		lzConvertCards.add(PokerCard.FANG_8);
		lzConvertCards.add(PokerCard.FANG_9);
		lzConvertCards.add(PokerCard.FANG_10);
		lzConvertCards.add(PokerCard.FANG_J);
		lzConvertCards.add(PokerCard.FANG_Q);
		lzConvertCards.add(PokerCard.FANG_K);
	}

	public static List<List<PokerCard>> lzComposite1 = new ArrayList<>();
	public static List<List<PokerCard>> lzComposite2 = new ArrayList<>();
	public static List<List<PokerCard>> lzComposite3 = new ArrayList<>();
	public static List<List<PokerCard>> lzComposite4 = new ArrayList<>();

	static {
		lzConvertCards.forEach(e -> lzComposite1.add(Arrays.asList(e)));
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				lzComposite2.add(Arrays.asList(lzConvertCards.get(i), lzConvertCards.get(j)));
			}
		}
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				for (int k = 0; k < 13; k++) {
					lzComposite3.add(Arrays.asList(lzConvertCards.get(i), lzConvertCards.get(j), lzConvertCards.get(k)));
				}
			}
		}
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				for (int k = 0; k < 13; k++) {
					for (int l = 0; l < 13; l++) {
						lzComposite4.add(Arrays.asList(lzConvertCards.get(i), lzConvertCards.get(j), lzConvertCards.get(k), PokerCard.lzConvertCards.get(l)));
					}
				}
			}
		}
		long time1 = System.currentTimeMillis();
		filterRepeatedComposite(lzComposite1);
		long time2 = System.currentTimeMillis();
		filterRepeatedComposite(lzComposite2);
		long time3 = System.currentTimeMillis();
		filterRepeatedComposite(lzComposite3);
		long time4 = System.currentTimeMillis();
		filterRepeatedComposite(lzComposite4);
		long time5 = System.currentTimeMillis();

		System.out.println("1:" + (time2 - time2) + " 2:" + (time3 - time2) + " 3:" + (time4 - time3) + "4:" + (time5 - time4));
	}

	static void filterRepeatedComposite(List<List<PokerCard>> cardGroupList) {
		List<List<PokerCard>> temp = new ArrayList<>();
		Iterator<List<PokerCard>> iter = cardGroupList.iterator();
		while (iter.hasNext()) {
			List<PokerCard> cardList = iter.next();
			if (contain(temp, cardList)) {
				iter.remove();
			} else {
				temp.add(cardList);
			}
		}
	}

	static boolean contain(List<List<PokerCard>> tempList, List<PokerCard> cardList) {
		for (List<PokerCard> pokerCards : tempList) {
			List<PokerCard> temp = new ArrayList<>(pokerCards);
			temp.removeAll(cardList);
			return temp.size() == 0;
		}
		return false;
	}


	public boolean gtOther(PokerCard card) {
		if (getPokerValue() > card.getPokerValue()) {
			return true;
		} else if (getPokerValue() == card.getPokerValue()) {
			return getPokerType().getValue() > card.getPokerType().getValue();
		} else {
			return false;
		}
	}

	public int getPokerValue() {
		int value = this.key;
		value = value & 0x00ff;
		return value;
	}

	public int getZjhPokerValue() {
		if (this == FANG_1
				|| this == MEI_1
				|| this == HONG_1
				|| this == HEI_1) {
			return getPokerValue() + 13;
		}
		return getPokerValue();
	}


	//用来比较的值
	public int getCompareValue() {
		if (this == JOKER_ONE) {
			return getPokerValue() + 15;
		} else if (this == JOKER_TWO) {
			return getPokerValue() + 16;
		} else {
			if (getPokerValue() < 3) {
				return getPokerValue() + 13;
			} else {
				return getPokerValue();
			}
		}
	}

	public int getKey() {
		return key;
	}

	public int getValue() {
		return value;
	}

	PokerCard(int key, int value) {
		this.key = key;
		this.value = value;
	}

	private static Map<Integer, PokerCard> idCaches = new ConcurrentHashMap<>();

	public static PokerCard getByValue(int value) {
		PokerCard cacheCard = idCaches.get(value);
		if (cacheCard != null) {
			return cacheCard;
		}
		for (PokerCard card : values()) {
			if (card.getKey() == value) {
				idCaches.put(value, card);
				return card;
			}
		}
		return null;
	}

}
