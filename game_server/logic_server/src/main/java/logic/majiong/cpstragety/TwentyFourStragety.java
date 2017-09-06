package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class TwentyFourStragety {

	//四字刻
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int count = 0;
		for (Integer value : gang) {
			if (!GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				count++;
			}
		}
		for (Integer value : ke) {
			if (!GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				count++;
			}
		}
		if (count >= 4) {
			return true;
		}
		return false;
	}

	//大三风
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int fengKeCount = 0;
		for (Integer value : ke) {
			if (GameUtil.isFengCard(GameUtil.getRealValue(value))) {
				fengKeCount++;
			}
		}
		if (fengKeCount >= 3) {
			return true;
		}
		return false;
	}

	//三同顺
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<Integer> chiList = new ArrayList<>(chi);
		int size = chiList.size();
		for (int i = 0; i < size; i++) {
			if (GameUtil.getCountOfValue(chiList, chiList.get(i)) >= 3) {
				return true;
			}
		}
		return false;
	}

	//七对单独处理下,获取抽取出来
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return remain.size() == 7;
	}

	//三连刻
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.isWanCard(realValue)) {
				continue;
			}
			if ((ke.contains(realValue + 1) || ke.contains(realValue + 1 + GameConst.AN_MASK))
					&& (ke.contains(realValue + 2) || ke.contains(realValue + 2 + GameConst.AN_MASK))) {
				return true;
			}
		}
		return false;
	}
}
