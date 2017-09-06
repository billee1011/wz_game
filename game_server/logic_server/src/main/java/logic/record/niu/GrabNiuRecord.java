package logic.record.niu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.poker.niuniu.NiuResult;
import logic.poker.niuniu.zhuang.RoomNiuNiuDeskInfo;
import logic.record.MJPlayerInfo;

public class GrabNiuRecord {
	private int gameId;
	private long roomId;// 如果是私房则是桌子id否则是房间conf id
	/** 1经典 2看牌模式 */
	private int model;
	/** 1抢庄 2轮庄 3霸庄 */
	private int rule;
	private int baseScore; // 底分
	/** 23rule时系统默认庄倍数 */
	private int multi;
	private List<MJPlayerInfo> playerList;
	/** 庄的位置 */
	private int zhuangPos;
	/** 玩家手中的牌 */
	/** 牛多少 无牛0 牛1-9 牛牛10 12五牛 13炸弹牛 14五小牛 */
	private Map<Integer, PosCard> posCards;
	/** 当前多少局 */
	private int curRound;

	class PosCard {
		/** 牌类型 牛几 */
		private int typed;
		/** 牌 */
		private List<Integer> handCards;
		/** 形成牛的最后两张牌 */
		private List<Integer> cardsAdd;
		/** 赢的金币 */
		private int coin;
		/** 抢庄倍数-没抢时会默认值 */
		private int grabMutli;
		/** 下注倍数-没抢时会默认值 */
		private int bet;
		/** 玩家手动点击抢庄的倍数 没抢则是0*/
		private int playerGabMutli;
		/** 玩家手动点击下注的倍数 没下注则是0 */
		private int playerBetMutli;

		public PosCard() {

		}

		public int getTyped() {
			return typed;
		}

		public void setTyped(int typed) {
			this.typed = typed;
		}

		public List<Integer> getHandCards() {
			return handCards;
		}

		public void setHandCards(List<Integer> handCards) {
			this.handCards = handCards;
		}

		public List<Integer> getCardsAdd() {
			return cardsAdd;
		}

		public void setCardsAdd(List<Integer> cardsAdd) {
			this.cardsAdd = cardsAdd;
		}

		public int getCoin() {
			return coin;
		}

		public void setCoin(int coin) {
			this.coin = coin;
		}
		
		public int getGrabMutli() {
			return grabMutli;
		}

		public void setGrabMutli(int grabMutli) {
			this.grabMutli = grabMutli;
		}

		public int getBet() {
			return bet;
		}

		public void setBet(int bet) {
			this.bet = bet;
		}

		public int getPlayerGabMutli() {
			return playerGabMutli;
		}

		public void setPlayerGabMutli(int playerGabMutli) {
			this.playerGabMutli = playerGabMutli;
		}

		public int getPlayerBetMutli() {
			return playerBetMutli;
		}

		public void setPlayerBetMutli(int playerBetMutli) {
			this.playerBetMutli = playerBetMutli;
		}

	}

	public GrabNiuRecord(int gameId, long roomId, int baseScore, int model, int rule,int mutli) {
		this.gameId = gameId;
		this.roomId = roomId;
		this.model = model;
		this.rule = rule;
		this.multi = mutli;
		this.baseScore = baseScore;
		playerList = new ArrayList<>();
	}

	public void initPlayer(List<RoomNiuNiuDeskInfo> list) {
		playerList.clear();
		posCards = null;
		posCards = new HashMap<>();
		list.forEach(e -> {
			playerList.add(new MJPlayerInfo(e.getPositionValue(), e.getPlayerInfo()));
			posCards.put(e.getPositionValue(), new PosCard());
		});
		zhuangPos = 0;
	}

	/** 抢庄倍数 */
	public void addZhuangMulti(int pos, int multi,int playerGabMutli) {
		PosCard posCard = posCards.get(pos);
		if (posCard != null) {
			posCard.setGrabMutli(multi);
			posCard.setPlayerGabMutli(playerGabMutli);
		}
	}

	/** 下注倍数 */
	public void addBetMulti(int pos, int multi,int playerBetMutli) {
		PosCard posCard = posCards.get(pos);
		if (posCard != null) {
			posCard.setBet(multi);
			posCard.setPlayerBetMutli(playerBetMutli);
		}
	}

	public void addNiuResult(int pos, NiuResult niu, List<Integer> cards, List<Integer> cardsAdd) {
		PosCard posCard = posCards.get(pos);
		if (posCard != null) {
			posCard.setTyped(niu.getValue());
			posCard.setHandCards(new ArrayList<>(cards));
			posCard.setCardsAdd(cardsAdd);
		}
	}

	public void addWinResult(int pos, int coin) {
		PosCard posCard = posCards.get(pos);
		if (posCard != null) {
			posCard.setCoin(coin);
		}
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getRule() {
		return rule;
	}

	public void setRule(int rule) {
		this.rule = rule;
	}

	public int getBaseScore() {
		return baseScore;
	}

	public void setBaseScore(int baseScore) {
		this.baseScore = baseScore;
	}

	public int getMulti() {
		return multi;
	}

	public void setMulti(int multi) {
		this.multi = multi;
	}

	public List<MJPlayerInfo> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<MJPlayerInfo> playerList) {
		this.playerList = playerList;
	}

	public int getZhuangPos() {
		return zhuangPos;
	}

	public void setZhuangPos(int zhuangPos) {
		this.zhuangPos = zhuangPos;
	}

	public Map<Integer, PosCard> getPosCards() {
		return posCards;
	}

	public void setPosCards(Map<Integer, PosCard> posCards) {
		this.posCards = posCards;
	}

	public int getCurRound() {
		return curRound;
	}

	public void setCurRound(int curRound) {
		this.curRound = curRound;
	}

}
