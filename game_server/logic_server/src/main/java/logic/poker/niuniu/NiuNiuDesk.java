package logic.poker.niuniu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.LogicActorManager;
import common.LogHelper;
import config.CoupleRoomInfoProvider;
import config.bean.ConfMatchCard;
import config.bean.ConfNiu;
import config.provider.ConfNiuProvider;
import data.MoneySubAction;
import database.DBUtil;
import database.DataQueryResult;
import define.constant.MessageConst;
import logic.DeskMgr;
import logic.define.GameType;
import logic.majiong.GameConst;
import logic.majiong.PlayerInfo;
import logic.poker.PokerCard;
import logic.poker.PokerDesk;
import logic.poker.PokerMatchCardUtil;
import logic.poker.PokerUtil;
import logic.record.TaxRecordUtil;
import logic.record.detail.NiuniuDetail;
import logic.record.detail.OneNiuniuRecord;
import proto.Common.PBPair;
import proto.Niuniu;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.ASObject;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;

/**
 * Created by Administrator on 2016/12/13.
 */
public class NiuNiuDesk extends PokerDesk {
	private static final Logger logger = LoggerFactory.getLogger(NiuNiuDesk.class);
	private Queue<PokerCard> paiPool = null;
	private List<PokerCard> zhuangCard = new ArrayList<>();
	private Map<XianPosition, List<PokerCard>> xianCardMap = new HashMap<>();
	private Map<PlayerInfo, Map<XianPosition, Integer>> playerChipInfo = new HashMap<>();                        //下注信息
	private Map<Integer, PlayerInfo> playerInfoMap = new HashMap<>();
	private Map<XianPosition, Integer> totalChipInfo = new HashMap<>();
	private Queue<PlayerInfo> zhuangList = new LinkedList<>();
	private PlayerInfo zhuangPlayer = null;                                                    //庄家相关的信息
	private int bannerRounds = 0;
	private boolean bannerChange = false;
	private NiuNiuState state = NiuNiuState.WAITING;
	private int chipTime = 0;
	private int calTime = 0;
	private ScheduledFuture<?> deskFuture = null;
	private Map<PlayerInfo, Map<XianPosition, Integer>> playerCalResultMap = new HashMap<>();                        //玩家最后结算的一些信息
	private Map<XianPosition, Integer> bannerCalResult = new HashMap<>();
	private Pair<Pair<PokerCard, PokerCard>, NiuResult> zhuangResult = null;
	private Map<XianPosition, Pair<Pair<PokerCard, PokerCard>, NiuResult>> xianResultMap = new HashMap<>();
	private Stack<Map<XianPosition, Integer>> historyRecordList = new Stack<>();
	private Map<XianPosition, Map<Integer, Integer>> chipInfoRecord = new HashMap<>();
	private Map<PlayerInfo, Map<XianPosition, Map<Integer, Integer>>> playerChipInfoRecord = new HashMap<>();
	private List<PlayerInfo> leavePlayerList = new ArrayList<>();
	private List<PlayerInfo> logoutPlayerList = new ArrayList<>();
	private List<Integer> bannerStatistics = new ArrayList<>();
	private long gameId;
	private int flowMoney;
	private int taxMoney;
	private int startTime;
	private NiuniuDetail detail;
	private float zhuangPayValue = 1;             // 庄家赔钱系数    正常情况下为1  如果庄家钱不够赔系数会小于1
	
	public NiuNiuDesk(int roomId,int deskId) {
		super(deskId, roomId);
	}

	public void playerEnterDesk(PlayerInfo player) {
		if (!playerChipInfo.containsKey(player)) {
			playerChipInfo.put(player, new HashMap<>());
			playerInfoMap.put(player.getPlayerId(), player);
		}
		leavePlayerList.remove(player);
		logoutPlayerList.remove(player);
		if (playerChipInfo.size() == 1 && deskFuture == null) {
			startChipTime();
			beginDeskFuture();
		}
		sendDeskInfoToPlayer(player);
	}

	private void sendDeskInfoToPlayer(PlayerInfo player) {
		switch (state) {
			case WAITING:
				break;
			case CHIP_TIME:
				sendRoomChipInfoToPlayer(player);
				break;
			case CAL_RESULT:
				sendRoomCalResultToPlayer(player);
				break;
			default:
				break;
		}
	}

	@Override
	protected Map<Integer, Integer> getAllPlayerMoney() {
		return null;
	}

	private void sendRoomCalResultToPlayer(PlayerInfo player) {
		Niuniu.PBNiuNiuResult.Builder builder = Niuniu.PBNiuNiuResult.newBuilder();
		builder.setRemainTime(this.calTime);
		builder.setBannerInfo(createPBZhuangInfo(player));
		builder.setZhuangInfo(createPBNiuNiuResultItem(zhuangCard, zhuangResult.getLeft(), zhuangResult.getRight()));
		if (null == zhuangPlayer) {
			builder.setZhuangCoinOrig(0+"");
		} else {
			builder.setZhuangCoinOrig(zhuangPlayer.getOld_coin()+"");
		}
		for (int i = XianPosition.ONE.getValue(); i < XianPosition.FIVE.getValue(); i++) {
			List<PokerCard> cardList = xianCardMap.get(XianPosition.getByValue(i));
			Pair<PokerCard, PokerCard> twoCards = xianResultMap.get(XianPosition.getByValue(i)).getLeft();
			NiuResult result = xianResultMap.get(XianPosition.getByValue(i)).getRight();
			builder.addXianInfo(createPBNiuNiuResultItem(cardList, twoCards, result));
		}
		Map<XianPosition, Integer> selfResult = playerCalResultMap.get(player);
		for (int i = XianPosition.ONE.getValue(); i <= XianPosition.FIVE.getValue(); i++) {
			Integer resultValue = selfResult == null ? 0 : selfResult.get(XianPosition.getByValue(i));
			if (resultValue == null) {
				resultValue = 0;
			}
			Integer bannerResultCoin = bannerCalResult.get(XianPosition.getByValue(i));
			if (bannerResultCoin == null) {
				bannerResultCoin = 0;
			}
			builder.addCalResult(CommonCreator.createPBTriple(getSelfChip(player, i), resultValue, bannerResultCoin));
		}
		builder.setSelfChip(CommonCreator.createPBPairListList(getSelfChipInfoList(player)));
		builder.setTotalButSelf(CommonCreator.createPBPairListList(getTotalChipRemoveSelfList(player)));
		builder.setGameNo(String.valueOf(gameId));
		bannerStatistics.forEach(builder::addBannerStatistics);
		if (null != player) {
			player.write(ResponseCode.NIUNIU_RESULT, builder.build());
		}
	}

