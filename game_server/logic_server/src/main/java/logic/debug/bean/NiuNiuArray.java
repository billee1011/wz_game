package logic.debug.bean;

import java.util.ArrayList;
import java.util.List;

import logic.poker.PokerCard;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NiuNiuArray {
	
	private List<PokerCard> cardList = new ArrayList<>();

	public NiuNiuArray(String value) {
		JSONObject json = JSONObject.fromObject(value);
		List<JSONArray> paiList = new ArrayList<>();
		int playerNum = 0;
		for (int i = 0; i < 10; i++) {
			if (value.contains("player" + (i+1))) {
				paiList.add(json.getJSONArray("player" + (i+1)));
				playerNum++;
			}
		}
		
		for (int i = 0; i < playerNum; i++) {
			for (int j = 0; j < paiList.get(i).size(); j++) {
				cardList.add(PokerCard.getByValue(paiList.get(i).getInt(j)));
			}
		}
	}
	
	public List<PokerCard> getCardList() {
		return cardList;
	}
}
