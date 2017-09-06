package logic.debug;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import logic.majiong.CoupleFanType;
import logic.majiong.GameUtil;
import logic.majiong.cpstragety.EightyEightStragety;
import logic.majiong.cpstragety.StragetyManager;

public class CheckFanType {
	private static CheckFanType instance = new CheckFanType();
	public static CheckFanType getInst() {
		return instance;
	}
	
	public String[] checkCoupleMj(List<Integer> gangList, List<Integer> keList, List<Integer> chiList,
			List<Integer> jiang) {
		Stack<Integer> gang = list2stack(gangList);
		LinkedList<Integer> ke = new LinkedList<>(keList);
		LinkedList<Integer> chi = new LinkedList<>(chiList);
		List<CoupleFanType> typeList = StragetyManager.getInstance().checkValidFanType(gang, ke, chi, jiang);
		if (!((chiList).size() > 0 || gangList.size() > 0 || keList.size() > 0)) {
			List list = new ArrayList<>();
			keList.forEach(e -> {
				list.add(GameUtil.getRealValue(e));
				list.add(GameUtil.getRealValue(e));
				list.add(GameUtil.getRealValue(e));
			});
			chiList.forEach(e -> {
				list.add(e);
				list.add(e.intValue() + 1);
				list.add(e.intValue() + 2);
			});
			jiang.forEach(e -> {
				list.add(e);
				list.add(e);
			});
			if (EightyEightStragety.three(gang, chi, ke, jiang)) {
				typeList.add(CoupleFanType.EIGHTY_EIGHT_3);
			}
		}
		StringBuffer sb = new StringBuffer();
		for (CoupleFanType type : typeList){
			sb.append(type.getDesc() + "    ");
		}
		int fan = GameUtil.getTotalFanshu(typeList);
		return new String[] { String.valueOf(fan), sb.toString() };
	}
	
	private Stack<Integer> list2stack(List<Integer> list){
		Stack<Integer> stack = new Stack<>();
		for (int i = 0; i < list.size(); i++) {
			stack.add(list.get(i));
		}
		return stack;
	}
}
