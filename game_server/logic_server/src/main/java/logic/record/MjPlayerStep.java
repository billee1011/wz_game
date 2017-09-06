package logic.record;

import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MjPlayerStep extends MjStep {
	private List<Integer> cards;
	private int position;

	public MjPlayerStep(int type, List<Integer> cards, int position) {
		this.type = type;
		this.cards = cards;
		this.position = position;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
