package logic.poker.ddz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import config.CoupleRoomInfoProvider;
import config.JsonUtil;
import config.bean.ConfMatchCard;
import data.MoneySubAction;
import define.constant.MessageConst;
import logic.DeskMgr;
import logic.debug.ArrayPai;
import logic.define.GameType;
import logic.majiong.GameConst;
import logic.majiong.PlayerInfo;
import logic.poker.DdzBottonCardType;
import logic.poker.PokerCard;
import logic.poker.PokerDesk;
import logic.poker.PokerMatchCardUtil;
import logic.poker.PokerUtil;
import logic.record.TaxRecordUtil;
import logic.record.ddz.DdzRecord;
import logic.record.detail.DdzDetail;
import logic.record.detail.DdzDetailBase;
import proto.Ddz;
import proto.creator.CommonCreator;
import proto.creator.CoupleCreator;
import protocol.s2c.ResponseCode;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;

/**
 * Created by Administrator on 2017/3/14.
 * 斗地主
 */
public class DdzDesk extends PokerDesk {
	private static final Logger logger = LoggerFactory.getLogger(DdzDesk.class);
	public static final int TIMEOUT_TIME_LIMIT = 2;
	private Map<PlayerInfo, DdzDeskInfo> deskInfoMap = new HashMap<>();
	protected Map<DdzPos, DdzDeskInfo> pos2InfoMap = new HashMap<>();
	private int rounds;
	private DdzPos lordPos = null;
	private DdzPos lasRunPos = null;
	private DdzPos lastDiscardPos = null;
	protected DdzPos callPos = null;
	private ScheduledFuture<?> operationFuture;
	private Stack<DdzGroup> deskStop;
	protected boolean callLord;                       //叫地主阶段
	private List<PokerCard> lordCards;
	private DdzState state;
	private int stateRemainTime;
	protected List<PokerCard> remainCards;
	protected DdzPos currentPos = null;
	protected int lzValue;
	private DdzRecord record;
	private long gameId;
	protected int startTime;
	DdzDetail ddz_detail = null;

