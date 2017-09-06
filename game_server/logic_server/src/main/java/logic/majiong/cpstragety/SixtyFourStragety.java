package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class SixtyFourStragety {

	//小四喜
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!GameUtil.isFengCard(remain.get(0))) {
			return false;
		}
		int fengKeCount = 0;
		for (Integer value : ke) {
			if (GameUtil.isFengCard(GameUtil.getRealValue(value))) {
				fengKeCount++;
			}
		}
		return fengKeCount >= 3;
	}

	//小三元
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!GameUtil.isJianCard(remain.get(0))) {
			return false;
		}
		int jianKeCount = 0;
		for (Integer value : ke) {
			if (GameUtil.isJianCard(GameUtil.getRealValue(value))) {
				jianKeCount++;
			}
		}
		if (jianKeCount < 2) {
			return false;
		}
		if (!GameUtil.isJianCard(remain.get(0))) {
			return false;
		}
		return true;
	}

	//四暗刻
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int anGangKeCount = 0;
		for (Integer value : ke) {
			if (value > GameConst.AN_MASK) {
				anGangKeCount++;
			}
		}
		for (Integer value : gang) {
			if (value > GameConst.AN_MASK) {
				anGangKeCount++;
			}
		}
		return anGangKeCount >= 4;
	}

	//双龙会 一种花色的两个老少副
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (remain.get(0) != GameConst.WAN_5) {
			return false;
		}
		List<Integer> chiList = new ArrayList<>(chi);
		if (GameUtil.getCountOfValue(chiList, 1) != 2 || GameUtil.getCountOfValue(chiList, 7) != 2) {
			return false;
		}
		return true;
	}

	//字一色
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (chi.size() > 0) {
			return false;
		}
		for (Integer value : gang) {
			if (GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (GameUtil.isWanCard(value)) {
				return false;
			}
		}
		return true;
	}

	//庄家打第一张牌胡 , 庄家暗杠不算
	public static boolean six(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		//to do
		return false;
	}
}
