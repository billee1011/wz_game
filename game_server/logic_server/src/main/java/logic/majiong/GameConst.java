package logic.majiong;

import logic.poker.PokerCard;

import java.util.*;
import java.util.stream.Collectors;

public class GameConst {
	public static int PERSONEL_OVER_MYSELF_TIME = 24 * 60 * 60 * 1000;	// 私房結束自己的時間
//	public static int PERSONEL_OVER_MYSELF_TIME = 60 * 1000;	// 私房結束自己的時間

	public static int MO_HUA_TIME = 750;  // 摸花牌的时间

	public static final int WAN_1 = 1;

	public static final int Hu_MASK = 100;
	public static final int AN_MASK = 1000;
	public static final int WAN_2 = 2;
	public static final int WAN_3 = 3;
	public static final int WAN_4 = 4;
	public static final int WAN_5 = 5;
	public static final int WAN_6 = 6;
	public static final int WAN_7 = 7;
	public static final int WAN_8 = 8;
	public static final int WAN_9 = 9;

	public static final int TONG_1 = 11;
	public static final int TONG_2 = 12;
	public static final int TONG_3 = 13;
	public static final int TONG_4 = 14;
	public static final int TONG_5 = 15;
	public static final int TONG_6 = 16;
	public static final int TONG_7 = 17;
	public static final int TONG_8 = 18;
	public static final int TONG_9 = 19;

	public static final int TIAO_1 = 21;
	public static final int TIAO_2 = 22;
	public static final int TIAO_3 = 23;
	public static final int TIAO_4 = 24;
	public static final int TIAO_5 = 25;
	public static final int TIAO_6 = 26;
	public static final int TIAO_7 = 27;
	public static final int TIAO_8 = 28;
	public static final int TIAO_9 = 29;


	public static final int DONG_FENG = 31;
	public static final int NAN_FENG = 32;
	public static final int XI_FENG = 33;
	public static final int BEI_FENG = 34;

	public static final int ZHONG = 41;
	public static final int FACAI = 42;
	public static final int BAIBAN = 43;

	public static final int CHUN = 51;
	public static final int XIA = 52;
	public static final int QIU = 53;
	public static final int DONG = 54;
	public static final int MEI = 55;
	public static final int LAN = 56;
	public static final int ZHU = 57;
	public static final int JU = 58;

	public static List<Integer> XUELIU_POOL = new ArrayList<>();

	static {
		for (int i = 0; i < 4; i++) {
			for (int j = WAN_1; j < WAN_9 + 1; j++) {
				XUELIU_POOL.add(j);
			}
			for (int j = TONG_1; j < TONG_9 + 1; j++) {
				XUELIU_POOL.add(j);
			}
			for (int j = TIAO_1; j < TIAO_9 + 1; j++) {
				XUELIU_POOL.add(j);
			}
		}
	}


	public static List<Integer> MAJIANG_POOL = new ArrayList<>();

	static {
		for (int i = 0; i < 4; i++) {
			for (int j = WAN_1; j < WAN_9 + 1; j++) {
				MAJIANG_POOL.add(j);
			}
			for (int j = DONG_FENG; j < BEI_FENG + 1; j++) {
				MAJIANG_POOL.add(j);
			}
			for (int j = ZHONG; j < BAIBAN + 1; j++) {
				MAJIANG_POOL.add(j);
			}
		}
		MAJIANG_POOL.add(CHUN);
		MAJIANG_POOL.add(XIA);
		MAJIANG_POOL.add(QIU);
		MAJIANG_POOL.add(DONG);
		MAJIANG_POOL.add(MEI);
		MAJIANG_POOL.add(LAN);
		MAJIANG_POOL.add(ZHU);
		MAJIANG_POOL.add(JU);
	}

	public static List<PokerCard> ONE_NORMAL_POKER = new ArrayList<>();


	static {
		for (PokerCard card : PokerCard.values()) {
			ONE_NORMAL_POKER.add(card);
		}
	}

	public static List<PokerCard> COUPLE_DDZ_CARD = new ArrayList<>();

	static {
		for (PokerCard card : PokerCard.values()) {
			if (card.getPokerValue() == 3 || card.getPokerValue() == 4) {
				continue;
			}
			COUPLE_DDZ_CARD.add(card);
		}
	}

	public static Queue<List<Integer>> TEST_POOL_LIST = new LinkedList<>();

	public static void addTestMajiangPool(List<Integer> list) {
		TEST_POOL_LIST.add(list);
	}

	public static List<Integer> consumeMajiangPool() {
		return TEST_POOL_LIST.poll();
	}


	public static List<Integer> getXueliuPoolCopy() {
		return new ArrayList<>(XUELIU_POOL);
	}


	public static List<Integer> getMajiangPoolCopy() {
		return new ArrayList<>(MAJIANG_POOL);
	}

	public static List<PokerCard> getOneCardPool() {
		return new ArrayList<>(Arrays.asList(PokerCard.values()));
	}

	public static List<PokerCard> getOneCardPoolWithoutJoker() {
		List<PokerCard> cardList = new ArrayList<>(Arrays.asList(PokerCard.values()));
		cardList.remove(PokerCard.JOKER_ONE);
		cardList.remove(PokerCard.JOKER_TWO);
		return cardList;
	}

	public static List<PokerCard> getCouplePokerCards() {
		return Arrays.stream(PokerCard.values()).filter(e -> e.getPokerValue() != 3 && e.getPokerValue() != 4).collect(Collectors.toList());
	}

	public static List<PokerCard> getOneNormalPokerCopy() {
		return new ArrayList<>(ONE_NORMAL_POKER);
	}


	public static final int NIUNIU_CHIP_TIME = 20;
	public static final int NIU_NIU_CAL_TIME = 23;

}
