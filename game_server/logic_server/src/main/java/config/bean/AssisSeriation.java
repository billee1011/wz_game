package config.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.majiong.define.MJType;
import net.sf.json.JSONObject;
import util.ASObject;

public class AssisSeriation {
	
	private int gameType;
	
	private int bankerPos;
	
	private Map<Integer, List<Integer>> seriationMap = new HashMap<Integer, List<Integer>>();
	
	private List<Integer> cardStackList = new ArrayList<>();
	
	

	public AssisSeriation(ASObject obj) {
		this.gameType = obj.getInt("game_type");
		String seriation = obj.getString("seriation");
		JSONObject seriationJson = JSONObject.fromObject(seriation);
		bankerPos = seriationJson.getInt("banker_pos");
		String[] cardStack = seriationJson.getString("card_stack").split(",");
		
		String[] cards_1 = seriationJson.getString("cards_1").split(",");
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < cards_1.length; i++) {
			list.add(Integer.valueOf(cards_1[i]));
		}
		seriationMap.put(1, list);
		
		String[] cards_2 = seriationJson.getString("cards_2").split(",");
		list = new ArrayList<>();
		for (int i = 0; i < cards_2.length; i++) {
			list.add(Integer.valueOf(cards_2[i]));
		}
		seriationMap.put(2, list);
		
		if (this.gameType == MJType.XUELIU.getValue()) { 
			String[] cards_3 = seriationJson.getString("cards_3").split(",");
			list = new ArrayList<>();
			for (int i = 0; i < cards_3.length; i++) {
				list.add(Integer.valueOf(cards_3[i]));
			}
			seriationMap.put(3, list);

			String[] cards_4 = seriationJson.getString("cards_4").split(",");
			list = new ArrayList<>();
			for (int i = 0; i < cards_4.length; i++) {
				list.add(Integer.valueOf(cards_4[i]));
			}
			seriationMap.put(4, list);
		}
		
		list = new ArrayList<>();
		for (int i = 0; i < cardStack.length; i++) {
			list.add(Integer.valueOf(cardStack[i]));
		}
		cardStackList = list;
	}
	
	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getBankerPos() {
		return bankerPos;
	}

	public void setBankerPos(int bankerPos) {
		this.bankerPos = bankerPos;
	}

	public Map<Integer, List<Integer>> getSeriationMap() {
		return seriationMap;
	}

	public void setSeriationMap(Map<Integer, List<Integer>> seriationMap) {
		this.seriationMap = seriationMap;
	}

	public List<Integer> getCardStackList() {
		return cardStackList;
	}

	public void setCardStackList(List<Integer> cardStackList) {
		this.cardStackList = cardStackList;
	}
	
}
