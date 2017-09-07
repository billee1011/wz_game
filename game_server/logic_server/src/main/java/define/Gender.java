package define;

/**
 * Created by think on 2017/9/7.
 */
public enum Gender {
	MALE(1),
	FEMALE(2),;


	int value;

	Gender(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

}
