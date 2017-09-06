package logic.majiong;

public enum MajiangOperationType {
	GUO_GUO(0, 0),
	GANG(1, 3),
	KE(2, 2),
	CHI(3, 1),
	TING(4, -1),
	HU(5, 4),
	GUO(6, 0),
	DISCARD(7, -1),            //打牌
	MOPAI(8, -1),                //摸牌
	;

	private int value;

	private int priority;

	MajiangOperationType(int value, int priority) {
		this.value = value;
		this.priority = priority;
	}

	public int getPriority() {
		return this.priority;
	}

	public int getValue() {
		return this.value;
	}

	public static MajiangOperationType getByValue(int value) {
		for (MajiangOperationType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
