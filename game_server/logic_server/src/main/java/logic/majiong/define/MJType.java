package logic.majiong.define;

/**
 * Created by Administrator on 2016/12/12.
 */
public enum MJType {
	COUPLE_MJ(1),
	XUELIU(2),;
	private int value;

	MJType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static MJType getByValue(int value) {
		for (MJType mjType : values()) {
			if (mjType.getValue() == value) {
				return mjType;
			}
		}
		return null;
	}

}
