package logic.poker.zjh;

import java.util.List;

/**
 * Created by hhhh on 2017/3/26.
 */
public class ZjhCompareStrategy {

	private ZjhGroupType groupType;
	private List<Integer> list;
	private List<Integer> type;

	public ZjhCompareStrategy(ZjhGroupType groupType, List<Integer> list, List<Integer> type) {
		this.groupType = groupType;
		this.list = list;
		this.type = type;
	}

	public ZjhGroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(ZjhGroupType groupType) {
		this.groupType = groupType;
	}

	public List<Integer> getList() {
		return list;
	}

	public void setList(List<Integer> list) {
		this.list = list;
	}

	public List<Integer> getType() {
		return type;
	}

	public void setType(List<Integer> type) {
		this.type = type;
	}
	
}
