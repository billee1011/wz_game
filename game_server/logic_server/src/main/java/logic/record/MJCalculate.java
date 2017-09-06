package logic.record;




import util.Pair;

import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MJCalculate {
	private int type;
	private int position;
	private int card;
	private int times;
	private List<Pair<Integer,Integer>> result;
	private List<Integer> hu_list;

	public MJCalculate(int type, int position, int card, int times, List<Pair<Integer, Integer>> result, List<Integer> hu_list) {
		this.type = type;
		this.position = position;
		this.card = card;
		this.times = times;
		this.result = result;
		this.hu_list = hu_list;
	}

	public List<Integer> getHu_list() {
		return hu_list;
	}

	public void setHu_list(List<Integer> hu_list) {
		this.hu_list = hu_list;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getCard() {
		return card;
	}

	public void setCard(int card) {
		this.card = card;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public List<Pair<Integer, Integer>> getResult() {
		return result;
	}

	public void setResult(List<Pair<Integer, Integer>> result) {
		this.result = result;
	}
}
