package logic.debug.bean;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MjArray {
	
	private int zhuangPos;

	private List<Integer> cardList = new ArrayList<>();

	public MjArray(String value) {
		JSONObject json = JSONObject.fromObject(value);
		List<JSONArray> paiList = new ArrayList<>();
		int playerNum = 2;
		paiList.add(json.getJSONArray("player1"));
		paiList.add(json.getJSONArray("player2"));
		if (value.contains("player3")) {
			paiList.add(json.getJSONArray("player3"));
			playerNum++;
		}
		if (value.contains("player4")) {
			paiList.add(json.getJSONArray("player4"));
			playerNum++;
		}
		for (int i = 0; i < paiList.size(); i++) {
			if (paiList.get(i).size() == 14) { // 找出庄家
				zhuangPos = i + 1;
				break;
			}
		}
		int index = zhuangPos - 1;
		for (int i = 0; i < playerNum; i++) {
			for (int j = 0; j < paiList.get(index).size(); j++) {
				cardList.add((paiList.get(index).getInt(j)));
			}
			if (paiList.get(index++) == null) {
				index = 0;
			}
		}
		if (value.contains("rest")) { // 剩余后续牌
			JSONArray rest = json.getJSONArray("rest");
			for (int i = 0; i < rest.size(); i++) {
				cardList.add(Integer.valueOf(rest.getInt(i)));
			}
		}
	}

	public int getZhuangPos() {
		return zhuangPos;
	}

	public void setZhuangPos(int zhuangPos) {
		this.zhuangPos = zhuangPos;
	}

	public List<Integer> getCardList() {
		return cardList;
	}

	public void setCardList(List<Integer> cardList) {
		this.cardList = cardList;
	}
	
}
