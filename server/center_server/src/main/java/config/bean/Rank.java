package config.bean;

import config.IConfParseBean;

/**
 * Created by Administrator on 2016/12/2.
 */
public class Rank implements IConfParseBean {
	private int id;
	private String rank;
	private int gold;
	private int fromRank;
	private int toRank;

	@Override
	public boolean parse() {
		String[] strs = rank.split(",");
		if (strs.length == 1) {
			fromRank = Integer.parseInt(rank);
		}
		if (strs.length == 2) {
			fromRank = Integer.parseInt(strs[0]);
			toRank= Integer.parseInt(strs[1]);
		}
		return true;
	}

	public int getFromRank() {
		return fromRank;
	}

	public int getToRank() {
		return toRank;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}
}
