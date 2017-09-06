package config.bean;

import util.ASObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/4/25.
 */
public class RoomConfig {

	private int room_id;
	private int max_round;
	private int compare_round;
	private int max_pots;
	private int look_round;
	private int full_round;
	private List<Integer> raise = new ArrayList<>();

	public RoomConfig(ASObject obj) {
		this.room_id = obj.getInt("room_id");
		this.max_round = obj.getInt("max_round");
		this.compare_round = obj.getInt("compare_round");
		this.max_pots = obj.getInt("max_pots");
		this.look_round = obj.getInt("look_round");
		this.full_round = obj.getInt("full_round");
		String string = obj.getString("raise");
		if(string != null && !string.equals("")){
			String[] array = obj.getString("raise").split(",");
			List<Integer> list = new ArrayList<>();
			for (String s : array){
				list.add(Integer.parseInt(s));
			}
			this.raise = list;
		}
	}

	public int getRoom_id() {
		return room_id;
	}

	public void setRoom_id(int room_id) {
		this.room_id = room_id;
	}

	public int getMax_round() {
		return max_round;
	}

	public void setMax_round(int max_round) {
		this.max_round = max_round;
	}

	public int getCompare_round() {
		return compare_round;
	}

	public void setCompare_round(int compare_round) {
		this.compare_round = compare_round;
	}

	public int getMax_pots() {
		return max_pots;
	}

	public void setMax_pots(int max_pots) {
		this.max_pots = max_pots;
	}

	public int getLook_round() {
		return look_round;
	}

	public void setLook_round(int look_round) {
		this.look_round = look_round;
	}

	public int getFull_round() {
		return full_round;
	}

	public void setFull_round(int full_round) {
		this.full_round = full_round;
	}

	public List<Integer> getRaise() {
		return raise;
	}

	public void setRaise(List<Integer> raise) {
		this.raise = raise;
	}
}
