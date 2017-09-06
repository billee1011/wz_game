package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.*;

public class SixteenStragety {

	//清龙 1-9万
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		Set<Integer> wanSet = new HashSet<>();
		for (Integer value : gang) {
			int realValue = GameUtil.getRealValue(value);
			if (GameUtil.isWanCard(realValue)) {
				wanSet.add(realValue);
			}
		}
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (GameUtil.isWanCard(realValue)) {
				wanSet.add(realValue);
			}
		}
		for (Integer value : chi) {
			if (GameUtil.isWanCard(value)) {
				wanSet.add(value);
				wanSet.add(value + 1);
				wanSet.add(value + 2);
			}
		}
		if (GameUtil.isWanCard(remain.get(0))) {
			wanSet.add(remain.get(0));
		}
		if (wanSet.size() == 9) {
			return true;
		}
		return false;
	}

	//三步高
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<Integer> chiList = new ArrayList<>(chi);
		if (chiList.size() < 3) {
			return false;
		}
		int size = chiList.size();
		for (int i = 0; i < size; i++) {
			if (chiList.contains(chiList.get(i) + 1) && chiList.contains(chiList.get(i) + 2)) {
				return true;
			}
		}
		return false;
	}

	//摸到全部的花牌,不在策略模式里面处理
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//三暗刻
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int anKeCount = 0;
		for (Integer value : ke) {
			if (value > GameConst.AN_MASK) {
				anKeCount++;
			}
		}
		for (Integer value : gang) {
			if (value > GameConst.AN_MASK) {
				anKeCount++;
			}
		}
		if (anKeCount >= 3) {
			return true;
		}
		return false;
	}

	//清一色
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (!GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (!GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.isWanCard(value)) {
				return false;
			}
		}
		return true;
	}

}
