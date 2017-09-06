package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class FourStragety {
	//全带幺
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (!GameUtil.isYaoJiuZiCard(value)) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.isYaoJiuZiCard(value)) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (!GameUtil.isYaoJiuZiCard(value)) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (value != GameConst.WAN_1 && value != GameConst.WAN_7) {
				return false;
			}
		}
		return true;
	}


	//双明杠
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int gangCount = 0;
		for (Integer value : gang) {
			if (value < GameConst.AN_MASK) {
				gangCount++;
			}
		}
		return gangCount >= 2;
	}

	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//和绝张,牌面有3个才算绝章
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}
}