	private Niuniu.PBNiuNiuResultItem createPBNiuNiuResultItem(List<PokerCard> cardList, Pair<PokerCard, PokerCard> twoCards, NiuResult result) {
		Niuniu.PBNiuNiuResultItem.Builder builder = Niuniu.PBNiuNiuResultItem.newBuilder();
		for (PokerCard pokerCard : cardList) {
			builder.addPokerValue(pokerCard.getKey());
		}
		if (twoCards != null) {
			builder.setNiuCard(CommonCreator.createPBPair(twoCards.getLeft().getKey(), twoCards.getRight().getKey()));
		}
		builder.setNiuResult(result.getValue());
		return builder.build();
	}

	private void sendRoomChipInfoToPlayer(PlayerInfo player) {
		Niuniu.PBNiuNiuRoomInfo.Builder builder = Niuniu.PBNiuNiuRoomInfo.newBuilder();
		builder.setRemainTime(chipTime);
		builder.setSelfChip(CommonCreator.createPBPairListList(getSelfChipInfoList(player)));
		builder.setTotalButSelf(CommonCreator.createPBPairListList(getTotalChipRemoveSelfList(player)));
		builder.setZhuangInfo(createPBZhuangInfo(player));
		bannerStatistics.forEach(e -> builder.addBannerStatistics(e));
		player.write(ResponseCode.NIUNIU_CHIP_INFO, builder.build());
	}

	private Niuniu.PBZhuangInfo createPBZhuangInfo(PlayerInfo player) {
		Niuniu.PBZhuangInfo.Builder builder = Niuniu.PBZhuangInfo.newBuilder();
		if (zhuangPlayer == null) {
			builder.setZhuangAccount("system");
			builder.setZhuangIcon("");
			builder.setZhuangCoin(0+"");
		} else {
			String phoneNum = "";
			if (phoneNum == null || phoneNum.equals("")) {
				phoneNum = String.valueOf(zhuangPlayer.getPlayerId());
			}
			builder.setZhuangAccount(phoneNum);
			builder.setZhuangCoin(zhuangPlayer.getCoin()+"");
			builder.setZhuangIcon(zhuangPlayer.getIcon());
		}
		builder.setRobZhuangTotal(zhuangList.size());
		builder.setSelfPosition(getSelfRobZhuangPosition(player));
		builder.setBannerRounds(bannerRounds);
		return builder.build();
	}

	private int getSelfRobZhuangPosition(PlayerInfo player) {
		Iterator<PlayerInfo> iter = zhuangList.iterator();
		int position = 0;
		while (iter.hasNext()) {
			position++;
			if (player == iter.next()) {
				return position;
			}
		}
		return 0;
	}
	
	/** 庄家金币下注比例 */
	private int getTotalChipLimit() {
		if (zhuangPlayer == null) {
			return getConfNiu().getBankCoin() / getConfNiu().getNiuniu_chip_limit();
		} else {
			return zhuangPlayer.getCoin() / getConfNiu().getNiuniu_chip_limit();
		}
	}

	/** 玩家还能否下注 */
	private boolean playerCanChip(PlayerInfo player, ChipType type) {
		int chipValue = getCurrentPlayerChip(player);
		long playerTotal = player.getCoin();
		return (chipValue + type.getValue()) <= playerTotal / getConfNiu().getNiuniu_chip_limit();
	}
	
	private boolean playerCanRenewad(PlayerInfo player, List<ChipType> typeList) {
		int chipValue = getCurrentPlayerChip(player);
		long playerTotal = player.getCoin();
		int typeValue = 0;
		for (ChipType type : typeList) {
			typeValue += type.getValue();
		}

		return (chipValue + typeValue) <= playerTotal / getConfNiu().getNiuniu_chip_limit();
	}

	private int getSelfRemainChipCount(PlayerInfo player) {
		int chipValue = getCurrentPlayerChip(player);
		long playerTotal = player.getCoin();
		return ((int) playerTotal / getConfNiu().getNiuniu_chip_limit() - chipValue);
	}

	private int getTotalRemainChipCount() {
		return (getTotalChipLimit() - getCurrentChipTotal());
	}
	
	/** 桌上还能否下注 */
	private boolean canChip(ChipType type) {
		return type.getValue() + getCurrentChipTotal() <= getTotalChipLimit();
	}
	
	private boolean canRenewad(List<ChipType> typeList) {
		int typeValue = 0;
		for (ChipType type : typeList) {
			typeValue += type.getValue();
		}

		return typeValue + getCurrentChipTotal() <= getTotalChipLimit();
	}

	/** 当前桌上金币下注总和  */
	private int getCurrentChipTotal() {
		int totalCount = 0;
		for (Map<Integer, Integer> chipInfo : chipInfoRecord.values()) {
			for (Map.Entry<Integer, Integer> entry : chipInfo.entrySet()) {
				ChipType type = ChipType.getTypeById(entry.getKey());
				if (type == null) {
					continue;
				}
				totalCount += type.getValue() * entry.getValue();
			}
		}
		return totalCount;
	}

	/** 玩家当前下注金币总和 */
	private int getCurrentPlayerChip(PlayerInfo player) {
		Map<XianPosition, Integer> chip = playerChipInfo.get(player);
		if (chip == null) {
			return 0;
		}
		int total = 0;
		for (Integer value : chip.values()) {
			total += value;
		}
		return total;
	}

