package data;

/**
 * Created by think on 2017/3/21.
 */
public enum OnlineAction {
	LOGIN(1),
	LOGOUT(2),
	;

	private int value;

	OnlineAction(int value) {
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}
}
