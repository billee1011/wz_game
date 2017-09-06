package logic.majiong;

import actor.LogicActorManager;
import common.LogHelper;
import config.AssisInfoProvider;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.JsonUtil;
import config.bean.AssisSeriation;
import config.provider.PersonalConfRoomProvider;
import data.MoneySubAction;
import define.DealZimo;
import logic.debug.ArrayPai;
import logic.define.GameType;
import logic.majiong.define.MJPosition;
import logic.majiong.define.MJType;
import logic.majiong.xnStragety.XnStragetyManager;
import logic.majiong.xueniu.XNOneCalRecord;
import logic.record.TaxRecordUtil;
import logic.record.detail.MjDetail;
import logic.record.detail.OnePosDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.Xueniu;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protobuf.creator.XueniuCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.*;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Administrator on 2016/12/27.
 */
public abstract class SCMJDesk extends MJDesk {
	private Map<Integer, Xueniu.PBXueNiuTotalCalculate> calculateResMap = new HashMap<>();

	public Xueniu.PBXueNiuTotalCalculate getCalCulate(PlayerInfo player) {
		return calculateResMap.get(player.getPlayerId());
	}

	private int switchType;

	private int baseScore;

	private boolean switchCard = true;

	private Map<MJPosition, List<Integer>> playerSwitchCards = new HashMap<>();

	private Map<MJPosition, MajongType> playerChoseType = new HashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(XueNiuDesk.class);

	private Map<MJPosition, List<Xueniu.PBCalculate>> alreadyGangCal = new HashMap<>();

	private List<PlayerInfo> defeatList = new ArrayList<>();

	private List<XueniuFanType> extraFanList = new ArrayList<>();

	private boolean tran;

	private int maxFan;

	private DealZimo dealZimo = DealZimo.ZIMO_JIABEI;

	private int enterTimes;

	private boolean diangangMo = true;
	
	private MJPosition stopPosition = null;

