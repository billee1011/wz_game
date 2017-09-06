package config.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import logic.poker.PokerCard;
import net.sf.json.JSONArray;
import util.Pair;
import util.Randomizer;

public class ConfMatchCard {
	private int status;
	private int rate;
	private List<List<Pair<Integer, Integer>>> match = new ArrayList<List<Pair<Integer,Integer>>>();
	private List<Integer> matchGrabNiu = new ArrayList<>();
	private Map<Integer, Integer> zjhPlayerMap = new HashMap<Integer, Integer>();
	private List<Integer> zjhCardList = new ArrayList<Integer>();
	private List<Pair<ConfMatchCardInfo, Integer>> ddzCards = new ArrayList<Pair<ConfMatchCardInfo,Integer>>();
	
	public ConfMatchCard(int status,int rate) {
		this.status = status;
		this.rate = rate;
	}
	
	public void loadConfMatchNiuCard(String match_niu) {
		JSONArray array = JSONArray.fromObject(match_niu);
		//[[[8,50],[9,30],[10,20]],[[9,80],[10,20]],[[10,100]]]
		
		for (int i = 0; i < array.size(); i++) {
			List<Pair<Integer, Integer>> matchItem = new ArrayList<>();
			JSONArray matchItemJson = array.getJSONArray(i);
			for (int j = 0; j < matchItemJson.size(); j++) {
				matchItem.add(new Pair<Integer, Integer>(matchItemJson.getJSONArray(j).getInt(0), matchItemJson.getJSONArray(j).getInt(1)));
			}
			match.add(matchItem);
		}
	}
	
	public void loadConfMatchGrabNiuCard(String match_grab_niu) {
		JSONArray array = JSONArray.fromObject(match_grab_niu);
		for (int i = 0; i < array.size(); i++) {
			matchGrabNiu.add(array.getInt(i));
		}
	}
	
	public void loadZjhPlayers(String zjhPlayers) {
		JSONArray array = JSONArray.fromObject(zjhPlayers);
		//[[3,2],[4,2],[5,2],[6,3]]
		
		for (int i = 0; i < array.size(); i++) {
			JSONArray matchItemJson = array.getJSONArray(i);
			zjhPlayerMap.put(matchItemJson.getInt(0),matchItemJson.getInt(1));
		}
	}
	
	public void loadZjhCard(String zjhCard) {
		JSONArray array = JSONArray.fromObject(zjhCard);
		
		for (int i = 0; i < array.size(); i++) {
			zjhCardList.add(array.getInt(i));
		}
	}
	
	public void loadDdzCard(Map<Integer, ConfMatchCardInfo> ddzCard, String cardRate) {
		JSONArray array = JSONArray.fromObject(cardRate);
		//[[1,100]]
		for (int i = 0; i < array.size(); i++) {
			JSONArray matchItemJson = array.getJSONArray(i);
			ConfMatchCardInfo confMatchCardDdz = ddzCard.get(matchItemJson.getInt(0));
			ddzCards.add(new Pair<ConfMatchCardInfo, Integer>(confMatchCardDdz, matchItemJson.getInt(1)));
		}
	}
	
	public int getMatchNiu(int niuVal){
		for (int j = 0; j < match.size(); j++) {
			List<Pair<Integer, Integer>> matchItem = match.get(j);
			if(niuVal < matchItem.get(0).getLeft()){
				int rand = Randomizer.nextInt(1,10001);
	    		int totalWeight = 0;
	    		for (int i = 0; i < matchItem.size(); i++) {
	    			totalWeight += matchItem.get(i).getRight();
	    			if(rand <= totalWeight){
	    				return matchItem.get(i).getLeft();
	    			}
				}
	    		break;
			}
		}
		return -1;
	}

	public boolean isOpen() {
		if(status != 1){
			return false;
		}
		return Randomizer.nextInt(1,10001) <= rate;
	}
	
	/** -1 0 1 2*/
	public int getGrabNiuType(){
		int rand = Randomizer.nextInt(1,10001);
		int totalWeight = 0;
		for (int i = 0; i < matchGrabNiu.size(); i++) {
			totalWeight += matchGrabNiu.get(i);
			if(rand <= totalWeight){
				return i;
			}
		}
		return -1;
	}
	
	public Integer getZjhNeedMatchNum(int curNum){
		return zjhPlayerMap.get(curNum);
	}
	
	public Integer getZjhCardType(){
		//[40,30,20,10]
		int rand = Randomizer.nextInt(1,10001);
		int totalWeight = 0;
		for (int i = 0; i < zjhCardList.size(); i++) {
			totalWeight += zjhCardList.get(i);
			if(rand <= totalWeight){
				return i;
			}
		}
		return -1;
	}
	
	public ConfMatchCardInfo getDdzCards(){
		int rand = Randomizer.nextInt(1,10001);
		int totalWeight = 0;
		for (int i = 0; i < ddzCards.size(); i++) {
			totalWeight += ddzCards.get(i).getRight();
			if(rand <= totalWeight){
				return ddzCards.get(i).getLeft();
			}
		}
		return null;
	}

}
