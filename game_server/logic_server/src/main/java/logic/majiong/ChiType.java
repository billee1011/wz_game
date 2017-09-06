package logic.majiong;


public enum ChiType {
	LEFT(1),
	MIDDLE(2),
	RIGHT(3),;

	private int value;

	ChiType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static ChiType getByValue(int value) {
		for (ChiType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
