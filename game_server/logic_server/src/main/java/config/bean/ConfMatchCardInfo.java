package config.bean;

import java.util.ArrayList;
import java.util.List;

import logic.define.GameType;
import logic.poker.PokerCard;
import net.sf.json.JSONArray;
import util.ASObject;

public class ConfMatchCardInfo {
	private int id;
	private String desc;
	private int randGroupNum;
	private List<List<PokerCard>> cardList = new ArrayList<>();

	public ConfMatchCardInfo(int id, GameType game, String desc,int randGroupNum) {
		this.id = id;
		this.desc = desc;
		this.randGroupNum = randGroupNum;
	}
	
	public void loadDdzCards(ASObject obj){
		JSONArray json1 = JSONArray.fromObject(obj.getString("card_group"));
		cardList.clear();
		
		for (int i = 0; i < json1.size(); i++) {
			JSONArray item = json1.getJSONArray(i);
			List<PokerCard> cards = new ArrayList<PokerCard>();
			for (int j = 0; j < item.size(); j++) {
				cards.add(PokerCard.getByValue(item.getInt(j)));
			}
			cardList.add(cards);
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<PokerCard> getCards() {
		List<PokerCard> handCards = new ArrayList<>();
		for (int i = 0; i < cardList.size(); i++) {
			List<PokerCard> list = cardList.get(i);
			for (int j = 0; j < list.size(); j++) {
				handCards.add(list.get(j));
			}
		}
		return handCards;
	}

	public List<List<PokerCard>> getCardList() {
		return cardList;
	}

	public int getRandGroupNum() {
		return randGroupNum;
	}

}
