package logic.majiong;

/**
 * Created by Administrator on 2016/12/17.
 */
public enum MajongType {
	WAN(1),
	TONG(2),
	TIAO(3),
	FENG(4),
	ZI(5),
	;

	private int value;

	MajongType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static MajongType getByValue(int value) {
		for (MajongType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
