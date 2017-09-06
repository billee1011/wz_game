package logic.poker.ddz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/14.
 */
public enum DdzGroupType {
	SINGLE(1, 1, 12),                        //单张
	COUPLE(2, 2, 11),                        //对子
	TRIPLE(3, 3),                        //三张
	FOUR(4, 4, 1),                            //炸弹
	THREE_ONE(5, 4, 10),                    //三带一
	THREE_TWO(6, 5, 1),                    //三带二
	FOUR_TWO_ONE(7, 6, 3),                //四带2单
	FOUR_TWO_TWO(8, 8, 2),                //四带两双
	STRAIGHT_5(9, 5, 8),                    //5连
	STRAIGHT_6(10, 6, 8),
	STRAIGHT_7(11, 7, 8),
	STRAIGHT_8(12, 8, 8),
	STRAIGHT_9(13, 9, 8),
	STRAIGHT_10(14, 10, 8),
	STRAIGHT_11(15, 11, 8),
	STRAIGHT_12(16, 12, 8),
	PLANE_2_0(17, 6, 6),                    //2连飞机不带
	PLANE_2_1(18, 8, 5),                    //2飞机 带单
	PLANE_2_2(19, 10, 4),                    //2飞机 带双
	PLANE_3_0(20, 9, 6),                    //3飞机 不带
	PLANE_3_1(21, 12, 5),
	PLANE_3_2(22, 15, 4),
	PLANE_4_0(23, 12, 6),
	PLANE_4_1(24, 16, 5),
	PLANE_4_2(25, 20, 4),
	PLANE_5_0(26, 15, 6),
	PLANE_5_1(27, 20, 5),
	PLANE_6_0(28, 18, 6),
	COUPLE_THREE(29, 6, 7),            //三连对
	COUPLE_FOUR(30, 8, 7),
	COUPLE_FIVE(31, 10, 7),
	COUPLE_SIX(32, 12, 7),                //六连对,
	COUPLE_SEVEN(33, 14, 7),                //七连对
	COUPLE_EIGHT(34, 16, 7),                //八连对
	COUPLE_NINE(35, 18, 7),                //九连对
	COUPLE_TEN(36, 20, 7),                //十连队
	JOKER_BONUS(37, 2, 1),                //王炸
	;

	private static Map<Integer, List<DdzGroupType>> cardNum2Type = new HashMap<>();

	static {
		for (DdzGroupType type : values()) {
			List<DdzGroupType> typeList = cardNum2Type.get(type.getCardNum());
			if (typeList == null) {
				typeList = new ArrayList<>();
				cardNum2Type.put(type.getCardNum(), typeList);
			}
			typeList.add(type);
		}
	}

	private int value;

	private int cardNum;

	private int weight;

	DdzGroupType(int value, int cardNum) {
		this(value, cardNum, 999);
	}

	DdzGroupType(int value, int cardNum, int weight) {
		this.value = value;
		this.cardNum = cardNum;
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public int getValue() {
		return value;
	}


	public void setValue(int value) {
		this.value = value;
	}

	public int getCardNum() {
		return cardNum;
	}

	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}

	public static DdzGroupType getByValue(int value) {
		for (DdzGroupType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}

	public static List<DdzGroupType> getByCardNum(int cardNum) {
		return cardNum2Type.get(cardNum);
	}

}
