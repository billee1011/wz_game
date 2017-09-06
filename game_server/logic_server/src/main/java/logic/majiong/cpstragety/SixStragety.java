package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class SixStragety {

	//小三风
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!GameUtil.isFengCard(remain.get(0))) {
			return false;
		}
		int fengCount = 0;
		for (Integer value : gang) {
			int resultValue = value > GameConst.AN_MASK ? value - GameConst.AN_MASK : value;
			if (GameUtil.isFengCard(resultValue)) {
				fengCount++;
			}
		}
		for (Integer value : ke) {
			int resultValue = value > GameConst.AN_MASK ? value = GameConst.AN_MASK : value;
			if (GameUtil.isFengCard(resultValue)) {
				fengCount++;
			}
		}
		if (fengCount < 2) {
			return false;
		}
		return true;
	}

	//双箭刻
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int jianCount = 0;
		for (Integer value : gang) {
			if (GameUtil.isJianCard(GameUtil.getRealValue(value))) {
				jianCount++;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.isJianCard(GameUtil.getRealValue(value))) {
				jianCount++;
			}
		}
		if (jianCount < 2) {
			return false;
		}
		return true;
	}

	//碰碰和
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return (gang.size() + ke.size()) == 4;
	}

	//双暗杠
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int anGangCount = 0;
		for (Integer value : gang) {
			if (value > GameConst.AN_MASK) {
				anGangCount++;
			}
		}
		if (anGangCount >= 2) {
			return true;
		}
		return false;
	}

	//混一色
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		boolean hasWan = false;
		boolean hasZi = false;
		for (Integer value : gang) {
			if (GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				hasWan = true;
			} else {
				hasZi = true;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				hasWan = true;
			} else {
				hasZi = true;
			}
		}
		for (Integer value : chi) {
			if (GameUtil.isWanCard(value)) {
				hasWan = true;
			} else {
				hasZi = true;
			}
		}
		for (Integer value : remain) {
			if (GameUtil.isWanCard(value)) {
				hasWan = true;
			} else {
				hasZi = true;
			}
		}
		if (hasWan && hasZi) {
			return true;
		}
		return false;
	}

	//全求人
	public static boolean six(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}
}
