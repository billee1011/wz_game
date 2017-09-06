package logic.poker.ddz;

/**
 * Created by Administrator on 2017/3/14.
 */
public enum DdzPos {
	ONE(1),
	TWO(2),
	THREE(3),;

	private int value;

	DdzPos(int value) {
		this.value = value;
	}


	public DdzPos nextPos() {
		if (this.value == 3) {
			return ONE;
		}
		return getByValue(value + 1);
	}

	public DdzPos prePos() {
		if (this.value == 1) {
			return THREE;
		}
		return getByValue(value - 1);
	}


	public int getValue() {
		return this.value;
	}

	public static DdzPos getByValue(int value) {
		for (DdzPos pos : values()) {
			if (pos.getValue() == value) {
				return pos;
			}
		}
		return null;
	}

}
