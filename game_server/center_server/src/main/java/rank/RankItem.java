package rank;


import util.MapObject;

/**
 * Created by Administrator on 2016/11/28.
 */
public class RankItem {
	private int id;
	private int rank;
	private int score;
	private String account;

	public RankItem(MapObject o) {
		id = o.getInt("player_id");
		account = o.getString("nickname");
		score = o.getInt("score");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}
