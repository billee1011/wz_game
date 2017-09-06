package logic.poker.ddz;

import logic.AbstractDeskInfo;
import logic.poker.PokerCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Administrator on 2017/3/14.
 */
public class DdzDeskInfo implements AbstractDeskInfo {
	private Stack<DdzGroup> deskStack;

	private List<PokerCard> handCards;

	private List<PokerCard> lastRoundCards;

	private DdzPos pos;

	private boolean ready;

	private boolean lord;

	private int robTimes;

	private boolean bonus;

	/** 不叫地主 */
	private boolean giveUpCall;

	private boolean leave;

	private boolean tuoguan;

	private int timeoutTimes;

	private ScheduledFuture<?> autoDiscardFuture;

	public DdzDeskInfo() {
		this.handCards = new ArrayList<>();
		this.deskStack = new Stack<>();
		this.lastRoundCards = new ArrayList<>();
		this.lord = false;
		this.giveUpCall = false;
		this.leave = false;
		this.tuoguan = false;
	}

	public void removeCardList(List<PokerCard> cardList) {
		handCards.removeAll(cardList);
	}

	public void addCard2DiscardStack(DdzGroup group) {
		deskStack.push(group);
		lastRoundCards = group.getCardList();
	}

	public void addHandCard(PokerCard card) {
		this.handCards.add(card);
	}

	public boolean isReady() {
		return ready;
	}

	public boolean containCards(List<PokerCard> cards) {
		if (cards == null) {
			return false;
		}
		for (PokerCard card : cards) {
			if (!handCards.contains(card)) {
				return false;
			}
		}
		return true;
	}


	public boolean isAutoDiscard() {
		return tuoguan;
	}

	public boolean isLeave() {
		return leave;
	}

	public void setLeave(boolean leave) {
		this.leave = leave;
	}

	public boolean isGiveUpCall() {
		return giveUpCall;
	}

	public void setGiveUpCall(boolean giveUpCall) {
		this.giveUpCall = giveUpCall;
	}

	public boolean isDiscardAll() {
		return handCards.size() == 0;
	}

	public boolean isRobLord() {
		return robTimes > 0;
	}

	public int getRobTimes() {
		return this.robTimes;
	}

	public void addRobLordTimes() {
		this.robTimes++;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public DdzPos getPos() {
		return pos;
	}

	@Override
	public int getPositionValue() {
		return getPos().getValue();
	}

	public void setPos(DdzPos pos) {
		this.pos = pos;
	}

	public Stack<DdzGroup> getDeskStack() {
		return deskStack;
	}

	public List<PokerCard> getLastRoundCards() {
		return lastRoundCards;
	}

	public void setLastRoundCards(List<PokerCard> lastRoundCards) {
		this.lastRoundCards = lastRoundCards;
	}

	public List<PokerCard> getLastDiscard() {
		if (deskStack.size() == 0) {
			return null;
		}
		return deskStack.peek().getCardList();
	}

	public int getTimeoutTimes() {
		return timeoutTimes;
	}

	public void addTimeoutTimes() {
		this.timeoutTimes++;
	}

	public void setTimeoutTimes(int timeoutTimes) {
		this.timeoutTimes = timeoutTimes;
	}

	public boolean isTuoguan() {
		return tuoguan;
	}

	public void setTuoguan(boolean tuoguan) {
		this.tuoguan = tuoguan;
	}

	public void setDeskStack(Stack<DdzGroup> deskStack) {
		this.deskStack = deskStack;
	}

	public List<PokerCard> getHandCards() {
		return handCards;
	}

	public List<Integer> getHandCardKeyList() {
		List<Integer> list = new ArrayList<>();
		handCards.forEach(e -> list.add(e.getKey()));
		return list;
	}

	public boolean isBonus() {
		return bonus;
	}

	public void setBonus(boolean bonus) {
		this.bonus = bonus;
	}

	public void setHandCards(List<PokerCard> handCards) {
		this.handCards = handCards;
	}

	public boolean isLord() {
		return lord;
	}

	public void setLord(boolean lord) {
		this.lord = lord;
	}

	public void stopAutoDiscard() {
		if (autoDiscardFuture != null) {
			autoDiscardFuture.cancel(true);
			autoDiscardFuture = null;
		}
	}

	public void setAutoDiscardFuture(ScheduledFuture<?> future) {
		this.autoDiscardFuture = future;
	}

	@Override
	public void clear() {
		deskStack.clear();
		handCards.clear();
		ready = false;
		lord = false;
		robTimes = 0;
		bonus = false;
		giveUpCall = false;
		tuoguan = false;
		if (lastRoundCards == null) {
			lastRoundCards = new ArrayList<>();
		}
		stopAutoDiscard();
		lastRoundCards.clear();
		timeoutTimes = 0;
	}
}
