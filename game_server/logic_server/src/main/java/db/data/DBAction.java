package db.data;

public enum DBAction {

	// 0. all
	ALL(0), //
	PLAYER(1),
	HERO(2),
	EQUIP(3),;


	private int value;

	private DBAction(int v) {
		this.value = v;
	}

	public int getValue() {
		return this.value;
	}

	public static DBAction getByValue(int v) {
		for (DBAction action : values()) {
			if (action.getValue() == v) {
				return action;
			}
		}
		return null;
	}

	public static DBAction getByName(String name) {
		if (name == null || (name = name.trim()).isEmpty()) {
			return null;
		}
		for (DBAction action : values()) {
			if (action.toString().equalsIgnoreCase(name)) {
				return action;
			}
		}
		return null;
	}
}
