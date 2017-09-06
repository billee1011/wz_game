package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class TwoStragety {
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//箭客
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (realValue == GameConst.ZHONG || realValue == GameConst.FACAI || realValue == GameConst.BAIBAN) {
				return true;
			}
		}
		for (Integer value : gang) {
			int realValue = GameUtil.getRealValue(value);
			if (realValue == GameConst.ZHONG || realValue == GameConst.FACAI || realValue == GameConst.BAIBAN) {
				return true;
			}
		}
		return false;
	}

	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return chi.size() >= 4;
	}

	//四归一
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<Integer> allCardsButGang = new ArrayList<>();
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			allCardsButGang.add(realValue);
			allCardsButGang.add(realValue);
			allCardsButGang.add(realValue);
		}
		for (Integer value : chi) {
			allCardsButGang.add(value);
			allCardsButGang.add(value + 1);
			allCardsButGang.add(value + 2);
		}
		allCardsButGang.addAll(remain);
		for (Integer value : allCardsButGang) {
			if (GameUtil.getCountOfValue(allCardsButGang, value) >= 4) {
				return true;
			}
		}
		return false;
	}

	//断幺 , 没有1, 9 ,字
	public static boolean six(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (GameUtil.isYaoJiuZiCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.isYaoJiuZiCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (value == GameConst.WAN_1 || value == GameConst.WAN_7) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (GameUtil.isYaoJiuZiCard(value)) {
				return false;
			}
		}
		return true;
	}

	//双暗客
	public static boolean seven(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
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
		if (anKeCount >= 2) {
			return true;
		}
		return false;
	}

	//暗杠
	public static boolean eight(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (value > GameConst.AN_MASK) {
				return true;
			}
		}
		return false;
	}

	//门前清
	public static boolean nine(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//报听
	public static boolean ten(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}
}
