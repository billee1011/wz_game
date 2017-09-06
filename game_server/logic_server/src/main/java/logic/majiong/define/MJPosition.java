package logic.majiong.define;

/**
 * Created by Administrator on 2016/12/12.
 */
public enum MJPosition {
	EAST(1),
	SOUTH(2),
	WEST(3),
	NORTH(4),;

	MJPosition(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private int value;

	public MJPosition nextPosition() {
		if (this.value == 4) {
			return EAST;
		} else {
			return getByValue(this.value + 1);
		}
	}

	public MJPosition oppositePosition() {
		return nextPosition().nextPosition();
	}

	public MJPosition prePosition() {
		if (this.value == 1) {
			return NORTH;
		} else {
			return getByValue(this.value - 1);
		}
	}

	//判断相隔距离
	public int minusPosition(MJPosition type) {
		if (this.value < type.getValue()) {
			return type.getValue() - this.value;
		} else {
			return (4 - this.value) + type.getValue();
		}
	}

	public static MJPosition getByValue(int value) {
		for (MJPosition postion : values()) {
			if (postion.getValue() == value) {
				return postion;
			}
		}
		return null;
	}
}
