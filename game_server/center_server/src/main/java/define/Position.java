package define;

/**
 * Created by hhhh on 2017/5/11.
 */
public enum  Position {
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),;

	private int value;

	Position(int value){
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static Position getByValue(int value) {
		for (Position position : values()) {
			if (position.getValue() == value) {
				return position;
			}
		}
		return null;
	}
}
