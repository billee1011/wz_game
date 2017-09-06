package data;

/**
 * Created by think on 2017/3/21.
 */
public enum MoneyAction {
	GAIN(1),
	LOSE(2),
	AGENT_WITHDRAW(4),
	;

	private int value;

	public int getValue(){
		return this.value;
	}

	MoneyAction(int value) {
		this.value = value;
	}
}
