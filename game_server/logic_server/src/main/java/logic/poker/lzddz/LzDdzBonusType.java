package logic.poker.lzddz;

/**
 * Created by think on 2017/4/25.
 */
public enum LzDdzBonusType {
	RUAN_RUAN(0x11),
	YING_RUAN(0x21),
	YING_YING(0x22),
	RUAN_YING(0x12),;

	private int value;

	LzDdzBonusType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static LzDdzBonusType getByValue(int src, int target) {
		for (LzDdzBonusType type : values()) {
			int one = type.getValue() >>> 4;
			int two = type.getValue() & 0x0f;
			if (one == src && two == target) {
				return type;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(LzDdzBonusType.getByValue(1, 1));
		System.out.println(LzDdzBonusType.getByValue(1, 2));
		System.out.println(LzDdzBonusType.getByValue(2, 1));
		System.out.println(LzDdzBonusType.getByValue(2, 2));
	}
}
