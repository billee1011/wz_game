package logic.record.zjh;

import logic.majiong.PlayerInfo;
import logic.poker.zjh.ZjhDeskInfo;
import logic.record.MJPlayerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hhhh on 2017/4/21.
 */
public class ZjhRecord {
	private int base_score;     //底分
	private int current_rounds; //打了的局数
	private int total_rounds;   //总局数
	private int zhuang_pos; // 庄家位置
	private int game_id;
	private long room_id;
	private int betPool;    //彩池上限
	private int maxRound;//      最大回合数
	private int limitRound; //比牌限制回合数
	private List<MJPlayerInfo> playerList;
	private Map<Integer, List<Integer>> playerCards;
	private List<Step> play;

	public ZjhRecord(long roomId, int base_score, int betPool, int maxRound, int limitRound) {
		this.base_score = base_score;
		this.current_rounds = 1;
		this.total_rounds = 99999;
		this.betPool = betPool;
		this.maxRound = maxRound;
		this.limitRound = limitRound;
		this.room_id = roomId;
		this.game_id = 6;
		playerList = new ArrayList<>();
		playerCards = new HashMap<>();
		play = new ArrayList<>();
//		infoMap.forEach((e, f) -> {
//			playerList.add(new MJPlayerInfo(f.getPositionValue(), e));
//		});
	}
	public void initPlayer(Map<PlayerInfo, ZjhDeskInfo> infoMap) {
		playerList.clear();
		infoMap.forEach((e, f) -> {
			if(!f.isWatch()){
				playerList.add(new MJPlayerInfo(f.getPositionValue(), e));
			}
		});
	}

	public void clearStatus(long room_id) {
		this.room_id = room_id;
		current_rounds++;
		playerCards.clear();
		play.clear();
	}

	public void dealCard(Map<PlayerInfo, ZjhDeskInfo> infoMap) {
		infoMap.forEach((e, f) -> {
			if(!f.isWatch()){
				playerCards.put(f.getPositionValue(), f.getHandCardKeyList());
			}
		});
	}

	public void addPots(int pos, int pots,int round,int sigle_pots) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new Step(1, pos, pots, null,0,round,sigle_pots));
	}

	public void lookCard(int pos,int round, int pots,int sigle_pots) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new Step(2, pos, pots, null,0,round,sigle_pots));
	}

	public void compareCard(int pos, int pots, Object[] player_pos,int isWin,int round,int sigle_pots) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new Step(3, pos, pots, player_pos,isWin,round,sigle_pots));
	}

	public void callResult(int pos, int pots,int round,int sigle_pots) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new Step(4, pos, pots, null,0,round,sigle_pots));
	}

	public void giveUp(int pos,int round, int pots,int sigle_pots) {
		if (play == null) {
			play = new ArrayList<>();
		}
		play.add(new Step(5, pos,pots, null,0,round,sigle_pots));
	}


	public int getBase_score() {
		return base_score;
	}

	public void setBase_score(int base_score) {
		this.base_score = base_score;
	}

	public int getCurrent_rounds() {
		return current_rounds;
	}

	public void setCurrent_rounds(int current_rounds) {
		this.current_rounds = current_rounds;
	}

	public int getTotal_rounds() {
		return total_rounds;
	}

	public void setTotal_rounds(int total_rounds) {
		this.total_rounds = total_rounds;
	}

	public long getGame_id() {
		return game_id;
	}

	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}

	public long getRoom_id() {
		return room_id;
	}

	public void setRoom_id(long room_id) {
		this.room_id = room_id;
	}

	public int getBetPool() {
		return betPool;
	}

	public void setBetPool(int betPool) {
		this.betPool = betPool;
	}

	public int getMaxRound() {
		return maxRound;
	}

	public void setMaxRound(int maxRound) {
		this.maxRound = maxRound;
	}

	public int getLimitRound() {
		return limitRound;
	}

	public void setLimitRound(int limitRound) {
		this.limitRound = limitRound;
	}

	public List<MJPlayerInfo> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<MJPlayerInfo> playerList) {
		this.playerList = playerList;
	}

	public Map<Integer, List<Integer>> getPlayerCards() {
		return playerCards;
	}

	public void setPlayerCards(Map<Integer, List<Integer>> playerCards) {
		this.playerCards = playerCards;
	}

	public List<Step> getPlay() {
		return play;
	}

	public void setPlay(List<Step> play) {
		this.play = play;
	}

	public int getZhuang_pos() {
		return zhuang_pos;
	}

	public void setZhuang_pos(int zhuang_pos) {
		this.zhuang_pos = zhuang_pos;
	}

	class Step {
		private int type;
		private int pos;
		private int pots;   //单注
		private Object[] player_pos;
		private int isWin;  //0 失败
		private int round;
		private int sigle_pots; //

		Step(int type, int pos, int pots, Object[] player_pos,int isWin,int round,int sigle_pots) {
			this.type = type;
			this.pos = pos;
			this.pots = pots;
			this.player_pos = player_pos;
			this.isWin = isWin;
			this.round = round;
			this.sigle_pots = sigle_pots;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public int getPots() {
			return pots;
		}

		public void setPots(int pots) {
			this.pots = pots;
		}

		public Object[] getPlayer_pos() {
			return player_pos;
		}

		public void setPlayer_pos(Object[] player_pos) {
			this.player_pos = player_pos;
		}

		public int getIsWin() {
			return isWin;
		}

		public void setIsWin(int isWin) {
			this.isWin = isWin;
		}

		public int getRound() {
			return round;
		}

		public void setRound(int round) {
			this.round = round;
		}

		public int getSigle_pots() {
			return sigle_pots;
		}

		public void setSigle_pots(int sigle_pots) {
			this.sigle_pots = sigle_pots;
		}
	}
}
