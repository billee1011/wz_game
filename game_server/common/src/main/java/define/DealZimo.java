package define;

/**
 * Created by Administrator on 2017/1/18.
 */
public enum DealZimo {
	ZIMO_JIAFAN(1),
	ZIMO_JIABEI(2),
	ZIMO_BUJIABEI(3),;

	private int value;

	DealZimo(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public int getResultFan(int fan) {
		if (getValue() == 1) {
			return fan + 1;
		} else if (getValue() == 2) {
			return fan << 1;
		} else {
			return fan;
		}
	}

	public static DealZimo getByValue(int value) {
		for (DealZimo type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return ZIMO_JIABEI;
	}
}