	public DdzDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId);
		this.deskStop = new Stack<>();
		this.callLord = false;
		lordCards = new ArrayList<>();
		remainCards = new ArrayList<>();
		state = DdzState.READY;
		lzValue = 0;
		initDdzDeskInfo(playerList);
		record = new DdzRecord(playerList, isPersonal() ?  getDeskId() : getConfId(), getGameType().getValue(),++rounds, getBaseScore());
		this.ddz_detail = new DdzDetail(getGameType() == null ? 0 : getGameType().getValue());
		operationFuture = LogicActorManager.registerOneTimeTask(5000, () -> checkPlayerReady(), getDeskId());
	}

	private void checkPlayerReady() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		boolean hasNoReady = false;
		List<PlayerInfo> list = getPlayerList();
		for (PlayerInfo playerInfo : list) {
			DdzDeskInfo info = getDeskInfo(playerInfo);
			if (!info.isReady()) {
//				msgHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(info.getPositionValue()));
//				DeskMgr.getInst().removePlayer(e,false);
////				LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, e.getPlayerId()));
				hasNoReady = true;
				break;
			}
		}
		if(hasNoReady){
			DeskMgr.getInst().removeDesk(this);
		}
	}

	private void initDdzDeskInfo(List<PlayerInfo> playerList) {
		int count = 0;
		for (PlayerInfo player : playerList) {
			DdzDeskInfo info = new DdzDeskInfo();
			count++;
			info.setPos(DdzPos.getByValue(count));
			deskInfoMap.put(player, info);
			player.setPosition(count);
			pos2InfoMap.put(info.getPos(), info);

			logger.info("{} | {} |初始化玩家 {} 位置 {}", this.deskId, this.gameId, player.getPlayerId(), info.getPositionValue());
		}
	}

	public void tuoguanGame(PlayerInfo player) {
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			logger.warn(" the player is null whe tuoguan");
			return;
		}
		info.setTuoguan(true);

		logger.info("{} | {} |设置玩家 {} 托管", this.deskId, this.gameId, player.getPlayerId());
		msgHelper.notifyMessage(ResponseCode.DDZ_TUOGUAN, CommonCreator.createPBInt32(info.getPositionValue()));
		if (currentPos == info.getPos()) {
			autoDiscard(player,0);
		}
	}


	private void autoDiscard(PlayerInfo player,int timeOutDiscard) {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		//如果上把是当前玩家
		if (this.state != DdzState.DISCARD) {
			return;
		}
		logger.info("{} | {} |当前位置玩家 {} 自动出牌", this.deskId, this.gameId, player.getPlayerId());

		if (lastDiscardPos == null || lastDiscardPos == getPlayerPos(player)) {
			discardCard(player, Arrays.asList(PokerUtil.getMinValueCard(getDeskInfo(player).getHandCards()).getKey()),timeOutDiscard);
		} else {
			passCard(player,timeOutDiscard);
		}
	}

	public void cancelTuoguan(PlayerInfo player) {
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			logger.warn("the player is null when cancel tuoguan");
			return;
		}

		logger.info("{} | {} |玩家{} 取消托管", this.deskId, this.gameId, player.getPlayerId());
		info.setTuoguan(false);
		info.setTimeoutTimes(0);
		info.stopAutoDiscard();
		msgHelper.notifyMessage(ResponseCode.DDZ_CANCEL_TUOGUAN, CommonCreator.createPBInt32(info.getPositionValue()));                //取消托管不做任何处理
	}

	public void passCard(PlayerInfo player,int timeOutDiscard) {
		if (currentPos != getPlayerPos(player)) {
			logger.debug(" is not your turn to discard");
			return;
		}
		if (currentPos == lastDiscardPos) {
			logger.debug(" you can't pass your self ");
			return;
		}

		record.addStep(currentPos.getValue(), null);
		DdzDeskInfo info = getDeskInfo(player);
		info.setLastRoundCards(null);
		msgHelper.notifyMessage(ResponseCode.DDZ_PASS, CommonCreator.createPBInt32(currentPos.getValue()));
		if(timeOutDiscard == -1){
			info.setTimeoutTimes(0);
			timeOutDiscard = info.getTimeoutTimes();
		}
		if(timeOutDiscard == 1){
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DDZ_TIME_OUT_AUTO_DISCARD));
		}
		currentPos = nextPos();
		logger.info("{} | {} |玩家 {} pass ,下家位置为 {}", this.deskId, this.gameId, player.getPlayerId(), currentPos);
		beginDiscard();
	}

	/** 抢地主次数 */
	protected int getRobLordTimes() {
		int totalTimes = 0;
		for (DdzDeskInfo info : deskInfoMap.values()) {
			totalTimes += info.getRobTimes();
		}
		return totalTimes;
	}

	/** 找出桌上炸弹数 */
	private int getBonusCount() {
		int count = 0;
		for (DdzGroup group : deskStop) {
			if (group.getType() == DdzGroupType.FOUR || group.getType() == DdzGroupType.JOKER_BONUS) {
				count++;
			}
		}
		return count;
	}

	@Override
	public boolean isAllPlayerLeave() {
		return deskInfoMap.values().stream().noneMatch(e -> !e.isLeave());
	}

	/** 抢地主和加倍总数 暂时只有抢地主赋值 */
	private int getRobAndJiabeiCount() {
		int count = 0;
		for (DdzDeskInfo info : deskInfoMap.values()) {
			if (info.isBonus()) {
				count++;
			}
		}
		count += getRobLordTimes();
		return count;
	}

	private int getFinalBonusCount(boolean lordWin) {
		return getFinalBonusCount(lordWin, true);
	}

	private int getFinalBonusCount(boolean lordWin, boolean isGameEnd) {
		int bonusCount = getBonusCount();
		int robCount = getRobAndJiabeiCount();
		int bottomTimes = DdzBottonCardType.getBottomBonus(lordCards);
		int finalTimes = (1 << (bonusCount + robCount)) * bottomTimes;
		//春天的计算
		if (isGameEnd) {
			if (lordWin)
				finalTimes = farmDiscardTimes() == 0 ? finalTimes * 2 : finalTimes;
			else
				finalTimes = lordDiscardTimes() == 1 ? finalTimes * 2 : finalTimes;

		}
		return finalTimes;
	}

	public int discardCard(PlayerInfo player, List<Integer> pokerList,int timeOutDiscard) {
		long time1 = System.currentTimeMillis();
		if (currentPos != getPlayerPos(player)) {
			logger.debug(" is not your turn to discard");
			return 1;
		}
		List<PokerCard> cardList = PokerUtil.convert2PokerCard(pokerList);
		if (!checkCardExist(player, cardList)) {
			logger.debug(" you don't have the card list ");
			return 2;
		}
//		cardList.forEach(e -> System.out.print(e + ","));
//		logger.info("玩家{}出牌:{}",player.getPlayerId(),cardList);
		//because the method will change the value of the card list so  copy a new list into it
		Pair<DdzGroupType, List<Integer>> discardResult = checkIfIsBigger(player, new ArrayList<>(cardList), lastDiscardPos == currentPos);
		if (discardResult == null) {
			logger.debug(" you card is not bigger than the desktop ");
			return 3;
		}
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			logger.debug("  not have the card info");
			return 4;
		}
		if(timeOutDiscard == -1){
			info.setTimeoutTimes(0);
			timeOutDiscard = info.getTimeoutTimes();
		}
		info.removeCardList(cardList);
		DdzGroup group = new DdzGroup(discardResult.getLeft(), cardList, discardResult.getRight());
		long time2 = System.currentTimeMillis();
		logger.debug(" the card group type is {} and the value is {} and discard cost time {}"
				, discardResult.getLeft(), discardResult.getRight().size() == 0 ? "joker_bonus" : discardResult.getRight().get(0), time2 - time1);
		this.deskStop.push(group);
		info.addCard2DiscardStack(group);
		record.addStep(info.getPositionValue(), cardList);
		msgHelper.notifyMessage(ResponseCode.DDZ_DISCARD_CARD, createPBDdzDiscard(getPlayerPosValue(player), group.getType().getValue(), pokerList));
		if(timeOutDiscard == 1){
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DDZ_TIME_OUT_AUTO_DISCARD));
		}

		logger.info("{} | {} |玩家 {} 出牌 {}", this.deskId, this.gameId, player.getPlayerId(), new Gson().toJson(pokerList));

		//每次打牌都计算一下 是否已经把所有的牌打完了
		if (isDiscardAll(info)) {
			calculateGameResult(player);
		} else {
			lastDiscardPos = currentPos;
			currentPos = nextPos();
			beginDiscard();
		}
		return 0;
	}

	private Ddz.PBDdzDiscard createPBDdzDiscard(int pos, int type, List<Integer> pokerList) {
		Ddz.PBDdzDiscard.Builder builder = Ddz.PBDdzDiscard.newBuilder();
		builder.setPos(pos);
		builder.setType(type);
		pokerList.forEach(e -> builder.addCardList(e));
		return builder.build();
	}


	protected boolean isDiscardAll(DdzDeskInfo info) {
		logger.debug(" check if card end and the hand card size is {}", info.getHandCards().size());
		return info.getHandCards().size() == 0;
	}

	private void clearGame() {
		deskInfoMap.values().forEach(e -> e.clear());
		deskStop.clear();
		lordCards.clear();
		callLord = false;
		lzValue = 0;
		lastDiscardPos = null;

		logger.info("{} | {} |结束牌局}", this.deskId, this.gameId);
	}

	/** 牌局结束 */
	private void calculateGameResult(PlayerInfo runPlayer) {
		DdzDeskInfo info = getDeskInfo(runPlayer);
		if (info == null) {
			return;
		}
		lasRunPos = info.getPos();
		Ddz.PBDdzCalculate.Builder builder = Ddz.PBDdzCalculate.newBuilder();
		builder.setRunPos(lasRunPos.getValue());
		int finalTimes = getFinalBonusCount(info.isLord());
		deskInfoMap.values().forEach(e -> builder.addPlayerCard(CommonCreator.createPBIntIntList(e.getPositionValue(), PokerUtil.convert2IntList(e.getHandCards()))));
		int pre_coin = 0;
		int totalGain = 0;
		int tax_coin = 0;
		int tmp_cur_coin = 0;
		boolean isFamerWin = !info.isLord();
		//计算的时候先计算输的钱， 然后在计算赢得钱， 避免出现负数
		for (PlayerInfo player : getFarmers()) {
			int changeMoney = 0;
			tmp_cur_coin = 0;
			if (!isFamerWin)
				changeMoney = -1 * MiscUtil.min(player.getCoin(), finalTimes * getBaseScore(), runPlayer.getCoin() / 2);
			else
				changeMoney = MiscUtil.min(player.getCoin(), finalTimes * getBaseScore(), getLordPlayer().getCoin() / 2);
			pre_coin = player.getCoin();
			totalGain -= changeMoney;
			if (changeMoney > 0) {
				tmp_cur_coin = changeMoney;
				changeMoney = (int) (changeMoney * (getGainRate() / 100f));
				tax_coin += (tmp_cur_coin - changeMoney);
			}
			player.updateCoin(Math.abs(changeMoney), isFamerWin, getLogMoneyType(isFamerWin ? 1 : 0), getGameType().getValue(), getGameId());
			builder.addPlayerCal(createPBDdzCalculateItem(player, finalTimes, changeMoney));
			ddz_detail.addOneRecord(player.getPlayerId(), isFamerWin ? 1 : 0, isFamerWin ? tmp_cur_coin - changeMoney : 0, pre_coin, player.getCoin(), player);
			TaxRecordUtil.sendGamePlayerStatus(player, changeMoney);
		}
		PlayerInfo lordPlayer = getLordPlayer();
		tmp_cur_coin = totalGain;
		totalGain = (int) (totalGain > 0 ? totalGain * (getGainRate() / 100f) : totalGain);
		tax_coin = 0 < tmp_cur_coin ? tmp_cur_coin - totalGain : tax_coin;
		pre_coin = lordPlayer.getCoin();
		lordPlayer.updateCoin(Math.abs(totalGain), info.isLord(), getLogMoneyType(info.isLord() ? 1 : 0), getGameType().getValue(), getGameId());                        //农民赢的钱 地主是减去的
		ddz_detail.addOneRecord(lordPlayer.getPlayerId(),  totalGain > 0 ? 1 : 0, totalGain > 0 ? tmp_cur_coin - totalGain : 0, pre_coin, lordPlayer.getCoin(), lordPlayer);
		TaxRecordUtil.sendGamePlayerStatus(lordPlayer, totalGain);
		builder.addPlayerCal(createPBDdzCalculateItem(lordPlayer, finalTimes * getFarmers().size(), totalGain));
		Ddz.PBDdzCalculate resultMessage = builder.build();
		record.addCalInfo(resultMessage);
		msgHelper.notifyMessage(ResponseCode.DDZ_RESULT, resultMessage);
		syncAllPlayerMoney();
		TaxRecordUtil.recordGamReply(new Gson().toJson(record), startTime, deskInfoMap.keySet(), getGameType(), String.valueOf(gameId));
		TaxRecordUtil.recordGameTaxInfo(startTime, getFarmers().size(), getGameId()
				, getGameType(), getConfId(), getPlayerInfoByPos(lordPos) == null ? 0 : getPlayerInfoByPos(lordPos).getPlayerId(), 0 < tmp_cur_coin ? tmp_cur_coin : -tmp_cur_coin, tax_coin, ddz_detail, this);
		onGameEnd();
	}

	private MoneySubAction getLogMoneyType(int win_lose) {
		switch (getGameType()) {
			case DDZ:
				if (1 == win_lose) {
					return MoneySubAction.DDZ_WIN;
				} else {
					return MoneySubAction.DDZ_LOSE;
				}
			case COUPLE_DDZ:
				if (1 == win_lose) {
					return MoneySubAction.DDZ_COUPLE_WIN;
				} else {
					return MoneySubAction.DDZ_COUPLE_LOSE;
				}
			case LZ_DDZ:
				if (1 == win_lose) {
					return MoneySubAction.DDZ_LZ_WIN;
				} else {
					return MoneySubAction.DDZ_LZ_LOSE;
				}
			default:
				return null;
		}
	}

	/** 农民出牌总次数 */
	private int farmDiscardTimes() {
		int totalCount = 0;
		for (PlayerInfo info : getFarmers()) {
			DdzDeskInfo deskInfo = getDeskInfo(info);
			if (deskInfo == null) {
				continue;
			}
			totalCount += deskInfo.getDeskStack().size();
		}
		return totalCount;
	}

	/** 地主出牌总次数  */
	private int lordDiscardTimes() {
		DdzDeskInfo lordInfo = pos2InfoMap.get(lordPos);
		if (lordInfo == null) {
			return 0;
		}
		return lordInfo.getDeskStack().size();
	}


	private Ddz.PBDdzCalculateItem createPBDdzCalculateItem(PlayerInfo player, int times, int coin) {
		Ddz.PBDdzCalculateItem.Builder builder = Ddz.PBDdzCalculateItem.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setName(player.getName());
		builder.setTimes(times);
		builder.setGainLose(coin);
		return builder.build();
	}

	/** 取出地主信息 */
	private PlayerInfo getLordPlayer() {
		for (Map.Entry<PlayerInfo, DdzDeskInfo> entry : deskInfoMap.entrySet()) {
			if (entry.getValue().isLord()) {
				return entry.getKey();
			}
		}
		return null;
	}

	private PlayerInfo getMyPartner(PlayerInfo player) {
		for (PlayerInfo info : getFarmers()) {
			if (info != player) {
				return info;
			}
		}
		return null;
	}

	/** 找出所有农民 */
	private List<PlayerInfo> getFarmers() {
		List<PlayerInfo> result = new ArrayList<>();
		for (Map.Entry<PlayerInfo, DdzDeskInfo> entry : deskInfoMap.entrySet()) {
			if (!entry.getValue().isLord()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}


	@Override
	protected Map<Integer, Integer> getAllPlayerMoney() {
		Map<Integer, Integer> result = new HashMap<>();
		for (Map.Entry<PlayerInfo, DdzDeskInfo> entry : deskInfoMap.entrySet()) {
			result.put(entry.getValue().getPositionValue(), entry.getKey().getCoin());
		}
		return result;
	}

	/**检查玩家有没有请求打出去的牌*/
	private boolean checkCardExist(PlayerInfo player, List<PokerCard> list) {
		DdzDeskInfo info = deskInfoMap.get(player);
		if (info == null) {
			return false;
		}
		return info.containCards(list);
	}

	/** 此牌能否出且比上家(非自己)大 */
	private Pair<DdzGroupType, List<Integer>> checkIfIsBigger(PlayerInfo player, List<PokerCard> list, boolean self) {
		//取出最后打的牌类型和数据
		DdzGroup currentGroup = deskStop.empty() ? null : deskStop.peek();
		//取出自己打出的牌的类型
		List<DdzGroupType> srcList = DdzGroupType.getByCardNum(list.size());
		if (srcList == null) {
			return null;
		}
		//如果是第一次打牌，不用验证之前的牌
		if (currentGroup == null) {
			//且一定要是地主
			if (getDeskInfo(player).isLord()) {
				return PokerUtil.filterDdzGroupType(srcList, list, lzValue);
			} else {
				return null;
			}
		}
		//如果最后是自己打的牌,不用验证之前的牌
		if (self) {
			return PokerUtil.filterDdzGroupType(srcList, list, lzValue);
		}
		//
		Pair<DdzGroupType, List<Integer>> result = PokerUtil.filterDdzGroupType(PokerUtil.getDiscardGroupList(srcList, currentGroup.getType()), list, lzValue);
		if (result == null) {
			return null;
		}
		if (isBiggerThanDesk(new DdzGroup(result.getLeft(), list, result.getRight()), currentGroup)) {
			return result;
		}
		return null;
	}

	/** 是否比桌上的牌大 */
	protected boolean isBiggerThanDesk(DdzGroup src, DdzGroup target) {
		if (src.getType() == target.getType()) {
			if (PokerUtil.isBigger(src.getType(), src.getCardDesc(), target.getCardDesc())) {
				return true;
			} else {
				return false;
			}
		} else {
			if (src.getType() != DdzGroupType.FOUR && src.getType() != DdzGroupType.JOKER_BONUS) {
				return false;
			} else {
				if (target.getType() == DdzGroupType.JOKER_BONUS) {
					return false;
				} else {
					return true;
				}
			}
		}
	}


	private void setState(DdzState state) {
		this.state = state;
		this.stateRemainTime = state.getSeconds();
	}


	private void startNewGame() {
		stopOperation();
		gameId = geneGameNo();
		dealAllPlayerCard();
		currentPos = getStartLordPos();                //发牌过后几秒才开始的啊
		rounds++;
		msgHelper.notifyGameStart();
		startTime = MiscUtil.getCurrentSeconds();
		setState(DdzState.DEAL_CARD);
		operationFuture = LogicActorManager.getTimer()
				.register(1000, 1000, this::update, LogicActorManager.getDeskActor(getDeskId()), "ddz_update");

		logger.info("开始ddz游戏,deskId 为 {} , gameId为 {}", this.getDeskId(), gameId);
	}

	private void stopOperation() {
		if (operationFuture != null) {
			operationFuture.cancel(true);
			operationFuture = null;
		}
	}

	private void update() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		if (stateRemainTime > 0) {
			stateRemainTime--;
			return;
		}
		switch (state) {
			case DEAL_CARD:
				setState(DdzState.CALL_LORD);
				beginChoseLord();
				break;
			case CALL_LORD:
				robLord(getCurrentOperationPlayer(), false);
				break;
			case ROB_LORD:
				robLord(getCurrentOperationPlayer(), false);
				break;
			case ROB_END:
				beginDiscard();
				break;
			case DISCARD:
				DdzDeskInfo info = pos2InfoMap.get(currentPos);
				if (info != null) {
					info.addTimeoutTimes();
					if (info.getTimeoutTimes() >= TIMEOUT_TIME_LIMIT) {
						tuoguanGame(getCurrentOperationPlayer());
					} else {
						autoDiscard(getCurrentOperationPlayer(),info.getTimeoutTimes());
					}
				}
				break;
			case LIUJU:
				clearGame();
				stopOperation();
				startNewGame();
				break;
			default:
				break;
		}
	}

	private DdzPos getStartLordPos() {
		if (rounds == 0) {
			return randomLordPos();
		} else {
			if (lasRunPos == null) {
				return randomLordPos();
			} else {
				return lasRunPos;
			}
		}
	}

	//是否抢地主
	public void robLord(PlayerInfo player, boolean rob) {
		if (getPlayerPos(player) != currentPos) {
			logger.debug(" is not your turn to do something ");
			return;
		}
		logger.info("{} | {} | 玩家 {} 是否抢地主 {} ", this.deskId, this.gameId, player.getPlayerId(), rob);
		if (callLord) {
			if (state != DdzState.ROB_LORD) {
				logger.warn("current state is not rob lord");
				return;
			}
			doRobLord(player, rob);
		} else {
			if (state != DdzState.CALL_LORD) {
				logger.warn("current state is not call lord");
				return;
			}
			callLord(player, rob);
		}
	}

//	public void jiabei(PlayerInfo player, boolean add) {
//		if (getPlayerPos(player) != currentPos) {
//			logger.debug(" is not your turn ");
//			return;
//		}
//		getDeskInfo(player).setBonus(add);
//		msgHelper.notifyMessage(ResponseCode.DDZ_JIABEI, CommonCreator.createPBPair(getPlayerPosValue(player), add ? 1 : 0));
//		currentPos = nextPos();
//		if (currentPos == lordPos) {
//			beginDiscard();
//		} else {
//			beginJiabei();
//		}
//	}

	protected void dealLordCard(PlayerInfo player) {
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			logger.debug(" the  lord info is null");
			return;
		}
		info.setLord(true);
		logger.info("{} | {} |玩家 {} 当了地主", this.deskId, this.gameId, player.getPlayerId());
		lordPos = info.getPos();
		currentPos = lordPos;
		if (getGameType() == GameType.LZ_DDZ) {
			randomLz();
		}
		lordCards.forEach(e -> info.addHandCard(e));
		msgHelper.notifyMessage(ResponseCode.DDZ_LORD_CARD, createPBDealLordCard(getPlayerPosValue(player), PokerUtil.convert2IntList(lordCards)));
		record.addLordCardInfo(getPlayerPosValue(player), lordCards, lzValue);
		setState(DdzState.ROB_END);
	}

	private void randomLz() {
		PokerCard debugLz = ArrayPai.getInst().getDDZLzPaiForPoker(getGameType());
		if(debugLz != null){
			lzValue = debugLz.getKey();
		}else{
			List<PokerCard> cardList = GameConst.getOneCardPoolWithoutJoker();
			int size = cardList.size();
			int randomNum = Randomizer.nextInt(size);
			lzValue = cardList.get(randomNum).getKey();
		}
	}

	protected void doRobLord(PlayerInfo player, boolean rob) {
		logger.info("{} | {} |玩家 {} 抢地主 {}", this.deskId, this.gameId, player.getPlayerId(), rob);
		if (rob) {
			getDeskInfo(player).addRobLordTimes();
			msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), 1, callLord ? 1 : 0));
			if (getPlayerPos(player) == callPos) {
				dealLordCard(player);
			} else {
				currentPos = nextPos();
				setState(DdzState.ROB_LORD);
				beginChoseLord();
			}
		} else {
			msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), 0, callLord ? 1 : 0));
			if (currentPos == callPos) {
				if (pos2InfoMap.get(currentPos.prePos()).isRobLord()) {
					dealLordCard(getPlayerInfoByPos(currentPos.prePos()));
				} else {
					dealLordCard(getPlayerInfoByPos(currentPos.prePos().prePos()));
				}
			} else if (nextPos() == callPos) {
				if (!pos2InfoMap.get(currentPos.prePos()).isRobLord()) {
					dealLordCard(getPlayerInfoByPos(callPos));
				} else {
					currentPos = nextPos();
					setState(DdzState.ROB_LORD);
					beginChoseLord();
				}
			} else {
				currentPos = nextPos();
				setState(DdzState.ROB_LORD);
				beginChoseLord();
			}
		}
	}

	private boolean isPreTwoGiveUpCall(DdzPos currentPos) {
		DdzDeskInfo preDeskInfo = deskInfoMap.get(currentPos.prePos());
		if (preDeskInfo == null || !preDeskInfo.isGiveUpCall()) {
			return false;
		}
		DdzDeskInfo nextDeskInfo = deskInfoMap.get(currentPos.nextPos());
		if (nextDeskInfo == null || !nextDeskInfo.isGiveUpCall()) {
			return false;
		}
		return true;
	}

	private void callLord(PlayerInfo player, boolean rob) {
		logger.info("{} | {} |玩家 {} 叫地主 {}", this.deskId, this.gameId, player.getPlayerId(), rob);
		if (rob) {
			callPos = currentPos;
			currentPos = nextPos();
			msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), rob ? 1 : 0, callLord ? 1 : 0));            //0是叫地主1是抢地主
			callLord = true;
			setState(DdzState.ROB_LORD);
			beginChoseLord();
		} else {
			getDeskInfo(player).setGiveUpCall(true);
			currentPos = nextPos();
			if (isAllGiveupCall()) {
				msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), rob ? 1 : 0, callLord ? 1 : 0));            //0是叫地主1是抢地主
				setState(DdzState.LIUJU);
				msgHelper.notifyMessage(ResponseCode.DDZ_LIUJU, null);
			} else {
				msgHelper.notifyMessage(ResponseCode.DDZ_ROB_LORD, CommonCreator.createPBTriple(getPlayerPosValue(player), rob ? 1 : 0, callLord ? 1 : 0));            //0是叫地主1是抢地主
				setState(DdzState.CALL_LORD);
				beginChoseLord();
			}
		}
	}

	private boolean isAllGiveupCall() {
		for (DdzDeskInfo info : deskInfoMap.values()) {
			if (!info.isGiveUpCall()) {
				return false;
			}
		}
		return true;
	}

	protected DdzPos nextPos() {
		return currentPos.nextPos();
	}

	protected int getPlayerPosValue(PlayerInfo info) {
		DdzPos pos = getPlayerPos(info);
		if (pos == null) {
			return 0;
		}
		return pos.getValue();
	}

	protected DdzPos getPlayerPos(PlayerInfo info) {
		for (Map.Entry<PlayerInfo, DdzDeskInfo> entry : deskInfoMap.entrySet()) {
			if (entry.getKey() == info) {
				return entry.getValue().getPos();
			}
		}
		return null;
	}

	private void beginDiscard() {
		setState(DdzState.DISCARD);
		//但是如果他是托管的玩家， 这个时候就要换下一个了啊
		DdzDeskInfo info = pos2InfoMap.get(currentPos);
		if (info == null) {
			logger.warn("player desk info is null int begin discard ");
			return;
		}
		if(info.getTimeoutTimes() == 1){
			stateRemainTime = 8;
		}
		msgHelper.notifyMessage(ResponseCode.DDZ_DISCARD_TURN, CommonCreator.createPBPair(currentPos.getValue(),stateRemainTime));

		logger.info("{} | {} | 轮到当前位置{} 出牌", this.deskId, this.gameId, currentPos);

		if (info.isAutoDiscard()) {
			info.setAutoDiscardFuture(LogicActorManager.registerOneTimeTask(3000, () -> autoDiscard(getPlayerInfoByPos(currentPos),0), getDeskId()));
		}
	}

	protected void beginChoseLord() {
		if (callLord && pos2InfoMap.get(currentPos).isGiveUpCall()) {
			robLord(getPlayerInfoByPos(currentPos), false);
		} else {
			msgHelper.notifyMessage(ResponseCode.DDZ_WHO_LORD, CommonCreator.createPBPair(currentPos.getValue(), callLord ? 1 : 0));
		}
	}

