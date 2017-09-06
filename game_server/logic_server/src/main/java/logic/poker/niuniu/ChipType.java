package logic.poker.niuniu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */
public enum ChipType {
//	ONE(1, 10),
	TWO(3, 100),
	THREE(4, 500),
	FOUR(5, 1000),
	FIVE(7, 10000),
	SIX(6, 5000),
	EIGHT(8, 50000),
	;

	private int id;
	private int value;

	ChipType(int id, int value) {
		this.id = id;
		this.value = value;
	}

	public static List<Integer> getChipList(int chipValue) {
		List<Integer> result;
		if (chipValue > EIGHT.getValue()) {
			return null;                    //不可能大于最大筹码 还需要 获得这个接口
		}
		result = new ArrayList<>();
		for (int i = EIGHT.getId(); i >= TWO.getId(); i--) {
			ChipType type = getTypeById(i);
			if (type == null) {
				continue;
			}
			int count = chipValue / type.getValue();
			for (int j = 0; j < count; j++) {
				result.add(i);
			}
			chipValue -= count * type.getValue();
		}
		return result;
	}


	public int getId() {
		return id;
	}

	public int getValue() {
		return value;
	}

	public static ChipType getTypeById(int id) {
		for (ChipType type : values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}

}
