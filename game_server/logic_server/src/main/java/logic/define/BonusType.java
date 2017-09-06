package logic.define;

/**
 * Created by Administrator on 2017/2/20.
 */
public enum BonusType {
	UN_KNOW(0, 9999),
	EIGHT(1, 16),
	SIXTEEN(2, 32),
	THIRTY_TWO(3, 64),;

	private int times;

	private int value;

	BonusType(int value, int times) {
		this.value = value;
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

	public int getValue() {
		return value;
	}

	public static BonusType getByValue(int value) {
		for (BonusType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return BonusType.UN_KNOW;
	}
}
