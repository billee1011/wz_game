package logic.poker.zjh;

import chr.Player;
import logic.AbstractDeskInfo;
import logic.majiong.PlayerInfo;
import logic.poker.PokerCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhhh on 2017/3/24.
 */
public class ZjhDeskInfo implements AbstractDeskInfo {

	private static final Logger logger = LoggerFactory.getLogger(ZjhDeskInfo.class);
	//状态 0 可操作玩家 1 旁观者
	private boolean isWatch;
	//位置
	private ZjhPostion postion = null;
	//手牌
	private List<PokerCard> handCards;
	//手牌状态
	private boolean open;
	//准备状态
	private boolean ready;
	//失败状态
	private boolean giveUp;
	//下注金额
	private int post;
	//离开状态
	private boolean leave;
	//是否需要推送
	private boolean isPush;
	//是否满压
	private boolean isFull;
	//牌型
	private ZjhCompareStrategy strategy;
	private int passiveTime;
	private int callTime;
	//是否被淘汰
	private boolean isLose;

	public PlayerInfo playInfo;

	private int round;

	/**
	 * 总计输赢
	 */
	private int totalpots;

	public void clearStatus(){
		this.open = false;
		this.ready = false;
		this.giveUp = false;
		this.leave = false;
		this.isPush = false;
		this.post = 0;
		this.isFull = false;
//		this.strategy = null;
		this.isWatch = false;
		this.callTime = 0;
		this.isLose = false;
		this.round = 0;
	}

	public int getCoin(){
		return playInfo.getCoin()-post;
	}
	/**
	 * 下注
	 * @param postValue
	 */
	public void addPost(int postValue){
		this.post+=postValue;
	}

	public int getPost(){
		return post;
	}

	public ZjhDeskInfo(ZjhPostion postion,PlayerInfo playInfo){
		this.open = false;
		this.ready = false;
		this.giveUp = false;
		this.leave = false;
		this.isPush = false;
		this.isFull = false;
		this.postion = postion;
		this.post = 0;
		this.isWatch = false;
		this.passiveTime = 0;
		this.callTime = 0;
		this.isLose = false;
		this.playInfo = playInfo;
		this.round = 0;
		handCards = new ArrayList<>();
	}

	public void addCallTime(){
		this.callTime++;
	}
	public int getCallTime(){
		return callTime;
	}

	public void addHandCards(PokerCard pokerCard){
		this.handCards.add(pokerCard);
	}


	public List<Integer> getHandCardKeyList() {
		List<Integer> list = new ArrayList<>();
		handCards.forEach(e -> list.add(e.getKey()));
		return list;
	}

	public int getStarategyValue(){
		return this.strategy.getGroupType().getValue();
	}

	public List<PokerCard> getHandCards(){
		return  this.handCards;
	}


	public ZjhPostion getPostion() {
		return postion;
	}

	@Override
	public int getPositionValue() {
		return getPostion().getValue();
	}

	public void setPostion(ZjhPostion postion) {
		this.postion = postion;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isGiveUp() {
		return giveUp;
	}

	public void setGiveUp(boolean giveUp) {
		this.giveUp = giveUp;
	}

	public boolean isLeave() {
		return leave;
	}

	public void setLeave(boolean leave) {
		this.leave = leave;
	}

	public boolean isPush() {
		return isPush;
	}

	public void setPush(boolean push) {
		isPush = push;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean full) {
		isFull = full;
	}

	public ZjhCompareStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(ZjhCompareStrategy strategy) {
		this.strategy = strategy;
	}

	public boolean isWatch() {
		return isWatch;
	}

	public void setWatch(boolean watch) {
		isWatch = watch;
	}

	public int getPassiveTime() {
		return passiveTime;
	}

	public void addPassiveTime(){
		this.passiveTime+=1;
	}
	public void cleanPassiveTime(){
		this.passiveTime = 0;
	}

	public void addTotalPots(int value){
		this.totalpots+=value;
	}

	public int getTotalpots() {
		return totalpots;
	}

	public void setTotalpots(int totalpots) {
		this.totalpots = totalpots;
	}

	public boolean isLose() {
		return isLose;
	}

	public void setLose(boolean lose) {
		isLose = lose;
	}

	@Override
	public void clear() {
		handCards.clear();
		ready = false;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public boolean isGameIng() {
		return !isWatch && !giveUp && !isLose;
	}
}
