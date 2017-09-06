package logic.majiong.cpstragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;

import java.util.*;

public class OneStragety {
	//一般高
	public static boolean one(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<Integer> chiList = new ArrayList<>(chi);
		for (Integer value : chiList) {
			if (GameUtil.getCountOfValue(chiList, value) >= 2) {
				return true;
			}
		}
		return false;
	}

	//连六
	public static boolean two(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
//		List<Integer> chiList = new ArrayList<>(chi);
//		Set<Integer> result = new HashSet<>();
//		for (Integer value : chiList) {
//			if (result.size() == 0) {
//				result.add(value);
//				result.add(value + 1);
//				result.add(value + 2);
//			} else {
//				if (islianOk(result, value)) {
//					result.add(value);
//				}
//				if (islianOk(result, value + 1)) {
//					result.add(value + 1);
//				}
//				if (islianOk(result, value + 2)) {
//					result.add(value + 2);
//				}
//			}
//		}
//		return result.size() >= 6;
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
		for (Integer value : remain){
			if (GameUtil.isWanCard(value)) {
				wanSet.add(value);
			}
		}
		if (wanSet.size() < 6){
			return false;
		}
		for (int i = GameConst.WAN_1; i <= GameConst.WAN_9 - (6 - 1); i++) {
			int count = 0;
			for (int j = 0; j < 6; j++) {
				if (wanSet.contains(i + j)) {
					count++;
				} else {
					break;
				}
				if (count >= 6) {
					return true;
				}
			}
		}
		return false;
	}

//	private static boolean islianOk(Set<Integer> result, int value) {
//		for (Integer i : result) {
//			if (Math.abs(i - value) <= 1) {
//				return true;
//			}
//		}
//		return false;
//	}

	//老少副
	public static boolean three(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<Integer> chiList = new ArrayList<>(chi);
		if (chiList.contains(1) && chiList.contains(7)) {
			return true;
		}
		return false;
	}

	//花牌,不加入计算番数策略,这个相当于已经完成了
	public static boolean four(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//明杠一个
	public static boolean five(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (value < GameConst.AN_MASK) {
				return true;
			}
		}
		return false;
	}

	//边张
	public static boolean six(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}


	//坎张
	public static boolean seven(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//单掉将
	public static boolean eight(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

	//自摸
	public static boolean nine(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return false;
	}

}
