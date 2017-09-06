package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.*;

public class EightyEightStragety {

	//大四喜
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int gangKe = 0;
		for (Integer value : gang) {
			if (GameUtil.isFengCard(GameUtil.getRealValue(value)))
				gangKe++;
		}
		for (Integer value : ke) {
			if (GameUtil.isFengCard(GameUtil.getRealValue(value))) {
				gangKe++;
			}
		}
		return gangKe >= 4;
	}

	//大三元
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (ke.size() + gang.size() < 3) {
			return false;
		}
		if ((ke.contains(GameConst.ZHONG) || ke.contains(GameConst.ZHONG + GameConst.AN_MASK) || gang.contains(GameConst.ZHONG) || gang.contains(GameConst.ZHONG + GameConst.AN_MASK))
				&& (ke.contains(GameConst.FACAI) || ke.contains(GameConst.FACAI + GameConst.AN_MASK) || gang.contains(GameConst.FACAI) || gang.contains(GameConst.FACAI + GameConst.AN_MASK))
				&& (ke.contains(GameConst.BAIBAN) || ke.contains(GameConst.BAIBAN + GameConst.AN_MASK) || gang.contains(GameConst.BAIBAN) || gang.contains(GameConst.BAIBAN + GameConst.AN_MASK))) {
			return true;
		}
		return false;
	}

	//9宝连灯 特殊处理
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (gang.size() > 0 || ke.size() > 0 || chi.size() > 0) {
			return false;
		}
		for (Integer value : remain) {
			if (!GameUtil.isWanCard(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		if (GameUtil.getCountOfValue(remain, GameConst.WAN_1) < 3 || GameUtil.getCountOfValue(remain, GameConst.WAN_9) < 3) {
			return false;
		}
		for (int i = GameConst.WAN_2; i < GameConst.WAN_9; i++) {
			if (GameUtil.getCountOfValue(remain, i) < 1) {
				return false;
			}
		}
		return true;
	}

	//大于5
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.gtWan5(realValue)) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (!GameUtil.gtWan5(value)) {
				return false;
			}
		}
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.gtWan5(realValue)) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.gtWan5(value)) {
				return false;
			}
		}
		return true;
	}

	//小于5
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
//		if (remain.size() != 7) {
//			return false;
//		}
		for (Integer value : gang) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.ltWan5(realValue)) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (!GameUtil.ltWan5(value + 2)) {
				return false;
			}
		}
		for (Integer value : ke) {
			int realValue = GameUtil.getRealValue(value);
			if (!GameUtil.ltWan5(realValue)) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.ltWan5(value)) {
				return false;
			}
		}
		return true;
	}

	//大七星
	public static boolean six(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (GameUtil.getCountOfValue(remain, GameConst.DONG_FENG) < 1 || GameUtil.getCountOfValue(remain, GameConst.NAN_FENG) < 1
				|| GameUtil.getCountOfValue(remain, GameConst.XI_FENG) < 1 || GameUtil.getCountOfValue(remain, GameConst.BEI_FENG) < 1
				|| GameUtil.getCountOfValue(remain, GameConst.ZHONG) < 1 || GameUtil.getCountOfValue(remain, GameConst.FACAI) < 1
				|| GameUtil.getCountOfValue(remain, GameConst.BAIBAN) < 1) {
			return false;
		}
		return true;
	}

	//连七对的计算, 是七对之后才会考虑de,1对应该不算其他的了 可以减少许多计算量
	public static boolean seven(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (remain.size() != 7) {
			return false;
		}
		Set<Integer> result = new HashSet<>();
		for (Integer value : remain) {
			if (!GameUtil.isWanCard(value)) {
				return false;
			}
			result.add(value);
		}
		if (result.size() != 7) {
			return false;
		}
		if ((!remain.contains(GameConst.WAN_1) && !remain.contains(GameConst.WAN_9))
				|| (!remain.contains(GameConst.WAN_1) && !remain.contains(GameConst.WAN_2))
				|| (!remain.contains(GameConst.WAN_8) && !remain.contains(GameConst.WAN_9))) {
			return true;
		}
		return false;
	}

	public static boolean eight(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	public static boolean nine(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	public static boolean ten(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain){
		return gang.size() == 4;
	}
}
