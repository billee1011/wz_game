package logic.poker.niuniu;

/**
 * Created by Administrator on 2016/12/13.
 */
public enum NiuResult {
	NO_NIU(0, 1),
	NIU_1(1, 1),
	NIU_2(2, 1),
	NIU_3(3, 1),
	NIU_4(4, 1),
	NIU_5(5, 1),
	NIU_6(6, 1),
	NIU_7(7, 2),
	NIU_8(8, 2),
	NIU_9(9, 2),
	NIU_NIU(10, 3),
//	FOUR_NIU(11, 3),
	FIVE_NIU(12, 4),
	BOMB_NIU(13, 4),
	FIVE_SMALL_NIU(14, 4),;


	NiuResult(int value, int times) {
		this.value = value;
		this.times = times;
	}

//	public int getTimes() {
//		return times;
//	}

	public int getValue() {
		return this.value;
	}

	private int value;

	private int times;

	public static NiuResult getByValue(int value) {
		for (NiuResult result : values()) {
			if (result.getValue() == value) {
				return result;
			}
		}
		return null;
	}
}