	public SCMJDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId, playerList);
		this.maxFan = 99999;
		this.baseScore = CoupleRoomInfoProvider.getInst().getBaseScoreOfRoom(roomConfId);
	}

	public SCMJDesk(int deskId,List<Integer> extraList, DealZimo dealZimo, boolean tran, int maxFan, int creator, int maxRounds
			, boolean switchCard, int baseScore, boolean personal, List<PlayerInfo> playerList, int enterTimes, boolean diangangMo) {
		super(deskId,creator, maxRounds, personal, 0, playerList);
		this.baseScore = baseScore;
		this.switchCard = switchCard;
		this.maxFan = maxFan;
		this.tran = tran;
		this.dealZimo = dealZimo;
		this.enterTimes = enterTimes;
		this.diangangMo = diangangMo;
		for (Integer value : extraList) {
			XueniuFanType type = XueniuFanType.getByValue(value);
            if (type != null) {
                extraFanList.add(type);
                if (type == XueniuFanType.TIANHU) {
                    extraFanList.add(XueniuFanType.DIHU);
                }
            }
        }
	}

	protected void mixAllCard() {
		paiPool = GameUtil.mixAllCard(MJType.XUELIU);
	}

	private Map<Integer, List<XNOneCalRecord>> calculateMap = new HashMap<>();

	private Xueniu.PBCalculate lastCalculate;

	private int flowMoney;

	private int taxMoney;

	@Override
	protected void initMjGameDeskInfo(List<PlayerInfo> playerList) {
		if (isPersonal()) {
			for (PlayerInfo player : playerList) {
				PlayerDeskInfo info = new PlayerDeskInfo();
				info.setDesk(this);
				info.setPosition(MJPosition.getByValue(player.getPosition()));
				player2InfoMap.put(player, info);
				postion2InfoMap.put(info.getPosition(), info);
			}
		} else {
			MJPosition position = MJPosition.EAST;
			for (PlayerInfo player : playerList) {
				PlayerDeskInfo info = new PlayerDeskInfo();
				info.setPosition(position);
				info.setDesk(this);
				player2InfoMap.put(player, info);
				postion2InfoMap.put(info.getPosition(), info);
				position = position.nextPosition();
			}
		}
	}

	@Override
	public int getBaseScore() {
		return this.baseScore;
	}

	@Override
	protected void beforeGame() {
		switchType = Randomizer.nextInt(3) + 1;
		if (switchCard) {
			stopOperationFuture();
			opBeginTime = MiscUtil.getCurrentSeconds();
			operationFuture = LogicActorManager.registerOneTimeTask(huanSanZhang, () -> notifyBeginSwitch(), getDeskId());
		}
	}
	
	private void checkAllPlayerDefeat() {
		for (PlayerInfo player : player2InfoMap.keySet()) {
			if (!getPlayerDeskInfo(player).isDefeat()) {
				checkPlayerDefeat(player);
			}
		}
	}

	private void notifyBeginSwitch() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		for (PlayerInfo player : player2InfoMap.keySet()) {
            player.write(ResponseCode.XUENIU_BEGIN_SWAP, null);
        }
		if (getRoomCreator() == null) {
			opBeginTime = MiscUtil.getCurrentSeconds();
			stopOperationFuture();
			operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> switchCardsTimeEnd(), getDeskId());
		}
	}

	private void switchCardsTimeEnd() {
		if (playerSwitchCards.size() == 4) {
            return;
        }
		for (MJPosition position : MJPosition.values()) {
			List<Integer> cards = playerSwitchCards.get(position);
			if (cards == null) {
				cards = postion2InfoMap.get(position).getSwitchCardsBySystem();
				playerSwitchCards.put(position, cards);
			}
		}
		logger.debug("{}|{} switch time end and begin switch cards  hahaha ", getDeskId(),getGameId());
		onSwitchCards();
	}

	public List<Integer> getSwitchCards(PlayerInfo player) {
		return playerSwitchCards.get(getPlayerPositionType(player));
	}

	//获取玩家两次杠上炮之间的呼叫转移结算记录
	private List<Xueniu.PBCalculate> getGangCalList(MJPosition position) {
		List<Xueniu.PBCalculate> result = new ArrayList<>();
		List<XNOneCalRecord> recordsGuafeng = calculateMap.get(XueNiuCalType.GUAFENG);
		if (recordsGuafeng != null) {
			for (XNOneCalRecord record : recordsGuafeng) {
				if (record.getPosition() == position.getValue()) {
					result.add(record.getCal());
				}
			}
		}
		List<XNOneCalRecord> recordsXiayu = calculateMap.get(XueNiuCalType.XIAYU);
		if (recordsXiayu != null) {
			for (XNOneCalRecord record : recordsXiayu) {
				if (record.getPosition() == position.getValue()) {
					result.add(record.getCal());
				}
			}
		}
		List<Xueniu.PBCalculate> alreadyCalList = alreadyGangCal.get(position);
		if (alreadyCalList != null) {
			result.removeAll(alreadyCalList);
			alreadyCalList.addAll(result);
		} else {
			alreadyGangCal.put(position, result);
		}
		return result;
	}

	private void choseTypeTimeEnd() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		if (playerChoseType.size() < 4) {
			for (MJPosition position : MJPosition.values()) {
				MajongType type = playerChoseType.get(position);
				if (type == null) {
					playerChoseType.put(position, getMinType(position));
				}
			}
			onChoseType();
		}
	}

	private MajongType getMinType(MJPosition position) {
		PlayerDeskInfo info = postion2InfoMap.get(position);
		if (info == null) {
			return MajongType.WAN;
		}
		return info.getMinType();
	}

	public boolean isChoseTypeEnd() {
		return playerChoseType.size() >= 4;
	}

	private void onChoseType() {
		logger.debug("{}|{} on chose type time  end  and begin chose type ", getDeskId(),getGameId());
		for (PlayerInfo player : player2InfoMap.keySet()) {
			player.write(ResponseCode.XUENIU_CHOSE_TYPE_END, createPBPairList(playerChoseType));
		}
		for (int i = MJPosition.EAST.getValue(); i <= MJPosition.NORTH.getValue(); i++) {
			record.addQueType(playerChoseType.get(MJPosition.getByValue(i)).getValue());
		}

		// 若不是私房,超时了就要出牌了
        if (!isPersonal()) {
            PlayerInfo p = getPlayerByPosition(currentTurn);
            sortHandCard(p);
			stopOperationFuture();
            opBeginTime = MiscUtil.getCurrentSeconds();
			autoChuPaiBeginFuture = LogicActorManager.registerOneTimeTask(time8, () -> discardCard(p, getPlayerDeskInfo(p).getLastHandCard()), getDeskId());
        }
	}

	private void onSwitchCards() {
		if(switchCard){
			for (PlayerInfo player : player2InfoMap.keySet()) {
				switchOnePlayerCard(player);
			}
		}
		List<List<Integer>> add = new ArrayList<>();
		List<List<Integer>> remove = new ArrayList<>();
		for (int i = MJPosition.EAST.getValue(); i <= MJPosition.NORTH.getValue(); i++) {
			MJPosition pos = MJPosition.getByValue(i);
			if (pos == null) {
				continue;
			}
			remove.add(playerSwitchCards.get(pos));
			add.add(getSwitchCards(pos));
		}
		record.addChangeInfo(switchType, add, remove);
		logger.error("{}|{} en xue niu switch cards end and begin chose type ", getDeskId(),getGameId());
		if (getRoomCreator() == null) {
			stopOperationFuture();
			opBeginTime = MiscUtil.getCurrentSeconds();
			operationFuture = LogicActorManager.registerOneTimeTask(time12, () -> choseTypeTimeEnd(), getDeskId());
		}
	}

	private void switchOnePlayerCard(PlayerInfo player) {
		List<Integer> removeCards = playerSwitchCards.get(getPlayerPositionType(player));
		List<Integer> switchCards = getSwitchCards(getPlayerPositionType(player));
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (removeCards != null) {
			removeCards.forEach(e -> info.removeHandCard(e));
		}
		if (switchCards != null) {
			switchCards.forEach(e -> info.addHandCard(e));
		}
		System.out.println(" the info position is " + info.getPosition());
		info.getHandCards().forEach(e -> System.out.print(e + " ,"));
		player.write(ResponseCode.XUENIU_BEGIN_CHOSE_TYPE, XueniuCreator.createPBBeginChoseType(removeCards, switchCards, switchType));
	}


	private Common.PBPairList createPBPairList(Map<MJPosition, MajongType> map) {
		Common.PBPairList.Builder builder = Common.PBPairList.newBuilder();
		map.entrySet().forEach(e -> builder.addList(CommonCreator.createPBPair(e.getKey().getValue(), e.getValue().getValue())));
		return builder.build();
	}

	private List<Integer> getSwitchCards(MJPosition position) {
		XueNiuSwitchDirection direction = XueNiuSwitchDirection.getByValue(switchType);
		switch (direction) {
			case SHUN:
				return playerSwitchCards.get(position.prePosition());
			case NI:
				return playerSwitchCards.get(position.nextPosition());
			case DUI:
				return playerSwitchCards.get(position.oppositePosition());
		}
		return null;
	}


	public void switchXueniuCards(PlayerInfo player, List<Integer> cards) {
		if (playerSwitchCards.size() == 4) {
			return;
		}
		List<Integer> original = playerSwitchCards.get(player);
		if (original != null) {
			return;
		}
		if (!GameUtil.checkHave(getPlayerDeskInfo(player), cards)) {
			logger.error("{}|{} 玩家{} 手牌并沒有{} ", getDeskId(),getGameId(),player.getPlayerId(),cards.toString());
			return;
		}
		playerSwitchCards.put(getPlayerPositionType(player), cards);
		if (playerSwitchCards.size() == 4) {
			logger.debug("{}|{} four player siwtch card succ and begin switch ", getDeskId(),getGameId());
			stopOperationFuture();
			onSwitchCards();
		}
	}

	public void playerChoseType(PlayerInfo player, int type) {
		if (playerChoseType.size() == 4) {
			logger.debug("{}|{} already  chose tyoe and no need any more ", getDeskId(),getGameId());
			return;
		}

		MJPosition position = getPlayerPositionType(player);
		if(position == null){
			logger.error("{}|{} 没有该玩家{}", getDeskId(),getGameId(), player.getPlayerId());
			return;
		}

		if (playerChoseType.get(position) != null) {
			logger.info("{}|{} 玩家{}已经定缺了",getDeskId(),getGameId(),player.getPlayerId());
			return;
		}

		MajongType choseType = MajongType.getByValue(type);
		if (choseType == null) {
			return;
		}
		playerChoseType.put(getPlayerPositionType(player), MajongType.getByValue(type));
		if (playerChoseType.size() == 4) {
			stopOperationFuture();
			onChoseType();
		}
	}

	@Override
	protected void calculateGang(PlayerInfo player, int value) {
		int fanshu = 0;
		boolean dianpao = (getPlayerPositionType(player) != currentTurn);
		if (dianpao || value > GameConst.AN_MASK) {
			fanshu = 2;
		} else {
			fanshu = 1;
		}
		List<Pair<Integer, Integer>> pairList =
				getCalResult(player, fanshu, dianpao);
		Xueniu.PBCalculate calResult = null;
		if (!dianpao && (fanshu == 1)) {
			calResult = XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.GUAFENG.getValue(), pairList, 0, fanshu);
			addCalculate(XueNiuCalType.GUAFENG.getValue(), null, calResult);
		} else {
			calResult = XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.XIAYU.getValue(), pairList, 0, fanshu);
			addCalculate(XueNiuCalType.XIAYU.getValue(), null, calResult);
		}
		notifyCalculate(calResult);
		checkAllPlayerDefeat();
	}

	@Override
	public void chiPai(PlayerInfo player, int type) {
		logger.debug(" xue liu majiang  can't chi pai ");

		if (!(gameState2 == MajongState2.CHU ||gameState2 == MajongState2.GUO)) {
			logger.error("{}|{} 玩家{} 状态{}不对,不能吃牌", getDeskId(),getGameId(),player.getPlayerId(), gameState2);
			return;
		}

        ChiType chiType = ChiType.getByValue(type);
        if (chiType == null) {
            return;
        }
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (currOperationTurn != info.getPosition()) {
            logger.error("{}|{} 玩家{} 沒輪到該位置{}吃牌", getDeskId(),getGameId(),player.getPlayerId(), info.getPosition());
            return;
        }

        List<Integer> ops = operationList.get(currOperationTurn);

        if (ops == null || !ops.contains(MajiangOperationType.CHI.getValue())) {
            logger.error("{}|{} 玩家{} 該位置{}沒有操作列表,不能吃", getDeskId(),getGameId(),player.getPlayerId(), currOperationTurn);
            return;
        }

        if (!GameUtil.checkChi(info, getCurrentTurnInfo().getDeskPaiStack().peek(), chiType)) {
			return;
		}
		setGangFalse();
		stopOperationFuture();
        removePositionOperation(info.getPosition());

		setGameState2(MajongState2.CHI);

		thinkAftChi();
	}

	@Override
	public void sortHandCard(PlayerInfo player) {
		PlayerDeskInfo info = player2InfoMap.get(player);
		MajongType chooseType = playerChoseType.get(info.getPosition());
		info.getHandCards().sort((h1, h2) -> {
			MajongType h1Type = GameUtil.getMajongTypeByValue(h1);
			MajongType h2Type = GameUtil.getMajongTypeByValue(h2);
			if (h1Type != h2Type) { // 定缺影响排序
				if (h1Type == chooseType) {
					return 1;
				} else if (h2Type == chooseType) {
					return -1;
				}
			}
			return h1.intValue() > h2.intValue() ? 1 : -1;
		});
	}

	private List<XueniuFanType> getTypeListContainGen(PlayerDeskInfo info, XueniuFanType type) {
		List<XueniuFanType> typeList = new ArrayList<>();
		typeList.add(type);
		int genCount = info.getGenCount();
		if (type == XueniuFanType.SHIBALUOHAN || type == XueniuFanType.QINGSHIBALUOHAN) {
			genCount = 0;
		}
		if (type == XueniuFanType.LONGQIDUI) {
			genCount -= 1;
		}
		for (int i = 0; i < genCount; i++) {
			typeList.add(XueniuFanType.GEN);
		}
		return typeList;
	}


	@Override
	protected void selectZhuang() {
		int random = Randomizer.nextInt(4) + 1;
		ArrayPai.getInst().setZhuangForXueliuMj(random);
		MJPosition position = MJPosition.getByValue(random);
		postion2InfoMap.get(position).setZhuang(true);
		setCurrentTurn(position);
		currOperationTurn = position;
	}

	public void hupai(PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> jiang) {
		stopAutoChuPaiBeginFuture();
//		logger.info("胡牌的stack,{}",getClassPath());

		if (stop) {
			return;
		}
        if (!((gameState2 == MajongState2.GUO || gameState2 == MajongState2.HU || gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.MO || gameState2 == MajongState2.CHU || isGangIng))) {
            logger.error("{}|{}玩家{} 当前状态{}不对，不能胡牌", getDeskId(), getGameId(), player.getPlayerId(), gameState2);
            return;
        }

		PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (currOperationTurn != info.getPosition()) {
            logger.error("{}|{}玩家{} 沒輪到當前位置{}胡牌,當前操作位置為{}", getDeskId(), getGameId(), player.getPlayerId(), info.getPosition(), currOperationTurn);
            return;
        }

		stopOperationFuture();
		removePositionOperation(info.getPosition());
		logger.info("{}|{}玩家{}位置{} 胡牌", getDeskId(), getGameId(), player.getPlayerId(), currOperationTurn);

		List<Integer> chiList = new ArrayList<>(info.getChiQueue());
		for (Integer value : chi) {
			chiList.add(value);
		}
		List<Integer> keList = new ArrayList<>(info.getKeQueue());
		for (Integer value : ke) {
			keList.add(value + GameConst.AN_MASK);
		}
		XueniuFanType type = XnStragetyManager.getInst().calFinalFanType(false, info, info.getGangQueue(), new LinkedList<>(keList), new LinkedList<>(chiList), jiang, extraFanList);
		if (type == null) {
			return;
		}
        logger.info("{}|{}玩家{} 胡牌{},chi{},ke{},jiang{}", getDeskId(), getGameId(), player.getPlayerId(), JsonUtil.getJsonString(type), JsonUtil.getJsonString(chi), JsonUtil.getJsonString(ke), JsonUtil.getJsonString(jiang));

		int resultFan = type.getFan();
		logger.info("{}|{}玩家{} 胡牌,番數為{},結果為{}", getDeskId(), getGameId(), player.getPlayerId(), resultFan, type);
		boolean selfTurn = isSelfTurn(player);
		List<XueniuFanType> typeList = getTypeListContainGen(info, type);
		int huValue = getHuValue(player);
		if (selfTurn) {
			if (!(!diangangMo && info.isGangIng())) {
				resultFan = dealZimo.getResultFan(resultFan);
				logger.info("{}|{}玩家{} 自摸  番數变为{} }", getDeskId(), getGameId(), player.getPlayerId(), resultFan);
			}
			info.removeHandCard(huValue);
		} else {
            if (!isHuIng()) {   // 點炮的賦值
                dianPaoPos = currentTurn;
            }

			if (isGangIng) {
				typeList.add(XueniuFanType.QIANGGANG);
				resultFan <<= 1;
				logger.info("{}|{}玩家{} 抢杠胡  番數变为{} }", getDeskId(), getGameId(), player.getPlayerId(), resultFan);

				// 抢了谁的gang，要把他的杠变成刻
				dealGangTurnPao();
			} else {
				if (getGangPosition() != null) { 
					typeList.add(XueniuFanType.GANGSHANGPAO);
					resultFan <<= 1;
					logger.info("{}|{}玩家{} 杠上炮  番數变为{} }", getDeskId(), getGameId(), player.getPlayerId(), resultFan);
 					if (tran) {
 						info.setHujiaoIng(true);
//						calHujiaozhuanyi(player);
					}
				}
			}
		}
		for (XueniuFanType xueniuFanType : typeList) {
			if (xueniuFanType == XueniuFanType.GEN) {
				resultFan <<= 1;
				logger.info("{}|{}玩家{} 算根 番數变为{} }", getDeskId(), getGameId(), player.getPlayerId(), resultFan);
			}
		}
		if (selfTurn && (info.isGangIng() || info.isSelfGang())) {
			typeList.add(XueniuFanType.GANGSHANGHUA);
			resultFan <<= 1;
			logger.info("{}|{}玩家{} 杠上花  番數变为{} }", getDeskId(), getGameId(), player.getPlayerId(), resultFan);
		}
		resultFan = Math.min(resultFan, maxFan);
		logger.info("{}|{}玩家{} 最大允许番数{} 最终番数为{}}", getDeskId(), getGameId(), player.getPlayerId(), maxFan, resultFan);
        if (isHuIng()) {    // 一炮多响的方式
            huValue = (huValue > 100 ? huValue : huValue + 100);
        }
        boolean zimo = false;
		PlayerInfo failPlayer = null;
		List<Pair<Integer, Integer>> pairList = null;
		XueNiuCalType xueNiuCalType;
		if (!selfTurn) {
			failPlayer = getPlayerByPosition(dianPaoPos);
			xueNiuCalType = XueNiuCalType.HU;
//			 点炮算钱
//			pairList = getDianpaoCalResult(player, getPlayerByPosition(dianPaoPos), resultFan); 
		} else {
			// 如果杠上花 并且不是自己杠 是别人点杠 并且 是点杠胡当自摸模式
			if (!diangangMo && typeList.contains(XueniuFanType.GANGSHANGHUA) && !info.isSelfGang()) {
				failPlayer = getPlayerByPosition(lastDianGangPos);
				xueNiuCalType = XueNiuCalType.ZIMO_DIANPAO; // 自摸儅點跑
//				 点炮算钱
//				pairList = getDianpaoCalResult(player, getPreTurnPlayer(), resultFan);
			} else {
				pairList = getZimoCalResult(player, resultFan);
				zimo = true;
//				info.addHupaiCard(GameUtil.getRealHuValue(huValue));
				info.addHupaiCard(huValue); // 現在存帶掩碼的值
				xueNiuCalType = XueNiuCalType.ZIMO;
				Xueniu.PBCalculate calResult = XueniuCreator.createPBCalculate(getPlayerPosition(player), xueNiuCalType.getValue(), pairList, huValue, resultFan, typeList);
				addCalculate(xueNiuCalType.getValue(), typeList, calResult);
				notifyCalculate(calResult);
				checkAllPlayerDefeat();
			}
		}
			
		// 如果不是自摸  有可能胡多家   结算放到  全部胡完之后处理
		if (!zimo) {
//			info.addHupaiCard(GameUtil.getRealHuValue(huValue));
			info.addHupaiCard(huValue); // 現在存帶掩碼的值
			addHuInfo(new HuInfo(resultFan, huValue, getBaseScore() * resultFan, player, failPlayer, xueNiuCalType, typeList));
		} else { // 自摸當前檢測 血戰模式 是否胡3傢結束
			if (isWillEnd()) {
				onAllCardOver(false);
				return;
			}
		}

//		if (getGameType() == GameType.XUEZHAN && threePeopleHupai()) {
//		if (isWillEnd()) {
//			onAllCardOver(false);
//			return;
//		}
		setHuIng(true);
		setGameState2(MajongState2.HU);
		pengWating = null;
		gangWating = null;
//		currOperationTurn = currentTurn.nextPosition();
        afterHuPai(getPlayerPositionType(player), zimo || !diangangMo);
    }

	private boolean isWillEnd() {
		int deadCount = 0;
		for (PlayerDeskInfo info : player2InfoMap.values()) {
			if (info.isDefeat() || getGameType() == GameType.XUEZHAN && info.isHuPai())
				deadCount++;
		}
		return deadCount >= 3;
	}

	private void notifyCalculate(Xueniu.PBCalculate cal) {
		for (PlayerInfo player1 : player2InfoMap.keySet()) {
			player1.write(ResponseCode.XUENIU_CALCULATE, cal);
		}
		record.addCalculateStep(cal.getType(), cal.getPosition(), cal.getCard(), cal.getTimes(), cal.getHuListList(), cal.getResultList());
	}
	
	private void calHujiaozhuanyi() {
		if (lastCalculate == null) {
			return;
		}
		if (lastCalculate.getType() != XueNiuCalType.GUAFENG.getValue()
				&& lastCalculate.getType() != XueNiuCalType.XIAYU.getValue()) {
			return;
		}
		PlayerInfo gangPlayer = null; // 谁在杠
		PlayerDeskInfo gangDeskPlayer = null;
		int needNum = 0; // 需要给多少人钱
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isGangIng()) {
				gangPlayer = entry.getKey();
				gangDeskPlayer = entry.getValue();
				continue;
			}
			if (!entry.getValue().isHujiaoIng()){
				continue;
			}
			needNum++;
		}
		if (needNum <= 0){
			logger.error("{}|{} 玩家 {} 不用被呼叫轉移  沒有呼叫轉移的人", getDeskId(), getGameId(), gangPlayer.getPlayerId());
			return;
		}
		if (gangPlayer.getCoin() <= 0){ //  沒錢不用呼叫轉移
			logger.error("{}|{} 玩家 {} 當前金幣為{} 無法被呼叫轉移", getDeskId(), getGameId(), gangPlayer.getPlayerId(), gangPlayer.getCoin());
			return;
		}
		int totalCoint = 0;
		int totalTimes = 0;
		for (Common.PBPair pair : lastCalculate.getResultList()) {
			if (pair.getKey() == gangDeskPlayer.getPositionValue()) {
				totalCoint += pair.getValue();
				totalTimes += lastCalculate.getTimes();
			}
		}
		int resultCoin = totalCoint / needNum;
		logger.info("{}|{} 玩家 {} 被呼叫轉移  需要支付{}人  總共應付{}", getDeskId(), getGameId(), gangPlayer.getPlayerId(), needNum, totalCoint);
		if (gangPlayer.getCoin() < totalCoint) { // 不够支付 扣除所有的钱
			resultCoin = gangPlayer.getCoin() / needNum;
			totalCoint = gangPlayer.getCoin();
		}
		gangPlayer.updateCoin(totalCoint, false);
		logger.info("{}|{} 玩家 {} 被呼叫轉移 總共實付{}", getDeskId(), getGameId(), gangPlayer.getPlayerId(), totalCoint);
		checkPlayerDefeat(gangPlayer);
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isGangIng()) {
				continue;
			}
			if (!entry.getValue().isHujiaoIng()){
				continue;
			}
			List<Pair<Integer, Integer>> pairList = new ArrayList<>();
			entry.getKey().updateCoin(resultCoin, true);
			pairList.add(new Pair<>(getPlayerPosition(entry.getKey()), resultCoin));
			pairList.add(new Pair<>(gangDeskPlayer.getPositionValue(), -resultCoin));
			Xueniu.PBCalculate calResult = XueniuCreator.createPBCalculate(getPlayerPosition(entry.getKey()), XueNiuCalType.TRAN.getValue(), pairList, 0, totalTimes);
			addCalculate(XueNiuCalType.TRAN.getValue(), null , calResult);
			notifyCalculate(calResult);
		}
		setHujiaoFalse();
	}

