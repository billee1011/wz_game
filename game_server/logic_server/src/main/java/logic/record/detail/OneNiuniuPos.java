package logic.record.detail;

import logic.poker.PokerCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/28.
 */
public class OneNiuniuPos {
	private int identity;
	private List<Integer> cards;
	private int cardType;
	private int result;

	public OneNiuniuPos(int identity, List<PokerCard> cards, int cardType, int result) {
		this.identity = identity;
		this.cards = new ArrayList<>();
		cards.forEach(e -> this.cards.add(e.getKey()));
		this.cardType = cardType;
		this.result = result;
	}

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
}
