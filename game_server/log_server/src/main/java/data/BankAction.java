package data;

/**
 * Created by think on 2017/3/21.
 */
public enum BankAction {
	SAVE(1),
	WITHDRAW(2),
	AGENT_SAVE(3),
	AGENT_WITHDRAW(4),
	AGENT_TAX(5),
	;

	private int value;

	BankAction(int value) {
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}
}
