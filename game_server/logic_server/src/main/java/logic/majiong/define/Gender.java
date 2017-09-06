package logic.majiong.define;

public enum Gender {
	MALE(1),
	FEMALE(2),;
	private int value;

	Gender(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static Gender getByValue(int value) {
		for (Gender gender : values()) {
			if (gender.getValue() == value) {
				return gender;
			}
		}
		return null;
	}
}
