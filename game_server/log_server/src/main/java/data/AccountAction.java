package data;

/**
 * Created by think on 2017/3/21.
 */
public enum AccountAction {
	CREATE(1),
	REGISTER(2),;
	private int value;

	public int getValue() {
		return this.value;
	}

	AccountAction(int value) {
		this.value = value;
	}
}
