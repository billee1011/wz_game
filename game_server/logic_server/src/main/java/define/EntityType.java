package define;

/**
 * Created by think on 2017/9/7.
 */
public enum EntityType {
	CHARACTER(1),
	HERO(2),
	EQUIP(3),;

	private int value;

	EntityType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
