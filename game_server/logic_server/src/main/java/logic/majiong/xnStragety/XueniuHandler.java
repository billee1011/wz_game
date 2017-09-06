package logic.majiong.xnStragety;

import logic.majiong.GameConst;
import logic.majiong.GameUtil;
import logic.majiong.MajongType;
import logic.majiong.XueniuFanType;

import java.util.*;

/**
 * Created by Administrator on 2016/12/20.
 */
public class XueniuHandler {

	public static boolean handlerPinghu(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return true;
	}

	public static boolean handlerDuidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return (gang.size() + ke.size()) == 4;
	}

	public static boolean handlerQidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return remain.size() >= 7;
	}

	public static boolean handlerQingyise(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		MajongType type = GameUtil.getMajongTypeByValue(remain.get(0));
		if (type == null) {
			return false;
		}
		for (Integer value : gang) {
			if (GameUtil.getMajongTypeByValue(GameUtil.getRealValue(value)) != type) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.getMajongTypeByValue(GameUtil.getRealValue(value)) != type) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (GameUtil.getMajongTypeByValue(value) != type) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (GameUtil.getMajongTypeByValue(value) != type) {
				return false;
			}
		}
		return true;
	}

	public static boolean handlerDai19(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (!GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}

		LinkedList<Integer> keList = new LinkedList<>(ke);
		// 如果有123的连刻 或者 789 的连刻，则ok并且移除
		check123or789ke(keList);

		for (Integer value : keList) {
			if (!GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}

		for (Integer value : chi) {
			if (!isContextYaoJiu(value)) {
				return false;
			}
		}
		return true;
	}

	private static void check123or789ke(LinkedList<Integer> keList){
		if (keList.contains(GameConst.WAN_1 + GameConst.AN_MASK) && keList.contains(GameConst.WAN_2 + GameConst.AN_MASK) && keList.contains(GameConst.WAN_3 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.WAN_1 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.WAN_2 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.WAN_3 + GameConst.AN_MASK));
		}
		if (keList.contains(GameConst.WAN_7 + GameConst.AN_MASK) && keList.contains(GameConst.WAN_8 + GameConst.AN_MASK) && keList.contains(GameConst.WAN_9 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.WAN_7 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.WAN_8 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.WAN_9 + GameConst.AN_MASK));
		}
		if (keList.contains(GameConst.TONG_1 + GameConst.AN_MASK) && keList.contains(GameConst.TONG_2 + GameConst.AN_MASK) && keList.contains(GameConst.TONG_3 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.TONG_1 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TONG_2 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TONG_3 + GameConst.AN_MASK));
		}
		if (keList.contains(GameConst.TONG_7 + GameConst.AN_MASK) && keList.contains(GameConst.TONG_8 + GameConst.AN_MASK) && keList.contains(GameConst.TONG_9 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.TONG_7 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TONG_8 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TONG_9 + GameConst.AN_MASK));
		}
		if (keList.contains(GameConst.TIAO_1 + GameConst.AN_MASK) && keList.contains(GameConst.TIAO_2 + GameConst.AN_MASK) && keList.contains(GameConst.TIAO_3 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.TIAO_1 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TIAO_2 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TIAO_3 + GameConst.AN_MASK));
		}
		if (keList.contains(GameConst.TIAO_7 + GameConst.AN_MASK) && keList.contains(GameConst.TIAO_8 + GameConst.AN_MASK) && keList.contains(GameConst.TIAO_9 + GameConst.AN_MASK)) {
			keList.remove(Integer.valueOf(GameConst.TIAO_7 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TIAO_8 + GameConst.AN_MASK));
			keList.remove(Integer.valueOf(GameConst.TIAO_9 + GameConst.AN_MASK));
		}
	}

	/**
	 * 是否包含1 9
	 * @param value
	 * @return
	 */
	private static boolean isContextYaoJiu(int value) {
		return GameUtil.isYaojiuCard(value) || GameUtil.isYaojiuCard(value + 1) || GameUtil.isYaojiuCard(value + 2);
	}


	public static boolean handlerJingou(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		int count = 0;
		for (Integer value : gang) {
			if (value < GameConst.AN_MASK) {
				count++;
			}
		}
		for (Integer value : ke) {
			if (value < GameConst.AN_MASK) {
				count++;
			}
		}
		return count == 4;
	}

	public static boolean handlerLongqidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.QIDUI)) {
			return false;
		}
		for (int i = 0, size = remain.size(); i < size; i++) {
			if (GameUtil.getCountOfValue(remain, remain.get(i)) >= 2) {
				return true;
			}
		}
		return false;
	}

	public static boolean handlerQingduidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.DUIDUI)) {
			return false;
		}
		return true;
	}

	public static boolean handlerQingQidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.QIDUI)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		return true;
	}

	public static boolean handlerQing19(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.DAIYAOJIU)) {
			return false;
		}
		return true;
	}

	public static boolean handlerJiangjingou(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.JINGOU)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		return true;
	}



	// 將金鉤
	public static boolean handlerJiangjingou2(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain){
		if (!fanList.contains(XueniuFanType.JINGOU)) {
			return false;
		}

		for (Integer value : remain) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}

		for (Integer value : gang) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}

		for (Integer value : ke) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}

		for (Integer value : chi) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}

		return true;
	}

	public static boolean handlerQinglongqidui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.LONGQIDUI)) {
			return false;
		}
		return true;
	}

	public static boolean handler18luohan(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		return gang.size() == 4;
	}

	public static boolean handlerQing18luohan(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.SHIBALUOHAN)) {
			return false;
		}
		if (!fanList.contains(XueniuFanType.QINGYISE)) {
			return false;
		}
		return true;
	}

	public static boolean handlerDuan19(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}
		for (Integer value : chi) {
			int remainValue = value % 10;
			if (remainValue == 1 || remainValue == 7) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (GameUtil.isYaojiuCard(value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean handlerJiangdui(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (!fanList.contains(XueniuFanType.DUIDUI)) {
			return false;
		}
		for (Integer value : gang) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (!GameUtil.isJiang(GameUtil.getRealValue(value))) {
				return false;
			}
		}
		for (Integer value : chi) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}
		for (Integer value : remain) {
			if (!GameUtil.isJiang(value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean handlerMenqianqing(List<XueniuFanType> fanList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		for (Integer value : gang) {
			if (value < GameConst.AN_MASK) {
				return false;
			}
		}
		for (Integer value : ke) {
			if (value < GameConst.AN_MASK) {
				return false;
			}
		}
		return true;
	}
}
