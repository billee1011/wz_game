package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class FortyEightStragety {

	//四同顺
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (chi.size() < 4) {
			return false;
		}
		List<Integer> chiList = new ArrayList<>(chi);
		for (Integer value : chiList) {
			if (GameUtil.getCountOfValue(chiList, value) >= 4) {
				return true;
			}
			return false;
		}
		return true;
	}

	//三元七对子
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (remain.size() != 7) {
			return false;
		}
		if (remain.contains(GameConst.ZHONG) && remain.contains(GameConst.FACAI) && remain.contains(GameConst.BAIBAN)) {
			return true;
		}
		return false;
	}

	//四喜七对子
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (remain.size() != 7) {
			return false;
		}
		if (remain.contains(GameConst.DONG_FENG) && remain.contains(GameConst.NAN_FENG)
				&& remain.contains(GameConst.XI_FENG) && remain.contains(GameConst.BEI_FENG)) {
			return true;
		}
		return false;
	}

	//四连刻
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.isWanCard(realValue)) {
				continue;
			}
			if ((ke.contains(realValue + 1) || ke.contains(realValue + 1 + GameConst.AN_MASK))
					&& (ke.contains(realValue + 2) || ke.contains(realValue + 2 + GameConst.AN_MASK))
					&& (ke.contains(realValue + 3) || ke.contains(realValue + 3 + GameConst.AN_MASK))) {
				return true;
			}
		}
		return false;
	}
}
