package logic.record.ddz;

import logic.majiong.PlayerInfo;
import logic.poker.PokerCard;
import logic.poker.PokerUtil;
import logic.poker.ddz.DdzPos;
import protobuf.Ddz;
import protobuf.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/20.
 */
public class DdzRecord {
	private List<PlayerInfo> playerList;
	private LordInfo lordCards;
	private Map<Integer, List<Integer>> handCards;
	private CalInfo result;
	private List<OneStep> playList;
	private int room_id;
	private int game_id;
	private int total_rounds;
	private int current_rounds;
	private int base_score;

	public DdzRecord(List<PlayerInfo> playerList, int room_id, int game_id, int current_rounds, int base_score) {
		this.playerList = playerList;
		this.room_id = room_id;
		this.base_score = base_score;
		this.game_id = game_id;
		this.total_rounds = 99999;
		this.current_rounds = current_rounds;
	}

	public void addLordCardInfo(int pos, List<PokerCard> cards, int laizi) {
		this.lordCards = new LordInfo(laizi, pos, PokerUtil.convert2IntList(cards));
	}

	public void addHandCards(int pos, List<Integer> cards) {
		if (handCards == null) {
			handCards = new HashMap<>();
		}
		handCards.put(pos, cards);
	}


	public void addStep(int pos, List<PokerCard> cardList) {
		if (playList == null) {
			playList = new ArrayList<>();
		}
		playList.add(new OneStep(pos, cardList == null ? new ArrayList<>() : PokerUtil.convert2IntList(cardList)));
	}

	public void addCalInfo(Ddz.PBDdzCalculate calculate) {
		result = new CalInfo(calculate.getRunPos());
		calculate.getPlayerCalList().forEach(e -> result.addItem(e.getName(), e.getTimes(), e.getGainLose()));
	}


	public List<PlayerInfo> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<PlayerInfo> playerList) {
		this.playerList = playerList;
	}

	public LordInfo getLordCards() {
		return lordCards;
	}

	public void setLordCards(LordInfo lordCards) {
		this.lordCards = lordCards;
	}

	public Map<Integer, List<Integer>> getHandCards() {
		return handCards;
	}

	public void setHandCards(Map<Integer, List<Integer>> handCards) {
		this.handCards = handCards;
	}

	public CalInfo getResult() {
		return result;
	}

	public void setResult(CalInfo result) {
		this.result = result;
	}

	public List<OneStep> getPlayList() {
		return playList;
	}

	public void setPlayList(List<OneStep> playList) {
		this.playList = playList;
	}

	public int getRoom_id() {
		return room_id;
	}

	public void setRoom_id(int room_id) {
		this.room_id = room_id;
	}

	public int getGame_id() {
		return game_id;
	}

	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}

	public int getTotal_rounds() {
		return total_rounds;
	}

	public void setTotal_rounds(int total_rounds) {
		this.total_rounds = total_rounds;
	}

	public int getCurrent_rounds() {
		return current_rounds;
	}

	public void setCurrent_rounds(int current_rounds) {
		this.current_rounds = current_rounds;
	}

	public int getBase_score() {
		return base_score;
	}

	public void setBase_score(int base_score) {
		this.base_score = base_score;
	}

	class CalInfo {
		private int winPos;
		List<CalItem> calResult;

		public CalInfo(int winPos) {
			this.winPos = winPos;
		}

		public void addItem(String name, int times, int ganLose) {
			if (calResult == null) {
				calResult = new ArrayList<>();
			}
			calResult.add(new CalItem(name, times, ganLose));
		}

	}

	class CalItem {
		private String name;
		private int times;
		private int gain_lose;

		public CalItem(String name, int times, int gain_lose) {
			this.name = name;
			this.times = times;
			this.gain_lose = gain_lose;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getTimes() {
			return times;
		}

		public void setTimes(int times) {
			this.times = times;
		}

		public int getGain_lose() {
			return gain_lose;
		}

		public void setGain_lose(int gain_lose) {
			this.gain_lose = gain_lose;
		}
	}


	class OneStep {
		private int pos;
		private List<Integer> cards;

		public OneStep(int pos, List<Integer> cards) {
			this.pos = pos;
			this.cards = cards;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public List<Integer> getCards() {
			return cards;
		}

		public void setCards(List<Integer> cards) {
			this.cards = cards;
		}
	}

	class LordInfo {
		private int laizi;
		private int pos;
		private List<Integer> cards;

		public LordInfo(int laizi, int pos, List<Integer> cards) {
			this.laizi = laizi;
			this.pos = pos;
			this.cards = cards;
		}

		public int getLaizi() {
			return laizi;
		}

		public void setLaizi(int laizi) {
			this.laizi = laizi;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public List<Integer> getCards() {
			return cards;
		}

		public void setCards(List<Integer> cards) {
			this.cards = cards;
		}
	}
}
