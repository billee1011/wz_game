package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ThirtyTwoStragety {

	//四步高
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (chi.size() < 4) {
			return false;
		}
		for (Integer value : chi) {
			if (chi.contains(value + 1) && chi.contains(value + 2) && chi.contains(value + 3)) {
				return true;
			}
		}
		return false;
	}

	//混幺9
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : chi) {
			if (value != GameConst.WAN_1 && value != GameConst.WAN_7) {
				return false;
			}
		}
		for (Integer value : gang) {
			if (!GameUtil.isYaoJiuZiCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (!GameUtil.isYaoJiuZiCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.isYaoJiuZiCard(value)) {
				return false;
			}
		}
		return true;
	}

	//三个杠
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return gang.size() == 3;
	}

	//天ting,是否天听由牌桌上的信息决定, 到时候传进来
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}
}
