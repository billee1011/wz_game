package sql.define;

/**
 * Created by think on 2017/3/27.
 */
public enum EWhereType {
	EQUAL("="),
	GT(">"),
	LT("<"),
	LIKE("like"),;

	private String signal;

	public String getSignal() {
		return signal;
	}

	EWhereType(String signal) {
		this.signal = signal;
	}


}
