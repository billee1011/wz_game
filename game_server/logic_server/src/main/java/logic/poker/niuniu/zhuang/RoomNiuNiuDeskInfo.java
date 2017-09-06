package logic.poker.niuniu.zhuang;

import logic.AbstractDeskInfo;
import logic.majiong.PlayerInfo;
import logic.poker.PokerCard;
import logic.poker.PokerUtil;
import logic.poker.niuniu.NiuResult;
import logic.poker.niuniu.XianPosition;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2017/4/30.
 */
public class RoomNiuNiuDeskInfo implements AbstractDeskInfo {
    //位置
    private XianPosition postion = null;
    //手牌
    private List<PokerCard> handCards = new ArrayList<>(5);

    // 加注
    private int betPos;     // 加注的下标

    private int grabZhuangPos; // 玩家抢庄的下标

    private PlayerInfo playerInfo;

    private Pair<NiuResult, List<PokerCard>> result;

    private Pair<Pair<PokerCard, PokerCard>, NiuResult> tempResult;

    private int enterTime;  // 进入房间的时间

    private boolean isWatch;  // 是否观察者

    //离开状态
    private boolean leave;
    
    /** 是否准备 */
    private boolean ready;
    
    private boolean isRevcPlayerCal;
    
    /** 玩家自己点击抢庄倍数 */
    private int playerGrabMulti;
    /** 玩家自己点击下注倍数 */
    private int playerBetMulti;
    /** 玩家未操作局数 */
    private int noGrabAndBetRound;
    
    /** 最后一次是否观察者 */
    private boolean lastIsWatch;
    
    @Override
    public int getPositionValue() {
        return postion.getValue();
    }

    @Override
    public void clear() {
        handCards.clear();
        result = null;
        tempResult = null;
        betPos = 0;
        grabZhuangPos = 0;
        isWatch = false;
        ready = false;
        this.isRevcPlayerCal = false;
        playerGrabMulti = 0;
        playerBetMulti = 0;
    }

    public Pair<NiuResult, List<PokerCard>> getResult() {
        return result;
    }

    public void setResult(Pair<NiuResult, List<PokerCard>> result) {
        this.result = result;
    }

    public void addHandCards(PokerCard card) {
        handCards.add(card);
    }
    
    public List<Integer> get5HandCardKeyList() {
        List<Integer> list = new ArrayList<>();
        handCards.forEach(e -> list.add(e.getKey()));
        return list;
    }

    public List<Integer> get4HandCardKeyList() {
        List<Integer> list = new ArrayList<>();
        handCards.subList(0, handCards.size() - 1).forEach(e -> list.add(e.getKey()));
        return list;
    }

    public void calNiuResult() {
        tempResult = PokerUtil.calNiuResult(handCards);
        result = new Pair<>(tempResult.getRight(), handCards);
    }

    public int getRemainHandCardKey() {
        return handCards.get(handCards.size() - 1).getKey();
    }


    public XianPosition getPostion() {
        return postion;
    }

    public void setPostion(XianPosition postion) {
        this.postion = postion;
    }

    public List<PokerCard> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<PokerCard> handCards) {
        this.handCards = handCards;
    }

    public int getBetPos() {
        return betPos;
    }

    public int getGrabZhuangPos() {
        return grabZhuangPos;
    }

    public void setGrabZhuangPos(int grabZhuangPos) {
        this.grabZhuangPos = grabZhuangPos;
    }

    public void setBetPos(int betPos) {
        this.betPos = betPos;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public boolean isLeave() {
        return leave;
    }

    public void setLeave(boolean leave) {
        this.leave = leave;
    }

    public int getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(int enterTime) {
        this.enterTime = enterTime;
    }

    public boolean isWatch() {
        return isWatch;
    }

    public void setWatch(boolean watch) {
        isWatch = watch;
    }

    public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isRevcPlayerCal() {
		return isRevcPlayerCal;
	}

	public void setRevcPlayerCal(boolean isRevcPlayerCal) {
		this.isRevcPlayerCal = isRevcPlayerCal;
	}

	public int getPlayerGrabMulti() {
		return playerGrabMulti;
	}

	public void setPlayerGrabMulti(int playerGrabMulti) {
		this.playerGrabMulti = playerGrabMulti;
		this.noGrabAndBetRound = 0;
	}

	public int getPlayerBetMulti() {
		return playerBetMulti;
	}

	public void setPlayerBetMulti(int playerBetMulti) {
		this.playerBetMulti = playerBetMulti;
		this.noGrabAndBetRound = 0;
	}
	
	public int getNoGrabAndBetRound() {
		return noGrabAndBetRound;
	}

	public void setNoGrabAndBetRound(int noGrabAndBetRound) {
		this.noGrabAndBetRound = noGrabAndBetRound;
	}

	public boolean isLastIsWatch() {
		return lastIsWatch;
	}

	public void setLastIsWatch(boolean lastIsWatch) {
		this.lastIsWatch = lastIsWatch;
	}

	public Pair<Pair<PokerCard, PokerCard>, NiuResult> getTempResult() {
        return tempResult;
    }

    public void setTempResult(Pair<Pair<PokerCard, PokerCard>, NiuResult> tempResult) {
        this.tempResult = tempResult;
    }
    
    /** 取出有牛时，最后两张牌 */
    public List<Integer> getTowCards(){
    	List<Integer> towCards = new ArrayList<>();
    	if(tempResult.getLeft() != null){
    		towCards.add(tempResult.getLeft().getLeft().getKey());
    		towCards.add(tempResult.getLeft().getRight().getKey());
    	}
    	return towCards;
    }
}