	public void getNiuniuHistoryRecord(PlayerInfo player) {
		Niuniu.PBNiuNiuHisRecord.Builder builder = Niuniu.PBNiuNiuHisRecord.newBuilder();
		Iterator<Map<XianPosition, Integer>> iter = historyRecordList.iterator();
		int count = 0;
		int oneCount = 0;
		int twoCount = 0;
		int threeCount = 0;
		int fourCount = 0;
		while (iter.hasNext()) {
			Map<XianPosition, Integer> record = iter.next();
			for (int i = XianPosition.ONE.getValue(); i < XianPosition.FIVE.getValue(); i++) {
				XianPosition position = XianPosition.getByValue(i);
				if (position == null) {
					continue;
				}
				boolean win = record.get(position) > 0;
				if (win) {
					switch (position) {
						case ONE:
							oneCount++;
							break;
						case TWO:
							twoCount++;
							break;
						case THREE:
							threeCount++;
							break;
						case FOUR:
							fourCount++;
							break;
						default:
							break;
					}
				}
			}
		}
		int hisSize = historyRecordList.size();
		if (hisSize >= 18) {
			historyRecordList.subList(hisSize - 18, hisSize).forEach(e -> builder.addRecord(createPBOneRecord(e)));
		} else {
			historyRecordList.subList(0, hisSize).forEach(e -> builder.addRecord(createPBOneRecord(e)));
		}
		builder.setTotalGameTimes(hisSize);
		builder.addColorWinTimes(oneCount);
		builder.addColorWinTimes(twoCount);
		builder.addColorWinTimes(threeCount);
		builder.addColorWinTimes(fourCount);
		player.write(ResponseCode.NIUNIU_HIS_RECORD, builder.build());
	}

	private Niuniu.PBOneRecord createPBOneRecord(Map<XianPosition, Integer> record) {
		Niuniu.PBOneRecord.Builder subBuilder = Niuniu.PBOneRecord.newBuilder();
		for (int i = XianPosition.ONE.getValue(); i < XianPosition.FIVE.getValue(); i++) {
			boolean win = record.get(XianPosition.getByValue(i)) > 0;
			subBuilder.addRecord(win);
		}
		return subBuilder.build();
	}

	/** 四个位置下注金币数 */
	private int getSelfChip(PlayerInfo player, int position) {
		Map<XianPosition, Integer> oneChipInfo = playerChipInfo.get(player);
		if (oneChipInfo == null) {
			return 0;
		}
		Integer result = oneChipInfo.get(XianPosition.getByValue(position));
		if (result == null) {
			return 0;
		}
		return result;
	}

	private int getTotalChip(int position) {
		Integer result = totalChipInfo.get(XianPosition.getByValue(position));
		if (result == null) {
			return 0;
		}
		return result;
	}

	private List<Map<Integer, Integer>> getSelfChipInfoList(PlayerInfo player) {
		List<Map<Integer, Integer>> result = new ArrayList<>();
		for (int i = XianPosition.ONE.getValue(); i < XianPosition.FIVE.getValue(); i++) {
			Map<Integer, Integer> oneRecord = new HashMap<>();
			Map<Integer, Integer> selfRecord = playerChipInfoRecord.get(player) == null ?
					null : playerChipInfoRecord.get(player).get(XianPosition.getByValue(i));
			if (selfRecord != null) {
				for (Map.Entry<Integer, Integer> entry : selfRecord.entrySet()) {
					Integer selfValue = selfRecord.get(entry.getKey());
					if (selfValue == null) {
						selfValue = 0;
					}
					oneRecord.put(entry.getKey(), selfValue);
				}
			}
			result.add(oneRecord);
		}
		return result;
	}

	private List<Map<Integer, Integer>> getTotalChipRemoveSelfList(PlayerInfo player) {
		List<Map<Integer, Integer>> result = new ArrayList<>();
		for (int i = XianPosition.ONE.getValue(); i < XianPosition.FIVE.getValue(); i++) {
			Map<Integer, Integer> oneRecord = new HashMap<>();
			Map<Integer, Integer> totalRecord = chipInfoRecord.get(XianPosition.getByValue(i));
			if (totalRecord == null) {
				result.add(oneRecord);
				continue;
			}
			Map<Integer, Integer> selfRecord = playerChipInfoRecord.get(player) == null ?
					null : playerChipInfoRecord.get(player).get(XianPosition.getByValue(i));
			if (selfRecord == null) {
				oneRecord.putAll(totalRecord);
			} else {
				for (Map.Entry<Integer, Integer> entry : totalRecord.entrySet()) {
					Integer selfValue = selfRecord.get(entry.getKey());
					if (selfValue == null) {
						selfValue = 0;
					}
					oneRecord.put(entry.getKey(), entry.getValue() - selfValue);
				}
			}
			result.add(oneRecord);
		}
		return result;
	}

	private void stopOperationFuture(){
		if (deskFuture != null) {
			deskFuture.cancel(true);
			deskFuture = null;
		}
	}
	
	private void beginDeskFuture() {
		stopOperationFuture();
		deskFuture = LogicActorManager.getTimer().register(1000, 1000, () -> onUpdate(), LogicActorManager.getDeskActor(getDeskId()), "niuniu_update");
	}

