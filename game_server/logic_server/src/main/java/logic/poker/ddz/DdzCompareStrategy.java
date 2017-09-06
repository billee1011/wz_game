package logic.poker.ddz;

import logic.poker.PokerCard;
import logic.poker.PokerUtil;

import java.util.*;

/**
 * Created by Administrator on 2017/3/14.
 */
public class DdzCompareStrategy {

	public static boolean single(List<Integer> list1, List<Integer> list2) {
		return PokerUtil.getCompareValue(list1.get(0)) > PokerUtil.getCompareValue(list2.get(0));
	}


	//un reachable
	public static boolean jokerBonus(List<Integer> list1, List<Integer> list2) {
		return true;
	}


}
