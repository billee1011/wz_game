package logic.record;

import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MjChangeCard {
	private int direct;
	private List<List<Integer>> remove;
	private List<List<Integer>> add;

	public MjChangeCard(int direct, List<List<Integer>> remove, List<List<Integer>> add) {
		this.direct = direct;
		this.remove = remove;
		this.add = add;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public List<List<Integer>> getRemove() {
		return remove;
	}

	public void setRemove(List<List<Integer>> remove) {
		this.remove = remove;
	}

	public List<List<Integer>> getAdd() {
		return add;
	}

	public void setAdd(List<List<Integer>> add) {
		this.add = add;
	}
}
