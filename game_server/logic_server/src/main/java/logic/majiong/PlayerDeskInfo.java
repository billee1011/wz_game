package logic.majiong;

import logic.AbstractDeskInfo;
import logic.majiong.define.MJPosition;
import proto.CoupleMajiang;
import util.MiscUtil;
import util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerDeskInfo implements AbstractDeskInfo {
	private Queue<Integer> keQueue;
	private Queue<Integer> chiQueue;
	private List<Pair<Integer, Integer>> resetChiData;
	private Stack<Integer> gangQueue;
	private List<Integer> handCards;
	private List<Integer> huaCards;
	private boolean gangIng;                                //杠手, 检查杠上开花
	private boolean hujiaoIng;                              //是否被呼叫转移
	private Stack<Integer> deskPaiStack;
	private int discardTimes;
	private int addTimes;
	private boolean baoTing;
	private boolean tianting;
	private MenFengType type;
	private int baotingDiscardTimes;
	private AtomicBoolean ready = new AtomicBoolean(false);
	private int timeoutTimes;
	private List<Integer> operationList = new ArrayList<>();
	private Map<Integer, List<Pair<Integer, Integer>>> tingMap = new HashMap<>();
	private int tingDropValue;
	private boolean leave;
	private boolean logout;
	private MJPosition position;
	private boolean zhuang;
	private Stack<Integer> hupaiList;
	private CoupleMajiang.PBTingReq huCache = null;
	private List<CoupleMajiang.PBTingItem> resultCache = new ArrayList<>();
	private boolean defeat = false;
	private MJDesk desk;
	private boolean isSelfGang = false;

	public PlayerDeskInfo() {
		keQueue = new PriorityQueue<Integer>();
		gangQueue = new Stack<Integer>();
		chiQueue = new LinkedList<Integer>();
		handCards = new ArrayList<Integer>();
		huaCards = new ArrayList<>();
		deskPaiStack = new Stack<>();
		resetChiData = new ArrayList<>();
		hupaiList = new Stack<>();
	}

	public void clear() {
		keQueue.clear();
		chiQueue.clear();
		gangQueue.clear();
		handCards.clear();
		huaCards.clear();
		gangIng = false;                                //杠手, 检查杠上开花
		hujiaoIng = false;
		isSelfGang = false;
		deskPaiStack.clear();
		discardTimes = 0;
		addTimes = 0;
		baoTing = false;
		tianting = false;
		defeat = false;
		type = MenFengType.DONG;
		baotingDiscardTimes = 0;
		ready.set(false);
		timeoutTimes = 0;
		tingMap.clear();
		tingDropValue = 0;
		resetChiData.clear();
		hupaiList.clear();
		setTianting(false);
	}


	public void addHuaCard(int value) {
		huaCards.add(value);
	}

	public void addHandCard(int value) {
		handCards.add(value);
	}

	//default from left to right
	public void addChiLeft(int value) {
		int value1 = value + 1;
		int value2 = value + 2;
		removeHandCard(value1);
		removeHandCard(value2);
		resetChiData.add(new Pair<>(value, value));
		chiQueue.add(value);
	}

	public void addChiMiddle(int value) {
		int value1 = value - 1;
		int value2 = value + 1;
		removeHandCard(value1);
		removeHandCard(value2);
		resetChiData.add(new Pair<>(value1, value));
		chiQueue.add(value1);
	}

	public void addChiRight(int value) {
		int value1 = value - 1;
		int value2 = value - 2;
		removeHandCard(value1);
		removeHandCard(value2);
		resetChiData.add(new Pair<>(value2, value));
		chiQueue.add(value2);
	}

	public int getTotalCardCotainGangChike() {
		return (chiQueue.size() + gangQueue.size() + keQueue.size()) * 3 + handCards.size();
	}

	public MJPosition getPosition() {
		return position;
	}

	@Override
	public int getPositionValue() {
		return position == null ? 0 : position.getValue();
	}

	public void setPosition(MJPosition position) {
		this.position = position;
	}

	public Stack<Integer> getDeskPaiStack() {
		return deskPaiStack;
	}

	public void setDeskPaiStack(Stack<Integer> deskPaiStack) {
		this.deskPaiStack = deskPaiStack;
	}

	public void removeHandCard(int value) {
		Iterator<Integer> iter = handCards.iterator();
		while (iter.hasNext()) {
			Integer cardValue = iter.next();
			if (cardValue.intValue() == value) {
				iter.remove();
				break;
			}
		}
	}


	public void setHuCache(CoupleMajiang.PBTingReq huCache) {
		this.huCache = huCache;
	}

	public void addTingResult(List<Pair<Integer, List<Pair<Integer, Integer>>>> result) {
		result.forEach(e -> tingMap.put(e.getLeft(), e.getRight()));
	}

	public boolean isLeave() {
		return leave;
	}

	public void setLeave(boolean leave) {
		this.leave = leave;
	}

	public void addKe(int value) {
		Iterator<Integer> iter = handCards.iterator();
		int removeCount = 0;
		while (iter.hasNext()) {
			Integer cardValue = iter.next();
			if (cardValue.intValue() == value) {
				iter.remove();
				removeCount++;
			}
			if (removeCount == 2) {
				break;
			}
		}
		keQueue.add(value);
	}

	public void addGang(int value, boolean self) {
		Iterator<Integer> iter = handCards.iterator();
		keQueue.remove(value);
		while (iter.hasNext()) {
			Integer cardValue = iter.next();
			if (cardValue.intValue() == GameUtil.getRealValue(value)) {
				iter.remove();
			}
		}
		gangQueue.add(value);
	}

	// 抢杠胡后把杠移除
	public void removeBuGang(){
		int value = gangQueue.pop();
		keQueue.add(value);
	}


	public int getLastGangValue() {
		int size = gangQueue.size();
		if (size == 0) {
			return 0;
		}
		return gangQueue.peek();
	}

	public void addHupaiCard(int value) {
		hupaiList.add(value);
	}

	public int getLastHuPaiCard(){
		return hupaiList.peek();
	}

	public Collection<Integer> getHuPaiList() {
		return new ArrayList<>(hupaiList);
	}

	public int getTingDropValue() {
		return tingDropValue;
	}

	public void setTingDropValue(int tingDropValue) {
		this.tingDropValue = tingDropValue;
	}

	public boolean isBaoTing() {
		return baoTing;
	}

	public boolean haveCard(int value){
        return handCards.contains(value);
    }

	public int getLastHandCard() {
		int size = handCards.size();
		return handCards.get(size - 1);
	}

	public int getDropTypeCard(MajongType type) {
		for (Integer value : handCards) {
			if (GameUtil.getMajongTypeByValue(value) == type) {
				return value;
			}
		}
		return getLastHandCard();
	}

	public void setBaoTing(boolean baoTing) {
		if (isZhuang() && baoTing && neverHandCard()) {
			this.tianting = true;
		}
		if (!isZhuang() && baoTing && getAddHandTimes() == 1) {
			this.tianting = true;
		}
		this.baoTing = baoTing;
	}

	public void doTing(int dropValue) {
		for (CoupleMajiang.PBTingItem item : huCache.getTingInfoList()) {
			if (item.getDropCard() == dropValue) {
				resultCache.add(item);
			}
		}
	}

	public boolean canHu(int value) {
		for (Pair<Integer, Integer> pair : tingMap.get(resultCache.get(0).getDropCard())) {
			if (pair.getLeft().intValue() == value) {
				return true;
			}
		}
		return false;
	}

	public boolean isTianting() {
		return tianting;
	}

	public void setTianting(boolean tianting) {
		this.tianting = tianting;
	}

	public int getDiscardTimes() {
		return discardTimes;
	}

	public int getBaotingDiscardTimes() {
		return baotingDiscardTimes;
	}


	public void addBaotingDiscardTimes() {
		this.baotingDiscardTimes++;
	}

	public void discardCard(int card) {
		removeHandCard(card);
		getDeskPaiStack().push(card);
		discardTimes++;
		if (baoTing) {
			baotingDiscardTimes++;
		}
	}

	public boolean dropCanTing(int value) {
		return tingMap.keySet().contains(value);
	}


	public boolean isLogout() {
		return logout;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public int getTimeoutTimes() {
		return this.timeoutTimes;
	}

	public void addTimeoutTimes() {
		this.timeoutTimes++;
	}

	public int getAddHandTimes() {
		return addTimes;
	}

	public boolean neverHandCard() {
		return this.addTimes == 0;
	}

	public void addHandTimes() {
		addTimes++;
	}

	public boolean isGangIng() {
		return gangIng;
	}

	public void setGangIng(boolean gangIng) {
		this.gangIng = gangIng;
	}
	
	public boolean isHujiaoIng() {
		return hujiaoIng;
	}

	public void setHujiaoIng(boolean hujiaoIng) {
		this.hujiaoIng = hujiaoIng;
	}

	public Queue<Integer> getKeQueue() {
		return keQueue;
	}

	public void setKeQueue(Queue<Integer> keQueue) {
		this.keQueue = keQueue;
	}

	public Queue<Integer> getChiQueue() {
		return chiQueue;
	}

	public void setChiQueue(Queue<Integer> chiQueue) {
		this.chiQueue = chiQueue;
	}

	public Stack<Integer> getGangQueue() {
		return gangQueue;
	}

	public int getQiurenCount() {
		int count = 0;
		for (Integer value : keQueue) {
			if (value < GameConst.AN_MASK) {
				count++;
			}
		}
		for (Integer value : gangQueue) {
			if (value < GameConst.AN_MASK) {
				count++;
			}
		}
		return chiQueue.size() + count;
	}

	public boolean isDefeat() {
		return defeat;
	}

	public void setDefeat(boolean defeat) {
		this.defeat = defeat;
	}

	public boolean isZhuang() {
		return zhuang;
	}

	public void setZhuang(boolean zhuang) {
		this.zhuang = zhuang;
	}
	
	public MJDesk getDesk() {
		return desk;
	}

	public void setDesk(MJDesk desk) {
		this.desk = desk;
	}

	public boolean isSelfGang() {
		return isSelfGang;
	}

	public void setSelfGang(boolean isSelfGang) {
		this.isSelfGang = isSelfGang;
	}

	public List<Pair<Integer, Integer>> getResetChiData() {
		return resetChiData;
	}

	public MenFengType getType() {
		return type;
	}

	public void setType() {
		this.type = zhuang ? MenFengType.DONG : MenFengType.XI;
	}

	public void setGangQueue(Stack<Integer> gangQueue) {
		this.gangQueue = gangQueue;
	}

	public List<Integer> getHandCards() {
		return handCards;
	}

	public List<Integer> getAllCards() {
		List<Integer> result = new ArrayList<>();
		result.addAll(handCards);
		for (Integer value : chiQueue) {
			GameUtil.addChiToCardList(result, value);
		}
		for (Integer value : gangQueue) {
			GameUtil.addKeToCardList(result, value);
		}
		for (Integer value : keQueue) {
			GameUtil.addKeToCardList(result, value);
		}
		return result;
	}

	public List<Integer> getHandCardsCopy() {
		return new ArrayList<>(handCards);
	}

	public void setHandCards(List<Integer> handCards) {
		this.handCards = handCards;
	}

	public List<Integer> getHuaCards() {
		return huaCards;
	}

	public boolean isReady() {
		return ready.get();
	}

	public List<Integer> getOperationList() {
		return operationList;
	}

	public void setOperationList(List<Integer> operationList) {
		this.operationList = operationList;
	}

	public void setReady(boolean ready) {
		this.ready.set(ready);
	}

	public boolean isHuPai() {
		return hupaiList.size() > 0;
	}

	public boolean ignorePengAndChi() {
		if (baoTing) {
			return true;
		}
		if (isHuPai()) {
			return true;
		}
		return false;
	}

	public int getGenCount() {
		List<Integer> cardList = new ArrayList<>(handCards);
		for (Integer ke : keQueue) {
			cardList.add(ke);
			cardList.add(ke);
			cardList.add(ke);
		}
		for (Integer chi : chiQueue) {
			cardList.add(chi);
			cardList.add(chi + 1);
			cardList.add(chi + 1);
		}
		int genCount = 0;
		Set<Integer> cardKinds = new HashSet<>();
		cardKinds.addAll(cardKinds);
		for (Integer kind : cardKinds) {
			if (GameUtil.getCountOfValue(cardList, kind) == 4) {
				genCount++;
			}
		}
		return genCount + gangQueue.size();
	}


	private int getTypeCardCount(MajongType type) {
		int count = 0;
		for (Integer card : handCards) {
			if (GameUtil.getMajongTypeByValue(card) == type) {
				count++;
			}
		}
		return count;
	}

	public List<Integer> getSwitchCardsBySystem() {
		MajongType type = getMinCardAndGt3(getAllCardCount());
		List<Integer> cardList = new ArrayList<>();
		for (Integer card : handCards) {
			if (GameUtil.getMajongTypeByValue(card) == type) {
				cardList.add(card);
			}
		}
		Collections.sort(cardList);
		return Arrays.asList(cardList.get(0), cardList.get(1), cardList.get(2));
	}

	private Map<MajongType, Integer> getAllCardCount() {
		Map<MajongType, Integer> result = new HashMap<>();
		for (Integer card : handCards) {
			MajongType type = GameUtil.getMajongTypeByValue(card);
			Integer value = result.get(type);
			if (value == null) {
				result.put(type, 1);
			} else {
				result.put(type, value + 1);
			}
		}
		return result;
	}

	private MajongType getMinCardAndGt3(Map<MajongType, Integer> cardMap) {
		MajongType result = null;
		for (Map.Entry<MajongType, Integer> entry : cardMap.entrySet()) {
			if (result == null) {
				result = entry.getKey();
				continue;
			}
			if (entry.getValue() < 3) {
				continue;
			}
			int resultCount = cardMap.get(result);
			if (resultCount < 3 || resultCount > entry.getValue()) {
				result = entry.getKey();
			}
		}
		return result;
	}


	public MajongType getMinType() {
		int tiaoCount = 0;
		int wanCount = 0;
		int tongCount = 0;
		for (Integer card : handCards) {
			if (GameUtil.isWanCard(card)) {
				wanCount++;
			}
			if (GameUtil.isTongCard(card)) {
				tongCount++;
			}
			if (GameUtil.isTiaoCard(card)) {
				tiaoCount++;
			}
		}
		int minCount = MiscUtil.min(tiaoCount, wanCount, tongCount);
		if (minCount == tiaoCount) {
			return MajongType.TIAO;
		} else if (minCount == tongCount) {
			return MajongType.TONG;
		} else {
			return MajongType.WAN;
		}
	}


}