	private void onUpdate() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		switch (state) {
			case WAITING:
				break;
			case CHIP_TIME:
				onChipUpdate();
				break;
			case CAL_RESULT:
				onCalUpdate();
				break;
			default:
				break;
		}
	}

	@Override
	public void playerLeave(PlayerInfo player) {
		if (player == null) {
			logger.error(" player is null when player leave ");
			return;
		}
		if ((player == zhuangPlayer)) {
			bannerChange = true;
			if (!leavePlayerList.contains(player)) {
				leavePlayerList.add(player);
			}
		} else {
			if (zhuangList.contains(player)) {
				zhuangList.remove(player);
				onZhuangInfoChange();
			}
			Map<XianPosition, Integer> playerChip = playerChipInfo.get(player);
			if (playerChip == null || playerChip.size() == 0) {
				playerInfoMap.remove(player.getPlayerId());
				playerChipInfo.remove(player);
				DeskMgr.getInst().removePlayer(player,false);
//				LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, player.getPlayerId()));
			} else {
				if (!leavePlayerList.contains(player)) {
					leavePlayerList.add(player);
				}
			}
		}
	}

	@Override
	public void playerLogout(PlayerInfo player) {
		playerLeave(player);
	}

	private boolean isNotInRoom(PlayerInfo player) {
		if (leavePlayerList.contains(player) || logoutPlayerList.contains(player)) {
			return true;
		}
		return false;
	}

	private void onCalUpdate() {
		this.calTime--;
		if (this.calTime == 0) {
			startChipTime();
		}
	}


	private void startChipTime() {
		this.gameId = geneGameNo();
		this.detail = new NiuniuDetail(GameType.NIUNIU.getValue(), zhuangPlayer == null ? "system" : zhuangPlayer.getName());
		this.startTime = MiscUtil.getCurrentSeconds();
		state = NiuNiuState.CHIP_TIME;
		chipTime = GameConst.NIUNIU_CHIP_TIME;
		dealWithLeaveAndLogoutPlayer();                            //这个位置处理离线跟登出的玩家
		clearOneGame();
		validBannerInfo();
		for (PlayerInfo player : playerChipInfo.keySet()) {
			if (isNotInRoom(player)) {
				continue;
			}
			sendDeskInfoToPlayer(player);
		}
		totalChipInfo.clear();
	}

	private void onChipUpdate() {
		this.chipTime--;
		if (this.chipTime == 0) {
			state = NiuNiuState.CAL_RESULT;
			this.calTime = GameConst.NIU_NIU_CAL_TIME;
			startNewGame();
		}
		for (PlayerInfo player : playerChipInfo.keySet()) {
			player.write(ResponseCode.NIUNIU_CHIP_UPDATE, CommonCreator.createPBPairListList(getTotalChipRemoveSelfList(player)));
		}
	}

	private void validBannerInfo() {
		if (zhuangPlayer == null) {
			zhuangPlayer = zhuangList.poll();
			bannerRounds = 1;
			bannerChange = false;
		} else {
			if (bannerChange) {
				nextBanner();
			} else if (bannerRounds == 10) {
				zhuangPlayer.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.BANNER_ROUNDS_10));
				nextBanner();
			} else if (zhuangPlayer.getCoin() < getConfNiu().getBannerLimit()) {
				zhuangPlayer.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.BANNER_COIN_NOT_ENOUGH));
				nextBanner();
			} else {
				bannerRounds++;
			}
		}
	}

	private void nextBanner() {
		if (zhuangList.size() == 0) {
			zhuangPlayer = null;
		} else {
			zhuangPlayer = zhuangList.poll();
			bannerChange = false;
			bannerRounds = 1;
		}
		bannerStatistics.clear();
		onZhuangInfoChange();
	}

	private void startNewGame() {
		paiPool = PokerUtil.mixAllCard(GameType.NIUNIU);                //洗牌
		if(zhuangPlayer != null){//系统坐庄不配牌
			ConfMatchCard confMatchNiuCard = CoupleRoomInfoProvider.getInst().getConfMatchCard(getConfId());
			if(confMatchNiuCard != null && confMatchNiuCard.isOpen()){
				logger.error("牛牛{}开启配牌",getConfId());
				paiPool = PokerMatchCardUtil.matchNiuCard(gameId, paiPool, confMatchNiuCard, 5);
			}
		}
		for (int i = 0; i < 5; i++) {
			zhuangCard.add(paiPool.poll());
		}
		logger.info("{}|庄牌{}", gameId, zhuangCard.toString());
		for (int i = 0; i < 4; i++) {
			List<PokerCard> xianCard = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				xianCard.add(paiPool.poll());
			}
			logger.info("{}|闲{}牌{}", gameId, i + 1, xianCard.toString());
			xianCardMap.put(XianPosition.getByValue(i + 1), xianCard);
		}
		//notify  deal card
		calGameResult();
		for (PlayerInfo player : playerChipInfo.keySet()) {
			if (isNotInRoom(player)) {
				continue;
			}
			sendDeskInfoToPlayer(player);
		}
	}

	private ConfNiu getConfNiu(){
		return ConfNiuProvider.getInst().getConfNiu(getConfId());
	}
	
	/** 系统坐庄时赢是否赢 */
	private boolean isBannerWin() {
		if (zhuangPlayer != null) {
			return false;
		}
		logger.error("牛牛{}庄家通杀概率{}",getConfId(), getConfNiu().getNiuniuWinRate());
		return Randomizer.nextInt(100) < getConfNiu().getNiuniuWinRate();
	}
	
	/** 获取牛倍数 */
	private int getNiuTimes(NiuResult niuResult){
		Map<Integer,Integer> niuMap = getConfNiu().getNiuResultMap();
		Integer times = 1; 
		if(niuMap != null){
			times = niuMap.get(niuResult.getValue());
		}
		if(times == null){
			times = 1;
			logger.error("牛牛{}倍数未配置啊。。,默认一倍了啊",getConfId());
		}
		return times;
	}

	/** 算牌*/
	public void calGameResult() {
		this.zhuangResult = PokerUtil.calNiuResult(zhuangCard);
		logger.info("{}|庄家牌型{}", gameId, zhuangResult.getRight().toString());
		Pair<NiuResult, List<PokerCard>> zhuangCardInfo = new Pair<>(zhuangResult.getRight(), zhuangCard);
		//系统坐庄时赢是否赢
		if (isBannerWin()) {
			logger.info("{}无庄家时，随机概率到庄家一定要赢，开始系统换牌。",getDeskId());
			//自动换牌让庄家赢
			for (Map.Entry<XianPosition, List<PokerCard>> entry : xianCardMap.entrySet()) {
				Pair<Pair<PokerCard, PokerCard>, NiuResult> xianResult = PokerUtil.calNiuResult(entry.getValue());
				Pair<NiuResult, List<PokerCard>> xianCardInfo = new Pair<>(xianResult.getRight(), entry.getValue());
				// 如果闲家比庄家大
				if (PokerUtil.isOneGtTwo(xianCardInfo, zhuangCardInfo)) {
					Pair<Pair<PokerCard, PokerCard>, NiuResult> temp = zhuangResult;
					zhuangResult = xianResult;
					this.xianResultMap.put(entry.getKey(), temp);
					List<PokerCard> tempCards = this.zhuangCard;
					this.zhuangCard = xianCardMap.get(entry.getKey());
					xianCardMap.put(entry.getKey(), tempCards);
					zhuangCardInfo = new Pair<>(zhuangResult.getRight(), zhuangCard);
				} else {
					this.xianResultMap.put(entry.getKey(), xianResult);
				}
			}
			logger.info("系统换牌之后,{}|庄家牌型{}", gameId, zhuangResult.getRight().toString());
		}
		//闲家输赢位置-牛倍数
		Map<XianPosition, Integer> winResult = new HashMap<>();
		for (Map.Entry<XianPosition, List<PokerCard>> entry : xianCardMap.entrySet()) {
			Pair<Pair<PokerCard, PokerCard>, NiuResult> xianResult = PokerUtil.calNiuResult(entry.getValue());
			logger.info("{}|闲家{}牌型{}", gameId, entry.getKey().getValue(), xianResult.getRight().toString());
			Pair<NiuResult, List<PokerCard>> xianCardInfo = new Pair<>(xianResult.getRight(), entry.getValue());
			this.xianResultMap.put(entry.getKey(), xianResult);
			if (PokerUtil.isOneGtTwo(zhuangCardInfo, xianCardInfo)) {
				winResult.put(entry.getKey(), -getNiuTimes(zhuangResult.getRight()));
				detail.addXianMatch(entry.getKey(),entry.getValue(), xianResult.getRight().getValue(), 0);
			} else {
				winResult.put(entry.getKey(), getNiuTimes(xianResult.getRight()));
				detail.addXianMatch(entry.getKey(),entry.getValue(), xianResult.getRight().getValue(), 1);
			}
		}
		historyRecordList.push(winResult);
		calPlayerChip(winResult);
		int joinPlayerCount = getJoinPlayerCount();
		if (zhuangPlayer != null && detail.getTax() > 0) {
			detail.updateZhuangRecord(zhuangPlayer.getPlayerId(), flowMoney, taxMoney);
		}
		TaxRecordUtil.recordGameTaxInfo(startTime, joinPlayerCount, gameId, getGameType(), getConfId()
				, zhuangPlayer == null ? 0 : zhuangPlayer.getPlayerId(), flowMoney, taxMoney, detail, this);
		
		if(LogicApp.getInst().isStop()){
    	    DeskMgr.getInst().removeDesk(this);
    		return;
       }
	}

	private int getJoinPlayerCount() {
		int count = 0;
		for (Map<XianPosition, Integer> info : playerChipInfo.values()) {
			if (info.size() > 0) {
				count++;
			}
		}
		return count;
	}


	private void dealWithLeaveAndLogoutPlayer() {
		for (PlayerInfo player : leavePlayerList) {
			if (zhuangList.contains(player)) {
				zhuangList.remove(player);
				onZhuangInfoChange();
			}
			if (player == null) {
				continue;
			}
			player.write(ResponseCode.ACCOUNT_UPDATE_COIN, CommonCreator.createPBInt32(player.getCoin()));                //强行同步金币给玩家
			playerInfoMap.remove(player.getPlayerId());
			playerChipInfo.remove(player);
			DeskMgr.getInst().removePlayer(player,false);
//			LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, player.getPlayerId()));
		}
		leavePlayerList.clear();
	}
	
	/** 判斷庄家錢是否夠陪  不够时设置赔偿比例*/
	public void checkZhuangCoinEnough(Map<XianPosition, Integer> result) {
		if (zhuangPlayer == null) {
			return;
		}
		float totalCostGain = 0;
		for (PlayerInfo player : playerChipInfo.keySet()) {
			for (Map.Entry<XianPosition, Integer> oneXianResult : result.entrySet()) {
				int chipValue = getPlayerChipValue(player, oneXianResult.getKey());
				int resultValue = chipValue * oneXianResult.getValue();
				totalCostGain += resultValue;
			}
		}
		if (totalCostGain < 0){ // 庄家赢钱
			return;
		}
		if (zhuangPlayer.getCoin() >= totalCostGain) {  // 庄家够赔钱
			return;
		}
		zhuangPayValue = zhuangPlayer.getCoin() / totalCostGain;
		logger.error("{}|{}庄家钱不够赔  赔钱系数变为 {}", getDeskId(), getGameId(), zhuangPayValue);
	}

	public void calPlayerChip(Map<XianPosition, Integer> result) {
		checkZhuangCoinEnough(result);
		for (PlayerInfo player : playerChipInfo.keySet()) {
			calOnePlayerChip(player, result);
		}
		int bannerChangeCoin = 0;
		int bannerTax = 0;
		for (Map.Entry<XianPosition, Integer> entry : bannerCalResult.entrySet()) {
			bannerChangeCoin += entry.getValue();
		}
		if (zhuangPayValue < 1){ // 如果庄家钱不够赔  直接扣除庄家所有钱
			bannerChangeCoin = -zhuangPlayer.getCoin();
		}
		if (zhuangPlayer != null && bannerChangeCoin > 0) {
			int tempMoney = bannerChangeCoin;
			flowMoney += tempMoney;
			int taxRate = CoupleRoomInfoProvider.getInst().getTaxRate(getConfId());
			bannerChangeCoin = bannerChangeCoin * (100 - taxRate) % 100 == 0 ? bannerChangeCoin * (100 - taxRate) / 100 : bannerChangeCoin * (100 - taxRate) / 100 + 1;
			taxMoney += tempMoney - bannerChangeCoin;
			bannerTax += tempMoney - bannerChangeCoin;
		}
		bannerCalResult.put(XianPosition.FIVE, bannerChangeCoin);
		detail.addBannerMatch(zhuangCard, zhuangResult.getRight().getValue(), bannerChangeCoin > 0 ? 1 : 0);
		detail.setBannerIncomeAndTax(bannerChangeCoin, bannerTax);
		if (zhuangPlayer != null) {
			zhuangPlayer.setOld_coin(zhuangPlayer.getCoin());
			if (bannerChangeCoin > 0){
				zhuangPlayer.updateCoin(bannerChangeCoin, true);
			} else {
				zhuangPlayer.updateCoin(-bannerChangeCoin, false);
			}
			if (bannerChangeCoin > 0) {
				zhuangPlayer.write(RequestCode.LOG_MONEY.getValue()
						, LogHelper.logGainMoney(zhuangPlayer.getPlayerId(), MoneySubAction.NIUNIU_GAIN.getValue(), getGameType().getValue(), bannerChangeCoin, zhuangPlayer.getOld_coin(), zhuangPlayer.getCoin(), zhuangPlayer.getIp(), zhuangPlayer.getChannel_id(), String.valueOf(zhuangPlayer.getPackage_id()), zhuangPlayer.getDevice(), getGameId()));

				TaxRecordUtil.sendGamePlayerStatus(zhuangPlayer, bannerChangeCoin);
			} else {
				if (0 != Math.abs(bannerChangeCoin)) {
					zhuangPlayer.write(RequestCode.LOG_MONEY.getValue()
							, LogHelper.logLoseMoney(zhuangPlayer.getPlayerId(), MoneySubAction.NIUNIU_LOSE.getValue(), getGameType().getValue(), Math.abs(bannerChangeCoin), zhuangPlayer.getOld_coin(), zhuangPlayer.getCoin(), zhuangPlayer.getIp(), zhuangPlayer.getChannel_id(), String.valueOf(zhuangPlayer.getPackage_id()), zhuangPlayer.getDevice(), getGameId()));

					TaxRecordUtil.sendGamePlayerStatus(zhuangPlayer, -1 * Math.abs(bannerChangeCoin));
				}
			}
			bannerStatistics.add(bannerChangeCoin);
		} 
		int saveCoin = bannerChangeCoin;
		boolean isPlayerZhuang  =  zhuangPlayer != null;
		LogicActorManager.getLogicActor().put(() -> {
			addBannerChangeCoinToDatabase(saveCoin,isPlayerZhuang);
			return null;
		});
	}

	private void addBannerChangeCoinToDatabase(int coin,boolean isPlayerZhuang) {
		Map<String, Object> where = new HashMap<>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		where.put("time", (cal.getTimeInMillis() / 1000));
		where.put("roomId", getConfId());
		List<ASObject> resultList = DataQueryResult.load("banner_gain", where);
		String dbFiledName = null;
		if(isPlayerZhuang){
			dbFiledName = "player_coin";
		}else{
			dbFiledName = "coin_value";
		}
		if (resultList.size() == 0) {
			Map<String, Object> data = new HashMap<>();
			data.put(dbFiledName, coin);
			try {
				DBUtil.executeInsertOrUpdate("banner_gain", where, data, false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			long currCoin = resultList.get(0).getLong(dbFiledName);
			Map<String, Object> data = new HashMap<>();
			data.put(dbFiledName, coin + currCoin);
			try {
				DBUtil.executeUpdate("banner_gain", where, data);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	public int getNiuNiuPlayerCount() {
		return playerChipInfo.size();
	}

	public int getNiuniuBannerCount() {
		return zhuangPlayer == null ? 0 : 1;
	}

	/** 更新四个位置输赢总和 */
	private void updateBannerResult(XianPosition position, int value) {
		Integer bannerCoin = bannerCalResult.get(position);
		if (bannerCoin == null) {
			bannerCalResult.put(position, value * -1);
		} else {
			bannerCalResult.put(position, bannerCoin - value);
		}
	}

	public void calOnePlayerChip(PlayerInfo player, Map<XianPosition, Integer> result) {
		Map<XianPosition, Integer> chipResult = new HashMap<>();
		int taxCount = 0;
		int totalCostGain = 0;
		for (Map.Entry<XianPosition, Integer> oneXianResult : result.entrySet()) {
			int chipValue = getPlayerChipValue(player, oneXianResult.getKey());
			int resultValue = chipValue * oneXianResult.getValue();
			if (resultValue > 0) { // 如果是赚钱 需要乘以系数 防止庄家不够赔钱
				resultValue *= zhuangPayValue;
			}
			chipResult.put(oneXianResult.getKey(), resultValue);
			totalCostGain += resultValue;
			updateBannerResult(oneXianResult.getKey(), resultValue);
		}
		if (totalCostGain > 0) {
			int tempMoney = totalCostGain;
			flowMoney += totalCostGain;
			int taxRate = CoupleRoomInfoProvider.getInst().getTaxRate(getConfId());
			totalCostGain = totalCostGain * (100 - taxRate) % 100 == 0 ? totalCostGain * (100 - taxRate) / 100 : totalCostGain * (100 - taxRate) / 100 + 1;
			taxMoney += (tempMoney - totalCostGain);
			taxCount += (tempMoney - totalCostGain);
			player.write(RequestCode.LOG_MONEY.getValue(), LogHelper.logGainMoney(player.getPlayerId()
					, MoneySubAction.NIUNIU_GAIN.getValue(), getGameType().getValue(), totalCostGain, player.getCoin(), player.getCoin()+totalCostGain, player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));
			TaxRecordUtil.sendGamePlayerStatus(player, totalCostGain);
		} else {
			totalCostGain = player.getCoin() > totalCostGain ? totalCostGain : player.getCoin(); // 錢不夠保護處理
			if (0 != Math.abs(totalCostGain)) {
				player.write(RequestCode.LOG_MONEY.getValue(), LogHelper.logLoseMoney(player.getPlayerId()
						, MoneySubAction.NIUNIU_LOSE.getValue(), getGameType().getValue(), Math.abs(totalCostGain), player.getCoin(), player.getCoin()-Math.abs(totalCostGain), player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));
				TaxRecordUtil.sendGamePlayerStatus(player, -1 * Math.abs(totalCostGain));
			}
		}
		chipResult.put(XianPosition.FIVE, totalCostGain);
		int pre_coin = player.getCoin();
		if (totalCostGain > 0){
			player.updateCoin(totalCostGain, true);
		} else {
			player.updateCoin(-totalCostGain, false);
		}
		int totalChangeValue = 0;
		Map<Integer, Pair<Integer, Integer>> map_bet_cast = new HashMap<>();
		for (Map.Entry<XianPosition, Integer> oneXianResult : result.entrySet()) {
			if (true == chipResult.containsKey(oneXianResult.getKey())) {
				//输赢金额值
				int oneChangeValue = chipResult.get(oneXianResult.getKey());
				totalChangeValue += oneChangeValue;
				if(0 != oneChangeValue) {
					Pair<Integer, Integer> tmp_pair = new Pair<>();
					tmp_pair.setLeft(getSelfChip(player, oneXianResult.getKey().getValue()));
					tmp_pair.setRight(oneChangeValue);
					map_bet_cast.put(oneXianResult.getKey().getValue(), tmp_pair);
				}
			}
		}
		detail.addPlayerRecord(player.getName(), player.getPlayerId(), getCurrentPlayerChip(player), totalChangeValue
				, taxCount, 0, 0, player.getChannel_id(), player.getPackage_id(), player.getDevice(), pre_coin, player.getCoin(), player.getIp(), map_bet_cast);
		if (totalChangeValue < 0) {
			if (zhuangList.contains(player)) {
				if (player.getCoin() < getConfNiu().getBannerLimit()) {
					zhuangList.remove(player);
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ZHUANG_COIN_NOT_ENOUGH));
					onZhuangInfoChange();
				}
			}
		}
		playerCalResultMap.put(player, chipResult);
	}

	/** 单个位置下注信息 */
	private int getPlayerChipValue(PlayerInfo player, XianPosition position) {
		Map<XianPosition, Integer> info = playerChipInfo.get(player);
		if (info == null) {
			return 0;
		}
		Integer result = info.get(position);
		if (result == null) {
			return 0;
		}
		return result;
	}

	/** 客户端发起 续押 */
	public void playerRenewad(PlayerInfo player, List<PBPair> renewadInfo) {
		if (state != NiuNiuState.CHIP_TIME) {
			logger.error(" is not niuniu chip time ===============");
			return;
		}
		if (zhuangPlayer == player) {
			logger.error(" the banner player can't chip");
			return;
		}

		List<ChipType> typeList = new ArrayList<>();

		for (PBPair e : renewadInfo) {
			int chipId = e.getValue();
			ChipType type = ChipType.getTypeById(chipId);
			if (type == null) {
				logger.error(" illegal chip type {}", chipId);
				return;
			}
			typeList.add(type);
		}

		boolean canTotalChip = canRenewad(typeList);
		boolean canSelfChip = playerCanRenewad(player, typeList);

		if (canTotalChip && canSelfChip) {
			renewadInfo.forEach(e -> {
				doPlayerOneChip(player, e.getKey(), e.getValue());
			});

			player.write(ResponseCode.NIUNIU_RENEWED_SUCC, CommonCreator.createPbPairList(renewadInfo));
		} else {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.CHIP_NOT_ENOUGH));
		}
	}
	
	/** 客户端发起下注 */
	public void playerChip(PlayerInfo player, int xianPosition, int chipId) {
		if (state != NiuNiuState.CHIP_TIME) {
			logger.error(" is not niuniu chip time ===============");
			return;
		}
		if (zhuangPlayer == player) {
			logger.error(" the banner player can't chip");
			return;
		}
		ChipType type = ChipType.getTypeById(chipId);
		if (type == null) {
			logger.error(" illegal chip type {}", chipId);
			return;
		}
		//桌上还能否下注
		boolean canTotalChip = canChip(type);
		//玩家自己金币还能否下注
		boolean canSelfChip = playerCanChip(player, type);
		if (canTotalChip && canSelfChip) {
			doPlayerOneChip(player, xianPosition, chipId);
			player.write(ResponseCode.NIUNIU_CHIP_SUCC, CommonCreator.createPBIntIntList(xianPosition, chipId));
		} else {
			if (!canTotalChip) {//桌上不能继续下注了
				//将使用最低的下注币
				int totalRemainCount = getTotalRemainChipCount();
				List<Integer> chipList = ChipType.getChipList(totalRemainCount);
				//没有最小可以使用的币种
				if (chipList == null || chipList.size() == 0) {
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.CHIP_FULL));
					return;
				}
				//可以使用，系统自动辅助下注
				chipList.forEach(e -> doPlayerOneChip(player, xianPosition, e));
				player.write(ResponseCode.NIUNIU_CHIP_SUCC, CommonCreator.createPBIntIntList(xianPosition, chipList));
			} else if (!canSelfChip) {
				//自己的钱不够下注
				int selfRemainCount = getSelfRemainChipCount(player);
				List<Integer> chipList = ChipType.getChipList(selfRemainCount);
				//使用最小的币种
				if (chipList == null || chipList.size() == 0) {
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.CHIP_MAX));
					return;
				}
				//辅助下注
				chipList.forEach(e -> doPlayerOneChip(player, xianPosition, e));
				player.write(ResponseCode.NIUNIU_CHIP_SUCC, CommonCreator.createPBIntIntList(xianPosition, chipList));
			} else {
				// do nothing
			}
		}
	}
	
	/** 玩家下注 */
	private void doPlayerOneChip(PlayerInfo player, int xianPosition, int chipId) {
		Map<XianPosition, Integer> chipInfo = playerChipInfo.get(player);
		if (chipInfo == null) {
			logger.debug(" the chip info is null , player {} is not int the niuniu game ", player.getPlayerId());
			return;
		}
		ChipType type = ChipType.getTypeById(chipId);
		if (type == null) {
			return;
		}
		XianPosition position = XianPosition.getByValue(xianPosition);
		if (position == null) {
			logger.debug(" the xian position {} is null ", xianPosition);
			return;
		}
		logger.info("{}|玩家{}在闲{}位置下注{}", gameId, player.getPlayerId(), position.getValue(), type.getValue());
		Integer value = chipInfo.get(position);
		if (value == null) {
			chipInfo.put(position, type.getValue());
		} else {
			chipInfo.put(position, type.getValue() + value);
		}
		addSelfChipRecord(player, position, type);
		addTotalChip(position, type);
	}

	/** 增加玩家下注信息 */
	private void addSelfChipRecord(PlayerInfo player, XianPosition position, ChipType type) {
		Map<XianPosition, Map<Integer, Integer>> playerRecord = playerChipInfoRecord.get(player);
		if (playerRecord == null) {
			playerRecord = new HashMap<>();
			playerChipInfoRecord.put(player, playerRecord);
		}
		Map<Integer, Integer> selfRecord = playerRecord.get(position);
		if (selfRecord == null) {
			selfRecord = new HashMap<>();
			playerRecord.put(position, selfRecord);
		}
		Integer result = selfRecord.get(type.getId());
		if (result == null) {
			selfRecord.put(type.getId(), 1);
		} else {
			selfRecord.put(type.getId(), result + 1);
		}
	}

	/** 增加桌上每个(黑红梅块)位置下注信息 */
	private void addTotalChip(XianPosition position, ChipType type) {
		Integer value = totalChipInfo.get(position);
		if (value == null) {
			totalChipInfo.put(position, type.getValue());
		} else {
			totalChipInfo.put(position, type.getValue() + value);
		}
		Map<Integer, Integer> chipRecord = chipInfoRecord.get(position);
		if (chipRecord == null) {
			chipRecord = new HashMap<>();
			chipInfoRecord.put(position, chipRecord);
		}
		Integer recordValue = chipRecord.get(type.getId());
		if (recordValue == null) {
			chipRecord.put(type.getId(), 1);
		} else {
			chipRecord.put(type.getId(), recordValue + 1);
		}
	}

	public void playerRobZhuang(PlayerInfo player) {
		if (player.getCoin() < getConfNiu().getBannerLimit()) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ROB_ZHUANG_COIN_NOT_ENOUGH));
			return;
		}
		if (zhuangList.contains(player)) {
			logger.debug(" you already in  banner list ");
			return;
		}
		zhuangList.offer(player);
		if (getSelfRobZhuangPosition(player) == 1) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ROB_ZHUANG_SUC));
		} else {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ROB_ZHUANG_WAIT_OTHER));
		}
		onZhuangInfoChange();
	}

	public void resetGameForPlayer(PlayerInfo player) {
		leavePlayerList.remove(player);
		logoutPlayerList.remove(player);
		sendDeskInfoToPlayer(player);
	}

	public void playerGiveUpBanner(PlayerInfo player) {
		bannerChange = true;
		player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PLAYER_GIVE_UP_BANNER));
	}

	public void playerCancelBanner(PlayerInfo player) {
		if (zhuangList.contains(player)) {
			zhuangList.remove(player);
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PLAYER_CANCEL_BANNER));
		} else {
			//  you don't in the zhuang list
		}
		onZhuangInfoChange();
	}

	private void onZhuangInfoChange() {
		for (PlayerInfo player : playerChipInfo.keySet()) {
			player.write(ResponseCode.NIUNIU_BANNER_CHANGE, CommonCreator.createPBPair(getSelfRobZhuangPosition(player), zhuangList.size()));
		}
	}

	public List<PlayerInfo> getNiuniuDeskRank(PlayerInfo player) {
		List<PlayerInfo> playerList = getPlayerList();
		Collections.sort(playerList, (e, f) -> f.getCoin() - e.getCoin());
		if (playerList.size() <= 10) {
			player.write(ResponseCode.NIUNIU_RANK_INFO, createPBNiuniuRankList(playerList));
		} else {
			player.write(ResponseCode.NIUNIU_RANK_INFO, createPBNiuniuRankList(playerList.subList(0, 10)));
		}
		return null;
	}

	private Niuniu.PBNiuniuRankList createPBNiuniuRankList(List<PlayerInfo> list) {
		Niuniu.PBNiuniuRankList.Builder builder = Niuniu.PBNiuniuRankList.newBuilder();
		list.forEach(e -> {
			Niuniu.PBNiuniuRankItem.Builder subBuilder = Niuniu.PBNiuniuRankItem.newBuilder();
			subBuilder.setIcon(e.getIcon());
			subBuilder.setCoin(e.getCoin());
			builder.addRank(subBuilder.build());
		});
		return builder.build();
	}

	@Override
	protected void playerMoneyChangeHook(PlayerInfo info) {

	}

	@Override
	public GameType getGameType() {
		return GameType.NIUNIU;
	}

	@Override
	public void playerReady(PlayerInfo player) {

	}

	@Override
	public List<PlayerInfo> getPlayerList() {
		return new ArrayList<>(playerChipInfo.keySet());
	}

	@Override
	public void playerReLogin(PlayerInfo player) {
		leavePlayerList.remove(player);
		logoutPlayerList.remove(player);
	}

	public PlayerInfo getPlayerInfo(int playerId) {
		return playerInfoMap.get(playerId);
	}

	private void clearOneGame() {
		zhuangCard.clear();
		xianCardMap.clear();
		for (Map<XianPosition, Integer> map : playerChipInfo.values()) {
			map.clear();
		}
		zhuangPayValue = 1;
		flowMoney = 0;
		taxMoney = 0;
		leavePlayerList.clear();
		logoutPlayerList.clear();
		playerCalResultMap.clear();
		xianResultMap.clear();
		chipInfoRecord.clear();
		playerChipInfoRecord.clear();
		bannerCalResult.clear();
	}

	@Override
	protected void disbandDesk() {

	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	@Override
	public void recordPlayerTaxInfo(Object detail, int roomId) {
		NiuniuDetail niuniu_detail = (NiuniuDetail)detail;
		for(OneNiuniuRecord one : niuniu_detail.getRecords()) {
			if(0 >= one.getTax()) {
				continue;
			}

			TaxRecordUtil.recordPlayerTaxInfoToDB(niuniu_detail.getType(), one.getPlayerId(), roomId, one.getTax(), one.getChannel_id(), one.getPackage_id(), one.getDevice());
		}
	}

	@Override
	public void destroy() {
		isDestroyed.set(true);
		stopOperationFuture();
		stopOverMyselfFuture();
		playerInfoMap.clear();
		playerChipInfo.clear();
		logoutPlayerList.clear();
		leavePlayerList.clear();
	}

	@Override
	public void disBankDeskTimeOver() {
		
	}

	@Override
	public void enterPlayer(PlayerInfo player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playerWantContinue(PlayerInfo playerInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPlayerLeave(PlayerInfo player) {
		return leavePlayerList.contains(player);
	}
}