//	private void calHujiaozhuanyi(PlayerInfo player) {
//		if (lastCalculate == null) {
//			return;
//		}
//		MJPosition gangPosition = getCurrentTurn();
//		if (lastCalculate.getPosition() != gangPosition.getValue()) {
//			return;
//		}
//		if (lastCalculate.getType() != XueNiuCalType.GUAFENG.getValue()
//				&& lastCalculate.getType() != XueNiuCalType.XIAYU.getValue()) {
//			return;
//		}
//		int totalNum = 0;
//		int totalTimes = 0;
//		for (Common.PBPair pair : lastCalculate.getResultList()) {
//			if (pair.getKey() == gangPosition.getValue()) {
//				totalNum += pair.getValue();
//				totalTimes += lastCalculate.getTimes();
//			}
//		}
//		List<Pair<Integer, Integer>> pairList = new ArrayList<>();
//		player.updateCoin(totalNum, true);
//		getPlayerByPosition(gangPosition).updateCoin(totalNum, false);
//		pairList.add(new Pair<>(getPlayerPosition(player), totalNum));
//		pairList.add(new Pair<>(gangPosition.getValue(), -totalNum));
//		addCalculate(XueNiuCalType.TRAN.getValue(), null
//				, XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.TRAN.getValue(), pairList, 0, totalTimes));
//	}


	public Map<MJPosition, MajongType> getPlayerChoseType() {
		return playerChoseType;
	}

	private boolean threePeopleHupai() {
		int huCount = 0;
		for (PlayerDeskInfo info : player2InfoMap.values()) {
			if (info.isHuPai()) {
				huCount++;
			}
		}
		return huCount >= player2InfoMap.size() - 1;
	}

	private void addCalculate(int type, List<XueniuFanType> typeList, Xueniu.PBCalculate result) {
		List<XNOneCalRecord> calList = calculateMap.get(type);
		if (calList == null) {
			calList = new ArrayList<>();
			calculateMap.put(type, calList);
		}
		if (type == XueNiuCalType.GUAFENG.getValue() || type == XueNiuCalType.XIAYU.getValue()){
			lastCalculate = result;
		}
		calList.add(new XNOneCalRecord(typeList, result));
	}

	@Override
	protected boolean canChi() {
		return false;
	}


	private List<Pair<Integer, Integer>> getZimoCalResult(PlayerInfo player, int fanshu) {
		logger.info("{}|{} 玩家 {} 自摸開始收錢", getDeskId(), getGameId(), player.getPlayerId());
		List<Pair<Integer, Integer>> pairList = new ArrayList<>();
		int resultTotal = 0; // 實際縂賺錢數
		for (PlayerInfo player1 : player2InfoMap.keySet()) {
			if (player1 == player) {
				continue;
			}
			if (getGameType() == GameType.XUEZHAN && getPlayerDeskInfo(player1).isHuPai()) {
				continue;
			}
			if (getPlayerDeskInfo(player1).isDefeat()) {
				continue;
			}
			int result = player1.getCoin() > getBaseScore() * fanshu ? getBaseScore() * fanshu : player1.getCoin();
			player1.updateCoin(result, false);
			logger.info("{}|{} 玩家 {} 應付 {} 實付 {}", getDeskId(), getGameId(), player1.getPlayerId(), getBaseScore() * fanshu, result);
			pairList.add(new Pair<>(getPlayerPosition(player1), result * -1));
//			checkPlayerDefeat(player1);
			resultTotal += result;
		}
		player.updateCoin(resultTotal, true);
		logger.info("{}|{} 胡牌玩家 {} 實際收穫{}", getDeskId(), getGameId(), player.getPlayerId(), resultTotal);
		pairList.add(new Pair<>(getPlayerPosition(player), resultTotal));
		return pairList;
	}

	private List<Pair<Integer, Integer>> getDianpaoCalResult(PlayerInfo huPlayer, PlayerInfo dianpaoPlayer, int fanshu) {
		List<Pair<Integer, Integer>> pairList = new ArrayList<>();
		int result = getBaseScore() * fanshu;
		huPlayer.updateCoin(result, true);

		result = (dianpaoPlayer.getCoin() > result ? result : dianpaoPlayer.getCoin());
		dianpaoPlayer.updateCoin(result, false);

		checkPlayerDefeat(dianpaoPlayer);
		pairList.add(new Pair<>(getPlayerPosition(huPlayer), result));
		pairList.add(new Pair<>(getPlayerPosition(dianpaoPlayer), result * -1));
		return pairList;
	}

	public Pair<Integer, Integer> getCurGameType() {
		int type_gain = MoneySubAction.XUENIU_GAIN.getValue();
		int type_lose = MoneySubAction.XUENIU_LOSE.getValue();
		if (isPersonal()){
			type_gain = MoneySubAction.XUENIU_GAIN_PERSONAL.getValue();
			type_lose = MoneySubAction.XUENIU_LOSE_PERSONAL.getValue();
		}

		if (this.getGameType() == GameType.XUEZHAN) {
			type_gain = MoneySubAction.XUEZHAN_GAIN.getValue();
			type_lose = MoneySubAction.XUEZHAN_LOSE.getValue();
			if (isPersonal()){
				type_gain = MoneySubAction.XUEZHAN_GAIN_PERSONAL.getValue();
				type_lose = MoneySubAction.XUEZHAN_LOSE_PERSONAL.getValue();
			}
		}
		return new Pair<>(type_gain, type_lose);
	}


	private List<Pair<Integer, Integer>> getCalResult(PlayerInfo player, int fanshu, boolean dianpao) {
		List<Pair<Integer, Integer>> pairList = new ArrayList<>();
		int result = getBaseScore() * fanshu;
		if (!dianpao) {
			int totalMoney = 0; // 实际获得钱数
			for (PlayerInfo player1 : player2InfoMap.keySet()) {
				if (player1 == player) {
					continue;
				}
				if (getGameType() == GameType.XUEZHAN && getPlayerDeskInfo(player1).isHuPai()) {
					continue;
				}
				if (getPlayerDeskInfo(player1).isDefeat()) {
					continue;
				}
				result = player1.getCoin() > result ? result : player1.getCoin();
				player1.updateCoin(result, false);
				pairList.add(new Pair<>(getPlayerPosition(player1), result * -1));
//				checkPlayerDefeat(player1);
				totalMoney += result;
			}
			player.updateCoin(totalMoney, true);
			pairList.add(new Pair<>(getPlayerPosition(player), totalMoney));
		} else {
			PlayerInfo dianpaoPlayer = getCurrentPlayer();
			result = dianpaoPlayer.getCoin() > result ? result : dianpaoPlayer.getCoin();
			dianpaoPlayer.updateCoin(result, false);
			pairList.add(new Pair<>(getPlayerPosition(dianpaoPlayer), result * -1));
//			checkPlayerDefeat(dianpaoPlayer);
			player.updateCoin(result, true);
			pairList.add(new Pair<>(getPlayerPosition(player), result));
		}
		return pairList;
	}

	//你TM 快点去充钱啊, 傻逼, 老子真的很烦的啊, 你继续 才能进行游戏啊
	public void checkPlayerDefeat(PlayerInfo player) {
		if (player.getCoin() <= 0) {
			if (isPersonal()) {
				stop = true;
				if (!defeatList.contains(player)){
					defeatList.add(player);
				}
				for (PlayerInfo p : player2InfoMap.keySet()) {
					//这个玩家是否认输啊  并不是现在就认输啊, 其他的玩家
					if (!getPlayerDeskInfo(player).isDefeat()) {
						p.write(ResponseCode.XUENIU_OPERATION, CommonCreator.createPBInt32(getPlayerPosition(player)));
					}
				}
			} else {
				getPlayerDeskInfo(player).setDefeat(true);
				for (PlayerInfo p : player2InfoMap.keySet()) {
					p.write(ResponseCode.XUENIU_RENSHU, CommonCreator.createPBInt32(getPlayerPosition(player)));
				}
//				if (threePlayerDefeat()) {
				if (isWillEnd()) {
					onAllCardOver(true);
				}
			}
		}
	}

	public void playerWantContinue(PlayerInfo player) {
		//检查这个玩家是否能继续
		if (player.getCoin() < getBaseScore() * enterTimes) {
			return;
		}
		defeatList.remove(player);
		syncAllPlayerMoney();
		msgHelper.notifyMessage(ResponseCode.XUENIU_CONTINUE, CommonCreator.createPBInt32(getPlayerPosition(player)));
		if (defeatList.size() == 0) {
			stop = false;
			if (stopPosition != null){
				dealOneCard(stopPosition);
				stopPosition = null;
			}
		}
	}


	//玩家认输
	public void playerAdmitDefeat(PlayerInfo player) {
		if (!stop) {
			return;
		}
		getPlayerDeskInfo(player).setDefeat(true);
		for (PlayerInfo p : player2InfoMap.keySet()) {
			p.write(ResponseCode.XUENIU_RENSHU, CommonCreator.createPBInt32(getPlayerPosition(player)));
		}
		defeatList.remove(player);
		if (defeatList.size() == 0) {
			stop = false;
			if (stopPosition != null){
				dealOneCard(stopPosition);
				stopPosition = null;
			}
		}
//		if (threePlayerDefeat()) {
		if (isWillEnd()) {
			onAllCardOver(true);
		}
	}

	private boolean threePlayerDefeat() {
		int defeatCount = 0;
		for (PlayerDeskInfo info : player2InfoMap.values()) {
			if (info.isDefeat())
				defeatCount++;
		}
		if (defeatCount >= 3) {
			return true;
		}
		return false;
	}

	@Override
	protected void onAllCardOver(boolean disband) {
		logger.info(" ==================================== all card over  hahahahaha");
		//先检查大叫跟花猪
		if (!isGameing()) {
			logger.info("{}|{} the game is not begin  ===", getDeskId(),getGameId());
			return;
		}
		if (!disband) {
			checkHuaZhu();
			checkDajiao();
			checkDrawback();
//			checkAllPlayerDefeat();
		}
		Map<PlayerInfo, Integer> gainAndLose = allGainAndLose();
		for (PlayerInfo player : player2InfoMap.keySet()) {
			if (!isPersonal() && getGameType() == GameType.XUEZHAN && getPlayerDeskInfo(player).isLeave()) {
				continue;
			}
			if (player == getRoomCreator()) {
				continue;
			}

			sendTotalCalculate(player, gainAndLose);
		}
		PlayerInfo createInfo = getRoomCreator();
		if (createInfo != null) {
			sendTotalCalculate(createInfo, gainAndLose);
		}
		//这个时候同步了
		syncAllPlayerMoney();
		TaxRecordUtil.recordGameTaxInfo(startTime, player2InfoMap.size()
				, getGameId(), getGameType(), getConfId(), getZhuangId(), flowMoney, taxMoney, detail, this);
		if (player2InfoMap.size() == 4) {
			switchZhuangPlayer(getZhuangInfo().getPosition().nextPosition());
		}
		onGameEnd();
	}

	private void checkDrawback() {
		for (PlayerInfo player : player2InfoMap.keySet()) {
			if (!getPlayerDeskInfo(player).isDefeat()) {
				checkDrawback(player);
			}
		}
	}
	
	private void checkDrawback(PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (MajongRule.checkTingpai(info.getHandCards(), getIgnoreType(info.getPosition()))) {
			return;
		}
		//这个时候去查找玩家通过杠收的钱
		List<XNOneCalRecord> recordList = calculateMap.get(XueNiuCalType.GUAFENG.getValue());
		if (recordList == null) {
			recordList = new ArrayList<>();
		}
		List<XNOneCalRecord> xiayuList = calculateMap.get(XueNiuCalType.XIAYU.getValue());
		if (xiayuList != null) {
			recordList.addAll(xiayuList);
		}

		for (XNOneCalRecord record1 : recordList) {
			if (info.getPosition().getValue() != record1.getPosition()) {
				continue;
			}
			float payValue = 1;
			Xueniu.PBCalculate cal = record1.getCal();
			for (Common.PBPair pair : cal.getResultList()) {
				if (pair.getKey() == info.getPosition().getValue()) {
					if (player.getCoin() <= 0){
						return;
					}
					if (player.getCoin() < pair.getValue()) { // 钱不够付 算系数
						payValue = player.getCoin() / (float) pair.getValue();
						break;
					}
				}
			}
			for (Common.PBPair pair : cal.getResultList()) {
				List<Pair<Integer, Integer>> pairList = new ArrayList<>();
				int coin = 0;
				if (pair.getKey() == info.getPosition().getValue()) {
					coin = player.getCoin() > pair.getValue() ? pair.getValue() : player.getCoin();
					player.updateCoin(coin, false);
					pairList.add(new Pair<>(pair.getKey(), coin * -1));

					record.addCalculateStepCouple(XueNiuCalType.DRAWBACK.getValue(), getPlayerPosition(player), 0, 0, null, pairList);
				} else {
					PlayerInfo posPlayer = getPlayerByPosition(MJPosition.getByValue(pair.getKey()));
					if (posPlayer != null && !getPlayerDeskInfo(posPlayer).isDefeat()) {
						coin = (int) (-pair.getValue() * payValue);
						posPlayer.updateCoin(coin, true);
						pairList.add(new Pair<>(pair.getKey(), coin));

						record.addCalculateStepCouple(XueNiuCalType.DRAWBACK.getValue(), getPlayerPosition(posPlayer), 0, 0, null, pairList);
					}
				}
				addCalculate(XueNiuCalType.DRAWBACK.getValue(), null
						, XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.DRAWBACK.getValue(), pairList, 0, cal.getTimes()));
			}
		}
	}


	private void checkHuaZhu() {
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isDefeat()) {
				continue;
			}
			if (GameUtil.isHuaZhu(entry.getValue().getAllCards(), getIgnoreType(entry.getValue().getPosition()))) {
				calHuaZhuResult(entry.getKey());
			}
		}
	}

	private void calHuaZhuResult(PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (player.getCoin() <= 0){ //  沒錢不查
			logger.error("{}|{} 玩家 {} 當前金幣為{} 無法被查花豬", getDeskId(), getGameId(), player.getPlayerId(), player.getCoin());
			return;
		}
		int resultFan = Math.min(64, maxFan);
		int resultCoin = getBaseScore() * resultFan;
		int needNum = 0;  // 需要支付几个人
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isDefeat()) {
				continue;
			}
			if (entry.getKey() == player) {
				continue;
			}
			if (GameUtil.isHuaZhu(entry.getValue().getAllCards(), getIgnoreType(entry.getValue().getPosition()))) {
				continue;
			}
			needNum++;
		}
		int payCoin = resultCoin * needNum;
		logger.info("{}|{} 玩家 {} 被查花豬開始付錢  需要支付{}人  總共應付{}", getDeskId(), getGameId(), player.getPlayerId(), needNum, payCoin);
		if (player.getCoin() < resultCoin * needNum){ // 不够支付 扣除所有的钱
			resultCoin = (int) (player.getCoin() / Float.valueOf(needNum));  // 每人能分多少
			payCoin = player.getCoin();
		}
		player.updateCoin(payCoin, false);
		logger.info("{}|{} 玩家 {} 被查花豬  總共實付{}", getDeskId(), getGameId(), player.getPlayerId(), payCoin);
		
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			List<Pair<Integer, Integer>> pairList = new ArrayList<>();
			if (entry.getValue().isDefeat()) {
				continue;
			}
			if (entry.getKey() == player) {
				continue;
			}
			if (GameUtil.isHuaZhu(entry.getValue().getAllCards(), getIgnoreType(entry.getValue().getPosition()))) {
				continue;
			}
