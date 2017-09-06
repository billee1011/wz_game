package logic.majiong;

public enum MenFengType {
	DONG(1),
	NAN(2),
	XI(3),
	BEI(4);

	private int value;

	private MenFengType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public MenFengType next() {
		switch (this) {
			case DONG:
				return NAN;
			case NAN:
				return XI;
			case XI:
				return BEI;
			case BEI:
				return DONG;
			default:
				return null;
		}
	}
}