//	private void beginJiabei() {
//		msgHelper.notifyMessage(ResponseCode.DDZ_WHO_JIABEI, CommonCreator.createPBInt32(currentPos.getValue()));
//	}


	private DdzPos randomLordPos() {
		int playerSize = getPlayerList().size();
		int randomNm = Randomizer.nextInt(playerSize) + 1;
		return DdzPos.getByValue(randomNm);
	}

	private PlayerInfo getCurrentOperationPlayer() {
		return getPlayerInfoByPos(currentPos);
	}

	protected PlayerInfo getPlayerInfoByPos(DdzPos pos) {
		for (Map.Entry<PlayerInfo, DdzDeskInfo> entry : deskInfoMap.entrySet()) {
			if (entry.getValue() == pos2InfoMap.get(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	protected DdzDeskInfo getDeskInfo(PlayerInfo player) {
		return deskInfoMap.get(player);
	}

	private List<DdzDeskInfo> getAllDeskInfo() {
		return new ArrayList<>(deskInfoMap.values());
	}

	private void dealAllPlayerCard() {
		Queue<PokerCard> mixedInitCard = PokerUtil.mixAllCard(getGameType());
		ConfMatchCard confMatchCard = CoupleRoomInfoProvider.getInst().getConfMatchCard(getConfId());
		if(confMatchCard != null && confMatchCard.isOpen()){
			logger.error("斗地主{}开启配牌",getConfId());
			mixedInitCard = PokerMatchCardUtil.matchDdzCard(gameId, mixedInitCard, confMatchCard, getAllDeskInfo().size());
		}
		Queue<PokerCard> mixedCard = mixedInitCard;
		
		getAllDeskInfo().forEach(e -> {
			for (int i = 0; i < 17; i++) {
				e.addHandCard(mixedCard.poll());
			}
			logger.info("{} | {} |位置 {} 手牌为 {}", this.deskId, this.gameId, e.getPositionValue(), JsonUtil.getJsonString(e.getHandCards()));
		});
		for (int i = 0; i < 3; i++) {
			lordCards.add(mixedCard.poll());
		}
		logger.info("{} | {} |地主牌为:{}", this.deskId, this.gameId, JsonUtil.getJsonString(lordCards));
		deskInfoMap.values().forEach(e -> record.addHandCards(e.getPositionValue(), PokerUtil.convert2IntList(e.getHandCards())));
		remainCards.addAll(mixedCard);
	}

	public Map<PlayerInfo, DdzDeskInfo> getDeskInfoMap() {
		return deskInfoMap;
	}

	@Override
	public void playerReady(PlayerInfo player) {
		if (state != DdzState.READY) {
			return;
		}

		logger.info("{} | {} |玩家 {} 准备", this.deskId, this.gameId, player.getPlayerId());

		deskInfoMap.get(player).setReady(true);
		msgHelper.notifyMessage(ResponseCode.COUPLE_GAME_READY, CommonCreator.createPBInt32(getPlayerPosValue(player)));
		boolean allReady = true;
		for (DdzDeskInfo info : pos2InfoMap.values()) {
			if (!info.isReady()) {
				allReady = false;
			}
		}
		if (allReady) {
			startNewGame();
		}

	}

	@Override
	public List<PlayerInfo> getPlayerList() {
		return new ArrayList<>(deskInfoMap.keySet());
	}

	@Override
	public void playerReLogin(PlayerInfo player) {
		if (state == DdzState.END) {
			playerLeave(player);
			return;
		}
		DdzDeskInfo info = deskInfoMap.get(player);
		if (info == null) {
			return;
		}
		// 若重登录处于结束状态,则退出
		if (state == DdzState.END) {
			playerLogout(player);
		}
		info.setLeave(false);
		getPlayerList().forEach(e -> e.write(ResponseCode.COUPLE_PLAYER_ENTER_DESK, CommonCreator.createPBInt32(player.getPlayerId())));
		player.write(ResponseCode.DDZ_RESET_GAME, createPBDdzResetGame(player));
	}

	private Ddz.PBDealLordCard createPBDealLordCard(int pos, List<Integer> cardList) {
		Ddz.PBDealLordCard.Builder builder = Ddz.PBDealLordCard.newBuilder();
		builder.setPos(pos);
		cardList.forEach(e -> builder.addCardList(e));
		if (getGameType() == GameType.LZ_DDZ) {
			builder.setLzValue(lzValue);
		}
		return builder.build();
	}

	private Ddz.PBDdzResetGame createPBDdzResetGame(PlayerInfo player) {
		//如果桌子结束， 踢出玩家
		Ddz.PBDdzResetGame.Builder builder = Ddz.PBDdzResetGame.newBuilder();
		builder.setPlayerList(CoupleCreator.createPBPlayerInfoListDdz(deskInfoMap));
		builder.setRoomId(getConfId());
		DdzDeskInfo deskInfo = getDeskInfo(player);
		if (deskInfo == null) {
			logger.warn("the player inf ois null and reset game ");
			return null;
		}
		deskInfo.getHandCards().forEach(e -> builder.addSelfCards(e.getKey()));
		builder.setLordPos(lordPos == null ? 0 : lordPos.getValue());
		builder.setBonus(getFinalBonusCount(true, false));                //第一个参数随便填
		builder.setCurrentRemainTime(stateRemainTime);
		builder.setCurrentState(state.getId());
		if (getGameType() == GameType.COUPLE_DDZ) {
			builder.setRangpaiCount(getRobLordTimes());
		}
		if (getGameType() == GameType.LZ_DDZ) {
			builder.setLaiziValue(lzValue);
		}
		lordCards.forEach(e -> builder.addButtomCards(e.getKey()));
		deskStop.forEach(e -> e.getCardList().forEach(f -> builder.addDeskTopList(f.getKey())));
		deskInfoMap.forEach((e, f) -> {
			builder.addOtherCardNum(CommonCreator.createPBPair(getPlayerPosValue(e), f.getHandCards().size()));
			List<Integer> result = new ArrayList<>();
			if (f.getLastRoundCards() != null && f.getLastRoundCards().size() <= 0) {
				result.add(0);
			} else {
				result.add(1);
			}
			if (f.getLastRoundCards() != null) {
				result.addAll(PokerUtil.convert2IntList(f.getLastRoundCards()));
			}
			builder.addOtherLastDiscard(CommonCreator.createPBIntIntList(getPlayerPosValue(e), result));
		});
		deskInfoMap.values().forEach(e -> builder.addTuoguanState(CommonCreator.createPBPair(e.getPositionValue(), e.isTuoguan() ? 1 : 0)));
		builder.setCurrentPos(currentPos.getValue());
		return builder.build();
	}

	@Override
	public void playerLogout(PlayerInfo player) {
		playerLeave(player);
	}

	@Override
	protected void playerMoneyChangeHook(PlayerInfo info) {
		syncAllPlayerMoney();
	}

	@Override
	protected void onGameEnd() {
		stopOperation();
		stopOverMyselfFuture();
		clearGame();
		setState(DdzState.END);
		//斗地主一局结束一定解散
		DeskMgr.getInst().removeDesk(this);
	}


	@Override
	protected boolean destroyDesk() {
		for (DdzDeskInfo info : deskInfoMap.values()) {
			if (info.isLeave()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void playerLeave(PlayerInfo player) {
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			logger.warn(" player desk info is null");
			return;
		}
		logger.info("{} | {} | 玩家 {} 离开", this.deskId, this.gameId, player.getPlayerId());
		info.setLeave(true);
		
		if(!isPersonal()){
			//所有人都离开了 不管游戏是否进行都解散
			if (isAllPlayerLeave()) {
				DeskMgr.getInst().removeDesk(this);
				return;
			} 
			//游戏未开始只要一个人离开都解散
			if (state == DdzState.END || state == DdzState.READY) {
				DeskMgr.getInst().removeDesk(this);
				return;
			} 
		}
		info.setLeave(false);
		msgHelper.notifyMessage(ResponseCode.LOBBY_PLAYER_LEAVE, CommonCreator.createPBPair(getPlayerPosValue(player), 0));
		info.setLeave(true);
	}

	@Override
	public boolean isPlayerLeave(PlayerInfo player) {
		DdzDeskInfo info = getDeskInfo(player);
		if (info == null) {
			return true;
		}
		return info.isLeave();
	}

	@Override
	protected void disbandDesk() {

	}

	@Override
	public GameType getGameType() {
		return GameType.DDZ;
	}

	protected DdzMsgHelper msgHelper = new DdzMsgHelper();

	public class DdzMsgHelper {

		public void notifyGameStart() {
			deskInfoMap.forEach((e, f) -> {
				e.write(ResponseCode.DDZ_DEAL_CARD, CommonCreator.createPBInt32List(f.getHandCardKeyList()));
			});
		}

		public void notifyMessage(ResponseCode code, MessageLite message) {
			for (PlayerInfo player : deskInfoMap.keySet()) {
				player.write(code, message);
			}
		}
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	@Override
	public void recordPlayerTaxInfo(Object detail, int roomId) {
		DdzDetail ddz_detail = (DdzDetail) detail;
		for (DdzDetailBase one : ddz_detail.getRecords()) {
			if (0 >= one.getTax()) {
				continue;
			}

			TaxRecordUtil.recordPlayerTaxInfoToDB(ddz_detail.getType(), one.getPlayerId(), roomId, one.getTax(), one.getChannel_id(), one.getPackage_id(), one.getDevice());
		}
	}


	@Override
	public void destroy() {
		isDestroyed.set(true);
		stopOperation();
		stopOverMyselfFuture();
		for (DdzDeskInfo info : deskInfoMap.values()) {
			info.stopAutoDiscard();
		}
		deskInfoMap.clear();
		pos2InfoMap.clear();
	}


	@Override
	public void disBankDeskTimeOver() {
		
	}


	@Override
	public void enterPlayer(PlayerInfo player) {
		
	}


	@Override
	public void playerWantContinue(PlayerInfo playerInfo) {
		// TODO Auto-generated method stub
		
	}
}