//			resultCoin = (int) (player.getCoin() > resultCoin ? resultCoin : player.getCoin());
//			player.updateCoin(resultCoin, false);
			entry.getKey().updateCoin(resultCoin, true);
			logger.info("{}|{} 玩家 {} 查花豬 收穫{}", getDeskId(), getGameId(), player.getPlayerId(), resultCoin);
			pairList.add(new Pair<>(getPlayerPosition(entry.getKey()), resultCoin));
			pairList.add(new Pair<>(getPlayerPosition(player), -resultCoin));
			addCalculate(XueNiuCalType.HUAZHU.getValue(), null
					, XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.HUAZHU.getValue(), pairList, 0, resultFan));

			record.addCalculateStepCouple(XueNiuCalType.HUAZHU.getValue(), getPlayerPosition(entry.getKey()), 0, resultFan, null, pairList);
		}
	}

	public boolean isSwitchEnd() {
		if (!switchCard) {
			return true;
		}
		return playerSwitchCards.size() == 4;
	}


	private int getFanCount(List<XueniuFanType> typeList) {
		XueniuFanType type = null;
		for (XueniuFanType fanType : typeList) {
			if (fanType.getId() < 16
					|| (fanType.getId() > XueniuFanType.GEN.getId() && fanType.getId() < XueniuFanType.END.getId())) {
				type = fanType;
				break;
			}
		}
		if (type == null) {
			return 0;
		}
		int fanCount = type.getFan();
		for (XueniuFanType fanType : typeList) {
			if (fanType == XueniuFanType.GEN) {
				fanCount <<= 1;
			}
		}
		return fanCount;
	}

	private Pair<Integer, List<XueniuFanType>> getMaxFanType(PlayerDeskInfo info, List<Integer> tingResult) {
		Pair<Integer, List<XueniuFanType>> maxType = null;
		for (int i = 0, count = tingResult.size(); i < count; i++) {
			List<Integer> cardList = new ArrayList<>(info.getHandCards());
			cardList.add(tingResult.get(i));
			List<Three<List<Integer>, List<Integer>, List<Integer>>> huResultList = MajongRule.getHuPaiResult(cardList);
			if (huResultList == null) {
				continue;
			}
			for (Three<List<Integer>, List<Integer>, List<Integer>> result : huResultList) {
				List<XueniuFanType> typeList = getXueniuFanType(result, info);
				int fanCount = getFanCount(typeList);
				if (maxType == null || maxType.getLeft() < fanCount) {
					maxType = new Pair<>(fanCount, typeList);
				}
			}
		}
		return maxType;
	}

	private List<XueniuFanType> getXueniuFanType(Three<List<Integer>, List<Integer>, List<Integer>> cardList, PlayerDeskInfo info) {
		List<Integer> chi = cardList.getA();
		List<Integer> ke = cardList.getB();
		List<Integer> jiang = cardList.getC();
		List<Integer> chiList = new ArrayList<>(info.getChiQueue());
		for (Integer value : chi) {
			chiList.add(value);
		}
		List<Integer> keList = new ArrayList<>(info.getKeQueue());
		for (Integer value : ke) {
			keList.add(value + GameConst.AN_MASK);
		}
		XueniuFanType type = XnStragetyManager.getInst().calFinalFanType(false, info, info.getGangQueue()
				, new LinkedList<>(keList), new LinkedList<>(chiList), jiang, extraFanList);
		if (type == null) {
			return null;
		}
		return getTypeListContainGen(info, type);
	}

	private List<XueniuFanType> getMaxXueniuFanType() {
		return null;
	}

	private int getFanCount(XueniuFanType type, List<XueniuFanType> typeList) {
		int fan = type.getFan();
		for (XueniuFanType fanType : typeList) {
			if (fanType == XueniuFanType.GEN) {
				fan <<= 1;
			}
		}
		return fan;
	}
	
	public boolean dealHuInfo() {
		if (huList.size() <= 0) { // 全部胡完后处理胡牌
			logger.error("{}|{} 當前並沒有點炮胡", getDeskId(), getGameId());
			return false;
		}
		Map<PlayerInfo, Integer> coinMap = new HashMap<>();
		int needCoin = 0;
		PlayerInfo failPlayer = null;
		for (HuInfo huInfo : huList) {
			failPlayer = huInfo.getFailPlayer();
			coinMap.put(huInfo.getPlayer(), huInfo.getCoin());
			needCoin += huInfo.getCoin();
		}
		logger.info("{}|{} 一炮{}餉  點炮玩家{} 需支付{}", getDeskId(), getGameId(), huList.size(), failPlayer.getPlayerId(), needCoin);
		// 不够支付
		float value = 1;
		int result = needCoin;
		if (failPlayer.getCoin() < needCoin) {
			value = failPlayer.getCoin() / (float)needCoin;
			result = failPlayer.getCoin();
		}
		logger.info("{}|{}  點炮玩家{} 實付支付{}", getDeskId(), getGameId(), failPlayer.getPlayerId(), result);
		failPlayer.updateCoin(result, false);
		for (HuInfo huInfo : huList) {
			List<Pair<Integer, Integer>> pairList = new ArrayList<>();
			int resultCoin = (int) (coinMap.get(huInfo.getPlayer()) * value);
			huInfo.getPlayer().updateCoin(resultCoin, true);
			logger.info("{}|{}  點炮胡牌玩家 {} 實際收穫{}", getDeskId(), getGameId(), huInfo.getPlayer().getPlayerId(), resultCoin);
			pairList.add(new Pair<>(getPlayerPosition(failPlayer), -resultCoin));
			pairList.add(new Pair<>(getPlayerPosition(huInfo.getPlayer()), resultCoin));
//			getPlayerDeskInfo(huInfo.getPlayer()).addHupaiCard(GameUtil.getRealHuValue(huInfo.getHuValue()));
			Xueniu.PBCalculate calResult = XueniuCreator.createPBCalculate(getPlayerPosition(huInfo.getPlayer()),
					huInfo.getXueNiuCalType().getValue(), pairList, huInfo.getHuValue(), huInfo.getResultFan(),
					huInfo.getTypeList());
			addCalculate(XueNiuCalType.HU.getValue(), huInfo.getTypeList(), calResult);
			notifyCalculate(calResult);
		}
		checkPlayerDefeat(failPlayer);
		if (huList.size() > 1) { // 一炮多响通知
			for (PlayerInfo player : player2InfoMap.keySet()) {
				player.write(ResponseCode.XUENIU_HU_TOGETHER, null);
			}
		}
		huList.clear();
		if (tran && getGangPosition() != null) {
			calHujiaozhuanyi(); // 非自摸 检测呼叫轉移
		}
		
		if (isWillEnd()) { // 非自摸 血戰模式胡3傢結束 檢測
			onAllCardOver(false);
			return true;
		}
		return false;
	}

	private void checkDajiaoResult(PlayerInfo player) {
		if (player.getCoin() <= 0){ // 錢不夠不會被查
			logger.error("{}|{} 玩家 {} 當前金幣為{} 無法被查大叫", getDeskId(), getGameId(), player.getPlayerId(), player.getCoin());
			return;
		}
		Map<PlayerInfo, Integer> player2coin = new HashMap<>();
		float needCoin = 0;
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isDefeat()) {
				continue;
			}
			if (entry.getKey() == player) {
				continue;
			}
			if (getPlayerDeskInfo(entry.getKey()).isHuPai()) {
				continue;
			}
			if (!MajongRule.checkTingpai(entry.getValue().getHandCards(), getIgnoreType(entry.getValue().getPosition()))) {
				continue;
			}
			List<Integer> tingResult = MajongRule.getTingpaiResult(entry.getValue().getHandCards());
			if (tingResult.size() < 1) {
				continue;
			}
			Pair<Integer, List<XueniuFanType>> maxFanType = getMaxFanType(entry.getValue(), tingResult);
			if (maxFanType == null) {
				continue;
			}
			int resultFan = Math.min(maxFanType.getLeft(), maxFan);
			needCoin += resultFan * getBaseScore();
			if (needCoin != 0) {
				player2coin.put(entry.getKey(), resultFan);
			} else {
				logger.error("{}|{} 玩家 {}  查大叫 手牌{} 番型{} 番数{}", getDeskId(), getGameId(), player.getPlayerId(),
						entry.getValue().getHandCards(), maxFanType.getRight().toString(), resultFan);
			}
		}
		if (needCoin == 0){
			return;
		}
		// 不够支付
		float value = 1;
		int payCoin = (int) needCoin;
		if (player.getCoin() < needCoin) {
			value = player.getCoin() / needCoin;
			payCoin = player.getCoin();
		}
		player.updateCoin(payCoin, false);
		logger.info("{}|{} 玩家 {} 被查大叫開始付錢 實付{}", getDeskId(), getGameId(), player.getPlayerId(), payCoin);
		for (Map.Entry<PlayerInfo, Integer> entry : player2coin.entrySet()) {
			List<Pair<Integer, Integer>> pairList = new ArrayList<>();
			int resultCoin = (int) (entry.getValue() * getBaseScore() * value);
			logger.info("{}|{} 玩家 {} 查大叫 實收{}", getDeskId(), getGameId(), entry.getKey().getPlayerId(), resultCoin);
			entry.getKey().updateCoin(resultCoin, true);
			pairList.add(new Pair<>(getPlayerPosition(player), resultCoin * -1));
			pairList.add(new Pair<>(getPlayerPosition(entry.getKey()), resultCoin));
			addCalculate(XueNiuCalType.DAJIAO.getValue(), null
					, XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.DAJIAO.getValue(), pairList, 0, entry.getValue()));

			record.addCalculateStepCouple(XueNiuCalType.DAJIAO.getValue(), getPlayerPosition(entry.getKey()), 0, entry.getValue(), null, pairList);
		}
	}


	private void checkDajiao() {
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
			if (entry.getValue().isDefeat()) {
				continue;
			}
			if (!MajongRule.checkTingpai(entry.getValue().getHandCards(), getIgnoreType(entry.getValue().getPosition()))) {
				checkDajiaoResult(entry.getKey());
			}
		}
	}


	private Map<PlayerInfo, Integer> allGainAndLose() {
		Map<PlayerInfo, Integer> result = new HashMap<>();
		Map<MJPosition, Integer> scoreHis = new HashMap<>();
		for (PlayerInfo player : player2InfoMap.keySet()) {
			int score = calOnePlayerResult(player);
			result.put(player, calOnePlayerResult(player));
			scoreHis.put(getPlayerPositionType(player), score);
		}
		scoreList.add(new Pair<>(startTime, scoreHis));
		return result;
	}

	private List<Xueniu.PBCalItem> getSelfRecordList(PlayerInfo player) {
		MJPosition position = getPlayerPositionType(player);
		List<Xueniu.PBCalItem> selfList = new ArrayList<>();
		for (Map.Entry<Integer, List<XNOneCalRecord>> entry : calculateMap.entrySet()) {
			for (XNOneCalRecord record : entry.getValue()) {
				if (record.containPosition(position.getValue())) {
					selfList.add(XueniuCreator.createPBcalItem(entry.getKey(), record, position.getValue()));
				}
			}
		}
		return selfList;
	}

    private int calOnePlayerResult(PlayerInfo player) {
        int total = 0;
        MJPosition position = getPlayerPositionType(player);
        for (List<XNOneCalRecord> recordList : calculateMap.values()) {
            for (XNOneCalRecord record : recordList) {
                total += record.getPositionGainLose(position.getValue());
            }
        }
        return total;
    }

	private void sendTotalCalculate(PlayerInfo player, Map<PlayerInfo, Integer> gainLose) {

		Xueniu.PBXueNiuTotalCalculate.Builder builder = Xueniu.PBXueNiuTotalCalculate.newBuilder();
		MJPosition position = getPlayerPositionType(player);
		List<Xueniu.PBCalItem> selfList = getSelfRecordList(player);
		int totalCoin = 0;
		for (Xueniu.PBCalItem item : selfList) {
			totalCoin += item.getCoin();
		}

		int taxRate = 0;
		if(isPersonal()){
			taxRate  = personalConfRoom == null?2:personalConfRoom.getTax_rate();
		} else {
			taxRate = coupleRoom.getTax_rate();
		}
		//如果大于0 就扣税
		if (totalCoin > 0) {
			int taxCount = totalCoin * taxRate % 100 == 0 ? totalCoin * taxRate / 100 : totalCoin * taxRate / 100 + 1;
			flowMoney += totalCoin;
			taxMoney += taxCount;
			player.updateCoin(taxCount, false);
			totalCoin -= taxCount;
			detail.addOneRecord(player.getPlayerId(), getPlayerPosition(player), 1, taxCount, player.getChannel_id(), player.getPackage_id(), player.getDevice(), player.getCoin()-totalCoin, player.getCoin(), player.getIp());
		} else {
			detail.addOneRecord(player.getPlayerId(), getPlayerPosition(player), 0, 0, player.getChannel_id(), player.getPackage_id(), player.getDevice(), player.getCoin()-totalCoin, player.getCoin(), player.getIp());
		}
		// 屏蔽掉私房的房主抽水
//		if (player == getRoomCreator()) {
//			int resultCoin = taxMoney * 20 / 100;
//			player.updateCoin(resultCoin, true);
//			List<Pair<Integer, Integer>> pairList = new ArrayList<>();
//			pairList.add(new Pair<>(getPlayerPosition(player), resultCoin));
//			addCalculate(XueNiuCalType.ROOM_CHARGE.getValue(), null
//					, XueniuCreator.createPBCalculate(getPlayerPosition(player), XueNiuCalType.ROOM_CHARGE.getValue(), pairList, 0, 0));
//		}
		for (Xueniu.PBCalItem item : getSelfRecordList(player)) {
			builder.addSelfResult(item);
		}
		builder.setTotalCoin(totalCoin);
		for (Map.Entry<PlayerInfo, Integer> entry : gainLose.entrySet()) {
			MJPosition playerPosition = getPlayerPositionType(entry.getKey());
			if (playerPosition == position) {
				continue;
			}
			int resultValue = entry.getValue() > 0 ?
					entry.getValue() * (100 - taxRate) / 100 : entry.getValue();
			builder.addOtherResult(XueniuCreator.createPBOtherItem(playerPosition.getValue(), entry.getKey().getName(), entry.getKey().getIcon(), resultValue));
		}
		for (Map.Entry<MJPosition, PlayerDeskInfo> entry : postion2InfoMap.entrySet()) {
			if (entry.getKey() == position) {
				continue;
			}
			builder.addOtherCards(XueniuCreator.createPBOtherCards(entry.getValue()));
		}
		builder.setGameNo(String.valueOf(getGameId()));
		player.write(ResponseCode.XUENIU_GAME_END, builder.build());

		calculateResMap.put(player.getPlayerId(),builder.build());

		LogUtil.logLogger(logger, "SCMJDesk_胡牌2", builder.build(), "" + getGameId(), "" + getDeskId(), "" + getGameType(), "" + player.getPlayerId());

		Pair<Integer, Integer> cur_game_type = getCurGameType();
		if (0 <= builder.getTotalCoin()) {
			player.write(RequestCode.LOG_MONEY.getValue()
					, LogHelper.logGainMoney(player.getPlayerId()
							, cur_game_type.getLeft(), getGameType().getValue(), builder.getTotalCoin(), player.getCoin()-builder.getTotalCoin(), player.getCoin(), player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));

			TaxRecordUtil.sendGamePlayerStatus(player, builder.getTotalCoin());
		} else {
			player.write(RequestCode.LOG_MONEY.getValue()
					, LogHelper.logLoseMoney(player.getPlayerId()
							, cur_game_type.getRight(), getGameType().getValue(), -builder.getTotalCoin(), player.getCoin()-builder.getTotalCoin(), player.getCoin(), player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));

			TaxRecordUtil.sendGamePlayerStatus(player, builder.getTotalCoin());
		}
	}


	public Map<Integer, List<XNOneCalRecord>> getCalculateMap() {
		return calculateMap;
	}

	@Override
	public void tingpai(PlayerInfo player, CoupleMajiang.PBTingReq request) {
//		if (!((gameState2 == MajongState2.HU ||gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.CHI || gameState2 == MajongState2.PENG))) {
//			logger.info("当前状态{}不能听牌", gameState2);
//			return;
//		}
		stopAutoChuPaiBeginFuture();
		logger.info("{}|{} 玩家{} 当前状态{}听牌",getDeskId(),getGameId(),player.getPlayerId(), gameState2);

		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		List<Pair<Integer, List<Pair<Integer, Integer>>>> result = new ArrayList<>();
		for (CoupleMajiang.PBTingItem tingItem : request.getTingInfoList()) {
			Pair<Integer, List<Pair<Integer, Integer>>> pair = new Pair<>();
			int dropValue = tingItem.getDropCard();
			pair.setLeft(dropValue);
			List<Pair<Integer, Integer>> fanList = new ArrayList<>();
			pair.setRight(fanList);
			result.add(pair);
			List<Integer> handCopy = info.getHandCardsCopy();
			handCopy.remove((Integer) dropValue);
			List<CoupleMajiang.PBHuReq> huList = tingItem.getHuList();
			for (CoupleMajiang.PBHuReq hu : huList) {
				List<Integer> chiList = new ArrayList<>(hu.getChiListList());
				List<Integer> keList = new ArrayList<>(hu.getKeListList());
				List<Integer> jiangList = new ArrayList<>(hu.getJiangValueList());
				List<Integer> allCards = new ArrayList<>();
				GameUtil.addChiToCardList(allCards, chiList);
				GameUtil.addKeToCardList(allCards, keList);
				GameUtil.addJiangToCardList(allCards, jiangList);
				handCopy.forEach(e -> allCards.remove(e));
				if (allCards.size() != 1) {
					logger.debug(" the card count is error {} ", allCards.size());
					continue;
				}
				int huValue = allCards.get(0);
				chiList.addAll(info.getChiQueue());
				List<Integer> resultKeList = new ArrayList<>(info.getKeQueue());
				for (Integer value : keList) {
					resultKeList.add(GameConst.AN_MASK + value);
				}
				keList.addAll(info.getKeQueue());
				XueniuFanType type = XnStragetyManager.getInst().calFinalFanType(true, info, info.getGangQueue()
						, new LinkedList<>(resultKeList), new LinkedList<>(chiList), jiangList, extraFanList);
				if (type == null) {
					fanList.add(new Pair<>(huValue, 0));
				} else {
					fanList.add(new Pair<>(huValue, type.getFan()));
				}
			}
		}
		logger.debug(" ting request the result is {} result size is {} and the request is {}", result, result.size(), request);
		info.addTingResult(result);
		info.setHuCache(request);
		player.write(ResponseCode.COUPLE_ON_TING, CoupleCreator.createPBTingFanRes(result));
	}

	@Override
	protected void selectMenfeng() {
		throw new IllegalArgumentException("un support operation of select men feng ");
	}

	@Override
	public PlayerDeskInfo getNexPlayerDeskInfo(PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return null;
		}
		return postion2InfoMap.get(info.getPosition().nextPosition());
	}

	@Override
	public MJPosition nextPosition() {
		return currentTurn.nextPosition();
	}

	@Override
	protected void checkHuapai() {
		// do nothing
	}

	@Override
	protected void clearDeskInfo() {
		flowMoney = 0;
		taxMoney = 0;
		playerSwitchCards.clear();
		playerChoseType.clear();
		calculateMap.clear();
		defeatList.clear();
		alreadyGangCal.clear();
		lastCalculate = null;
		if (operationFuture != null) {
			operationFuture.cancel(true);
			operationFuture = null;
		}

		calculateResMap.clear();
	}

	@Override
	protected MajongType getIgnoreType(MJPosition position) {
		return playerChoseType.get(position);
	}

	@Override
	protected boolean canDropCard(PlayerInfo player, int card) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return false;
		}
		if (info.isHuPai() && card != info.getLastHandCard()) {
			return false;
		}
		return true;
	}

	@Override
	public void doTing(PlayerInfo player, int value) {
		//do noting
	}


	private boolean isXuezhanHupai(MJPosition position) {
		return getGameType() == GameType.XUEZHAN && postion2InfoMap.get(position).isHuPai();
	}

	@Override
	protected void dealOneCard(MJPosition position) {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		if (stop) { // 如果现在是暂停阶段 不发牌
			stopPosition = position;
			return;
		}
		// 杠牌,胡牌，出牌 之后可以摸牌
		if(!((gameState2 == MajongState2.GUO ||gameState2 == MajongState2.CHU || gameState2== MajongState2.GANG|| isGangIng || gameState2 == MajongState2.HU))){
			logger.error("{}|{} 当前状态 {} 不对,不能摸牌",getDeskId(),getGameId(), gameState2);
			return;
		}
        if (currentTurn != position) {
            logger.info("{}|{} 沒有輪到該位置{}摸牌，當前操作位置為{}", getDeskId(),getGameId(), position, currentTurn);
            return;
        }

		// 可胡牌 点过 也要结算胡牌
		if (isHuIng()) {
			setHuIng(false);
			if (dealHuInfo()) {
				return;
			}
		}
        
		// 补杠后需要摸牌出牌 为了确保 呼叫转移的杠状态  自己摸牌不清除自己的 杠状态
		setOtherGangFalse(postion2InfoMap.get((position)));
		
		setHuIng(false);
        dianPaoPos = null;
        gangWating = null;
        // 可胡牌 点过 也要结算胡牌
        
        if (postion2InfoMap.get(position) == null || isXuezhanHupai(position) || postion2InfoMap.get(position).isDefeat()) {
            logger.info("{}|{} 当前位置{}玩家胡牌或者认输了，换下一个", getDeskId(),getGameId(), position);
            setCurrentTurn(position.nextPosition());
            currOperationTurn = currentTurn;
            dealOneCard(currentTurn);
        } else {
//			setCurrentTurn(position);
//            currOperationTurn = position;

//            logger.info("当前轮到位置{}", position);
//            logger.info("当前处理牌的位置为{}", position);
            if (paiPool.size() == 0) {
				onAllCardOver(false);
				return;
			}
			operationList.clear();
			PlayerDeskInfo info = postion2InfoMap.get(position);
			if (info.getTotalCardCotainGangChike() == 14) {
				return;
			}
			int pai = paiPool.poll();
			info.addHandCard(pai);
			info.addHandTimes();
			msgHelper.notifyMoPai(position, Arrays.asList(pai), pai);

			setGameState2(MajongState2.MO);

			thinkAftMo(0);
		}
	}

	@Override
	protected int getEnterTimes() {
		return this.enterTimes;
	}

	public abstract GameType getGameType();
}
