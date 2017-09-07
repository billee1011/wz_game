package define;

/**
 * Created by think on 2017/9/7.
 */
public enum EntityType {
	CAHRACTER(1),
	HERO(2),;

	private int value;

	EntityType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
