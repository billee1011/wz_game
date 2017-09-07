package logic.record;

import logic.majiong.PlayerDeskInfo;
import logic.majiong.PlayerInfo;
import logic.majiong.define.MJPosition;
import proto.Common;
import util.MiscUtil;
import util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MjRecord {
	private List<MJPlayerInfo> playerList = null;
	private int banker_id;
	private List<Integer> dice;
	private List<List<Integer>> hand_cards;
	private List<List<Integer>> color_cards;
	private MjChangeCard change_card;
	private List<Integer> que_type;
	private List<MjStep> play;
	private int room_id;
	private int game_id;
	private int total_rounds;
	private int current_rounds;
	private int base_score;

	public void clear() {
		banker_id = 0;
		dice = null;
		hand_cards = null;
		color_cards = null;
		change_card = null;
		que_type = null;
		play = null;
	}

	public void addSelfRoomRecord(int game_id, int total_rounds, int current_rounds, int base_score, int deskId) {
		this.game_id = game_id;
		this.total_rounds = total_rounds;
		this.current_rounds = current_rounds;
		this.base_score = base_score;
		this.room_id = deskId;
	}

	public void addCalculateStepCouple(int type, int pos, int card, int times, List<Integer> huList, List<Pair<Integer, Integer>> gainLose) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new MJCalculateStep(9, new MJCalculate(type, pos, card, times, gainLose, huList)));
	}

	public void addCalculateStep(int type, int pos, int card, int times, List<Integer> huList, List<Common.PBPair> gainLose) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new MJCalculateStep(9, new MJCalculate(type, pos, card, times, MiscUtil.fromPBPair(gainLose), huList)));
	}

	public void addPlayerStep(int type, int card, int position) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new MjPlayerStep(type, Arrays.asList(card), position));
	}

	public void addPlayerStep(int type, List<Integer> cards, int position) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new MjPlayerStep(type, cards, position));
	}

	public void addQueType(int type) {
		if (que_type == null) {
			que_type = new ArrayList<>();
		}
		que_type.add(type);
	}

	public void addChangeInfo(int direct, List<List<Integer>> add, List<List<Integer>> remove) {
		change_card = new MjChangeCard(direct, remove, add);
	}

	public MjRecord(Map<PlayerInfo, PlayerDeskInfo> infoMap, int roomId) {
		this.room_id = roomId;
		playerList = new ArrayList<>();
		infoMap.forEach((e, f) -> playerList.add(new MJPlayerInfo(f.getPosition().getValue(), e)));
	}

	public void addDiceInfo(Pair<Integer, Integer> diceInfo) {
		dice = new ArrayList<>();
		dice.add(diceInfo.getLeft());
		dice.add(diceInfo.getRight());
	}

	public void addHandAndHuaCard(Map<MJPosition, PlayerDeskInfo> infoMap) {
		hand_cards = new ArrayList<>();
		color_cards = new ArrayList<>();
		for (int i = MJPosition.EAST.getValue(); i <= MJPosition.NORTH.getValue(); i++) {
			MJPosition pos = MJPosition.getByValue(i);
			PlayerDeskInfo info = infoMap.get(pos);
			if (info == null) {
				continue;
			}
			List<Integer> handCards = new ArrayList<>(info.getHandCards());
			hand_cards.add(handCards);
			List<Integer> huaCards = new ArrayList<>(info.getHuaCards());
			color_cards.add(huaCards);
		}
	}

	public int getRoom_id() {
		return room_id;
	}

	public void setRoom_id(int room_id) {
		this.room_id = room_id;
	}

	public MjChangeCard getChange_card() {
		return change_card;
	}

	public void setChange_card(MjChangeCard change_card) {
		this.change_card = change_card;
	}

	public void setPlay(List<MjStep> play) {
		this.play = play;
	}

	public List<MjStep> getPlay() {
		return play;
	}

	public List<MJPlayerInfo> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<MJPlayerInfo> playerList) {
		this.playerList = playerList;
	}

	public int getBanker_id() {
		return banker_id;
	}

	public void setBanker_id(int banker_id) {
		this.banker_id = banker_id;
	}

	public List<Integer> getDice() {
		return dice;
	}

	public void setDice(List<Integer> dice) {
		this.dice = dice;
	}

	public List<List<Integer>> getHand_cards() {
		return hand_cards;
	}

	public void setHand_cards(List<List<Integer>> hand_cards) {
		this.hand_cards = hand_cards;
	}

	public List<List<Integer>> getColor_cards() {
		return color_cards;
	}

	public void setColor_cards(List<List<Integer>> color_cards) {
		this.color_cards = color_cards;
	}

	public List<Integer> getQue_type() {
		return que_type;
	}

	public void setQue_type(List<Integer> que_type) {
		this.que_type = que_type;
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
}
