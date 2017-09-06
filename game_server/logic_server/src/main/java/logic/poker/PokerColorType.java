package logic.poker;

/**
 * Created by Administrator on 2016/12/13.
 */
public enum PokerColorType {
	FANG(1),
	MEI(2),
	HONG(3),
	HEI(4),
	JOKER(5),
	;

	private int value;

	PokerColorType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static PokerColorType getByValue(int value) {
		for (PokerColorType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
