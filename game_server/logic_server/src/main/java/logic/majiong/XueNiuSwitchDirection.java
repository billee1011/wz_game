package logic.majiong;

/**
 * Created by Administrator on 2016/12/17.
 */
public enum XueNiuSwitchDirection {
	SHUN(1),
	NI(2),
	DUI(3),;

	XueNiuSwitchDirection(int value) {
		this.value = value;
	}

	private int value;

	public int getValue() {
		return this.value;
	}

	public static XueNiuSwitchDirection getByValue(int value) {
		for (XueNiuSwitchDirection direction : values()) {
			if (direction.getValue() == value) {
				return direction;
			}
		}
		return null;
	}
}
