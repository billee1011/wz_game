package logic.poker.zjh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import common.LogHelper;
import config.CoupleRoomInfoProvider;
import config.JsonUtil;
import config.bean.ConfMatchCard;
import config.bean.CoupleRoom;
import config.bean.PersonalConfRoom;
import config.bean.RoomConfig;
import config.provider.PersonalConfRoomProvider;
import data.MoneySubAction;
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
import logic.record.detail.OnePosDetail;
import logic.record.detail.ZjhDetail;
import logic.record.zjh.ZjhRecord;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.Zjh;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;

/**
 * Created by hhhh on 2017/3/24.
 */
public class ZjhDesk extends PokerDesk {

	private static final Logger logger = LoggerFactory.getLogger(ZjhDesk.class);

	private Map<PlayerInfo, ZjhDeskInfo> deskInfoMap = new HashMap<>();
	private Map<ZjhPostion, ZjhDeskInfo> posInfoMap = new HashMap<>();
	private int gameRounds; // 轮数
	private int rounds;//当前回合
	private int maxRounds;//最大回合
	private int minPost;    //底分/前注
	private int enterTimes;  // 私房倍数
	private int compareRound;    //拼牌限制回合
	private int lookRound;  //看牌回合限制
	private int fullRound;  //满压回合
	private List<Integer> raiseRound; //加注倍数

	private int post;//奖池
	private int lastPost = 0;    //前一个玩家的下注金额
	private ZjhPostion zhuangjia;  //庄家
	private int zhuangId;          //庄家id
	private ZjhPostion firstPos;   //一手位
	private ZjhPostion currentPos; //当前位置
	private GameState gameState;   //游戏状态
	private long gameId;
	private int createId;
	private int taxRate;//系统税率
	private ScheduledFuture<?> operationFuture;
	private int startTime;
	//操作时间
	private long wait_game_start = 2 * 1000;
	private long waitTime = 15 * 1000;
	private long disbankTime = 8 * 1000;
	private List<Integer> postList = new ArrayList<>();
	private int lastOperatingTime;    //上一次操作时间
	private int over_time;
	private int recordId;
	private ZjhRecord record;
	private boolean isFull = false; //是否压满
	private boolean isAllIn = false;
	private float fullCoin = 0;   //最大可下注金额
	private float maxPots;//	当前回合最大可下金额

	private int resultType;     //赢的类型
	private int winPos;         //赢家位置
	private int leaveCount = 1; //踢人次数

	protected List<Pair<Integer, Map<ZjhPostion, Integer>>> scoreList = new ArrayList<>();
	protected List<Integer> recordIdList = new ArrayList<>();

	// 个人扣税日志
	protected ZjhDetail detail = null;

	// 房间配置
	private RoomConfig config = null;
	private CoupleRoom room = null;
	private PersonalConfRoom personalConfRoom = null;

	/**
	 * 私房创建桌子
	 *
	 * @param deskId
	 * @param playerList
	 */
	public ZjhDesk(int createId, int maxGameRound, int deskId, List<PlayerInfo> playerList, int minPost, int enterTimes, int maxRounds, int compareRound, int lookRound, int fullRound) {
		super(deskId,0);
		setPersonal(true);
		// 私房配置
		personalConfRoom = PersonalConfRoomProvider.getInst().getPersonalConfRoomById(GameType.ZJH.getValue());
		
		this.gameRounds = 0;
		this.createId = createId;
		this.minPost = minPost;
		this.enterTimes = enterTimes;
		this.compareRound = compareRound;
		this.maxRounds = maxRounds == 0 ? 20 : maxRounds;
		this.lookRound = lookRound == 0 ? 2 : lookRound;
		this.fullRound = fullRound == 0 ? 3 : fullRound;
		this.raiseRound = new ArrayList<>();
		raiseRound.add(2);
		raiseRound.add(5);
		raiseRound.add(10);
		this.taxRate = personalConfRoom == null ? 2 : personalConfRoom.getTax_rate();
		this.record = new ZjhRecord(deskId, minPost, 0, maxRounds, compareRound);

		initZjhDeskInfo(playerList);
		gameState = GameState.WAIT;
		overMyself = LogicActorManager.registerOneTimeTask(GameConst.PERSONEL_OVER_MYSELF_TIME, () -> disBankDeskTimeOver(), getDeskId());
	}

	/**
	 * 匹配房创建桌子
	 *
	 * @param deskId
	 * @param playerList
	 */
	public ZjhDesk(int deskId, int roomId, List<PlayerInfo> playerList) {
		super(deskId,roomId);
		// 匹配房配置
		config = CoupleRoomInfoProvider.getInst().getConfRoomEx(getConfId());
		room = CoupleRoomInfoProvider.getInst().getRoomConf(getConfId());

		this.gameRounds = 0;
		this.minPost = (int) (room.getBase() * 100);
		this.maxRounds = config.getMax_round();
		this.compareRound = config.getCompare_round();
		this.lookRound = config.getLook_round();
		this.fullRound = config.getFull_round();
		this.raiseRound = config.getRaise();
		this.taxRate = room.getTax_rate();
		this.record = new ZjhRecord(getConfId(), minPost, 0, maxRounds, compareRound);
		
		initZjhDeskInfo(playerList);
		gameState = GameState.WAIT;
		operationFuture = LogicActorManager.registerOneTimeTask(wait_game_start, () -> allPlayerIsReady(), getDeskId());
	}

	/**
	 * 加入人员
	 *
	 * @param player
	 */
	public void enterPlayer(PlayerInfo player) {
		if (deskInfoMap.keySet().contains(player)) {
			logger.error("玩家{}已经在桌子里", player.getPlayerId());
			return;
		}

		ZjhPostion pos = ZjhPostion.getFree(posInfoMap.keySet());
		if (pos == null) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_FULL));
			logger.error("{}|{}该桌子已经满了,走吧 {}", this.deskId, this.gameId, player.getPlayerId());
			return;
		}

		ZjhDeskInfo info = new ZjhDeskInfo(pos, player);

		if (GameState.WAIT != gameState) {
			info.setWatch(true);
		}
		player.setPosition(info.getPositionValue());

		logger.info("玩家{}进入到桌子{},是否围观:{}", player.getPlayerId(), this.getDeskId(), info.isWatch());

		deskInfoMap.put(player, info);
		posInfoMap.put(info.getPostion(), info);
		DeskMgr.getInst().registerDesk(player.getPlayerId(), this);

		zjhMsgeHelper.notifyMessageNoSelf(ResponseCode.ZJH_ENTER_DESK, CoupleCreator.createZjhPlayerInfo(player, info), player);
		player.write(ResponseCode.COUPLE_ENTER_DESK, CoupleCreator.createPBPlayerInfoListZjh(deskInfoMap));
	}

	/**
	 * 初始化玩家信息
	 *
	 * @param playerList
	 */
	public void initZjhDeskInfo(List<PlayerInfo> playerList) {
		for (PlayerInfo player : playerList) {
			ZjhDeskInfo info = null;
			if(isPersonal()){
				info = new ZjhDeskInfo(ZjhPostion.getByValue(player.getPosition()), player);
			}else{
				info = new ZjhDeskInfo(ZjhPostion.getFree(posInfoMap.keySet()), player);
			}
			player.setPosition(info.getPositionValue());
			logger.info("{}|{}|初始化玩家{}位置{", this.deskId, this.gameId, player.getPlayerId(), info.getPositionValue());
			deskInfoMap.put(player, info);
			posInfoMap.put(info.getPostion(), info);
		}
	}

	private void serZhuang(ZjhPostion postion){
		zhuangjia = postion;
		zhuangId = getPlayerByPos(postion).getPlayerId();
	}
	
	/**
	 * 初始化庄家
	 */
	private void selectZhuang() {
		if (winPos != 0) {
			ZjhPostion zhuang = ZjhPostion.getByValue(winPos);
			if (zhuang != null) { //  現在玩家離開不會清除 winPos 所以要判斷 ZjhPostion 是否合法
				ZjhDeskInfo info = getDeskInfo(zhuang);
				if (info != null && !info.isWatch() && info.isReady()) {
					serZhuang(zhuang);
					firstPos = getNextPos(zhuangjia.getNextPos());
					currentPos = ZjhPostion.getByValue(firstPos.getValue());
					record.setZhuang_pos(zhuangjia.getValue());
					return;
				}
			}
			winPos = 0;
		}

		List<ZjhDeskInfo> list = new ArrayList<>();
		deskInfoMap.values().forEach(e -> {
			if (!e.isWatch()) {
				list.add(e);
			}
		});

		ZjhDeskInfo zhuangInfo = list.get(Randomizer.nextInt(list.size()));
		serZhuang(zhuangInfo.getPostion());
		firstPos = getNextPos(zhuangjia.getNextPos());
		currentPos = ZjhPostion.getByValue(firstPos.getValue());
		logger.info("选庄成功,位置为{},第一个操作位置为{}", zhuangjia, firstPos);
		record.setZhuang_pos(zhuangjia.getValue());
	}

	/**
	 * 重置游戏数据
	 */
	private void resetGameStaus() {
		record.clearStatus(getConfId());
		getDeskInfoMap().forEach((e, f) -> {
			f.clearStatus();
		});
		this.post = 0;
		postList.clear();
		this.lastPost = 0;
		this.rounds = 0;
		this.isFull = false;
		this.isAllIn = false;
		this.firstPos = null;
		this.fullCoin = 0;
	}

	   /** 获取参与游戏人数 */
    private int getJoinPlayerCount(){
    	int count = 0;
    	for (ZjhDeskInfo info : getDeskInfoMap().values()) {
    		if(!info.isWatch()){
    			count++;
    		}
		}
    	return count;
    }
    
	/**
	 * 发牌
	 */
	private void dealCard() {
		Queue<PokerCard> mixedInitCard = PokerUtil.mixAllCard(GameType.ZJH);
		int roomId = getConfId();
		if(isPersonal()){
			roomId = -getGameType().getValue();
		}
		ConfMatchCard confMatchCard = CoupleRoomInfoProvider.getInst().getConfMatchCard(roomId);
		if(confMatchCard != null && confMatchCard.isOpen()){
			logger.error("扎金花{}开启配牌",getConfId());
			mixedInitCard = PokerMatchCardUtil.matchZjhCard(gameId, mixedInitCard, confMatchCard, getJoinPlayerCount());
		}
		Queue<PokerCard> mixedCard = mixedInitCard;
		
		getDeskInfoMap().forEach((e, f) -> {
			if (!f.isWatch() && f.isReady()) {
				f.getHandCards().clear();
				for (int i = 0; i < 3; i++) {
					f.addHandCards(mixedCard.poll());
				}
				logger.info("{}|{}|玩家{}获得手牌 {}", this.deskId, this.gameId, e.getPlayerId(), JsonUtil.getJsonString(f.getHandCards()));

				ZjhCompareStrategy strategy = ZjhCardCompare.getInst().checkTypeAndValue(f.getHandCards());
				f.setStrategy(strategy);
			}
		});
		record.dealCard(deskInfoMap);
	}

	/**
	 * 游戏正式开始之前下注
	 */
	private void beForeGame() {
		//前注
		deskInfoMap.values().forEach(e -> {
			if (!e.isWatch()) {
				recordPost(e, minPost, true);
			}
		});

		lastPost = minPost;

		//第一个玩家10s不出牌默认放弃
		PlayerInfo info = getPlayerByPos(currentPos);
		callNextGiveUp(info);
	}
	
	/**
	 * 私房初始化战绩
	 */
	private void initPersonalScore() {
		Map<ZjhPostion, Integer> scoreHis = new HashMap<>();
		for (ZjhPostion post : posInfoMap.keySet()) {
			scoreHis.put(post, 0);
		}
		scoreList.add(new Pair<>(startTime, scoreHis));
	}

	/**
	 * 开始游戏
	 */
	public void startGame() {
		stopOperationFuture();
		logger.info("{}|{}开始诈金花游戏 {}", this.deskId, this.gameId);
		gameRounds++;
		gameId = geneGameNo();
		detail = new ZjhDetail(getGameType() == null ? 0 : getGameType().getValue());
		gameState = GameState.START;
		record.initPlayer(deskInfoMap);
		startTime = MiscUtil.getCurrentSeconds();
		selectZhuang();
		dealCard();
		zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_DESK_INFO, zjhMsgeHelper.deskInfo(minPost, maxRounds, compareRound, lookRound, fullRound, raiseRound));
		zjhMsgeHelper.notifyGameStart();
		beForeGame();
		calculateMaxFull();
	}

	/**
	 * 下注记录
	 *
	 * @param value
	 * @param isChange
	 */
	private void recordPost(ZjhDeskInfo info, int value, boolean isChange) {
		logger.info("{}|{}|桌子位置{}下注{}", this.deskId, this.gameId, info.getPostion(), value);
		info.addPost(value);
		postList.add(value);
		this.post += value;
		if (isChange) {
			lastPost = value;
		}
	}

	@Override
	public List<PlayerInfo> getPlayerList() {
		return new ArrayList<>(deskInfoMap.keySet());
	}

	@Override
	public GameType getGameType() {
		return GameType.ZJH;
	}

	@Override
	public void playerLogout(PlayerInfo player) {
//		playerLeave(player);
		ZjhDeskInfo info = getDeskInfo(player);
		if (info == null) {
			return;
		}
		info.setLeave(true);
		logger.info("{}|{}|玩家{}logout .....", this.deskId, this.gameId, player.getPlayerId());
		playerLeave(player);
	}

	@Override
	public boolean isAllPlayerLeave() {
		for (ZjhDeskInfo info : deskInfoMap.values()) {
			if (!info.isLeave() /*&& !info.isWatch()*/) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void playerLeave(PlayerInfo player) {
		ZjhDeskInfo info = getDeskInfo(player);
		if (info == null) {
			return;
		}
		info.setLeave(true);
		
		boolean canLeave = false;
		if(!isPersonal()){
			if(isAllPlayerLeave() && gameState != GameState.START){ 
				deskInfoMap.keySet().forEach(e ->{
					DeskMgr.getInst().removePlayer(e,true);
				});
				onGameEnd();
				DeskMgr.getInst().removeDesk(this);
				return;
			}
			//可以解除桌子
			logger.info("{}|{}|玩家{}离开房间 :", this.deskId, this.gameId, player.getPlayerId());
			//弃牌玩家离开房间立即计算
			if ((info.isGiveUp() || info.isLose()) && gameState == GameState.START) {
				int pre_coin = player.getCoin();
				player.updateCoin(info.getPost(), false);
				detail.addOneRecord(player.getPlayerId(), info.getPositionValue(), 0, 0, player.getChannel_id(), player.getPackage_id(), player.getDevice(), pre_coin, player.getCoin(), player.getIp());
				player.write(RequestCode.LOG_MONEY.getValue(), LogHelper.logLoseMoney(player.getPlayerId(), getLogMoneyType(false), getGameType().getValue(), info.getPost(), pre_coin, player.getCoin(), player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));
				canLeave = true;
			}
			if (info.isWatch() || gameState == GameState.WAIT || gameState == GameState.OVER) {
				canLeave = true;
			}
		}
		info.setLeave(false);
		if (canLeave) {
			zjhMsgeHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(info.getPostion().getValue()));
			info.setLeave(true);
			leaveDesk(player);
		} else {
			zjhMsgeHelper.notifyMessage(ResponseCode.LOBBY_PLAYER_LEAVE, CommonCreator.createPBPair(info.getPostion().getValue(), 0));
			info.setLeave(true);
		}
	}

	@Override
	public void playerReady(PlayerInfo player) {
		if (gameState != GameState.WAIT) {
			return;
		}
		if (!isPlayerMoneyEnough(player)) {
			player.write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
			return;
		}
		
		ZjhDeskInfo deskInfo = getDeskInfo(player);
		logger.info("deskInfo is ready " + deskInfo.isReady());
		if (deskInfo.isReady()) {
			return;
		}
		logger.info("{}|{}玩家{}准备", this.deskId, this.gameId, player.getPlayerId());

		deskInfo.setReady(true);
		deskInfo.setWatch(false);
		zjhMsgeHelper.notifyMessage(ResponseCode.COUPLE_GAME_READY, CommonCreator.createPBInt32(deskInfo.getPostion().getValue()));

		if (isPersonal()) {
			logger.info("deskInfo is isAllPlayerReady " + isAllPlayerReady());
			if (isAllPlayerReady()) {
				startGame();
			}
		}
	}

	public boolean isAllPlayerReady() {
		for (ZjhDeskInfo info : getAllDeskInfo()) {
			if (!info.isReady()) {
				return false;
			}
		}
		return true;
	}

	private void allPlayerIsReady() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		stopOperationFuture();
		int count = 0;
		
		List<ZjhDeskInfo> leaveList = new ArrayList<>();
		for (PlayerInfo player : getPlayerList()) {
			ZjhDeskInfo info = getDeskInfo(player);
			if (info.isReady()) {
				count++;
			} else {
				if (info.getCallTime() > 2) {
					player.write(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(player.getPlayerId(), 0));
					leaveList.add(info);
					info.setLeave(true);
					continue;
				}
				info.setWatch(true);
				info.addCallTime();
			}
		}
		if (isAllPlayerLeave()) {
			DeskMgr.getInst().removeDesk(this);
			return;
		}
		for (ZjhDeskInfo info : leaveList) {
			zjhMsgeHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(info.getPostion().getValue()));
			leaveDesk(info.playInfo);
		}
	    
		if (count >= 2) {
			startGame();
		} else {
			operationFuture = LogicActorManager.registerOneTimeTask(wait_game_start, () -> allPlayerIsReady(), getDeskId());
		}
	}

	@Override
	public void playerReLogin(PlayerInfo player) {
		logger.info("{}|{}|玩家{}重新登录到诈金花桌子", this.deskId, this.gameId, player.getPlayerId());
		ZjhDeskInfo info = getDeskInfo(player);
		info.setLeave(false);
		if (!info.isWatch()) {
			zjhMsgeHelper.notifyMessage(ResponseCode.COUPLE_PLAYER_ENTER_DESK, CommonCreator.createPBInt32(player.getPlayerId()));
		}
		player.write(ResponseCode.ZJH_DESK_INFO, zjhMsgeHelper.deskInfo(minPost, maxRounds, compareRound, lookRound, fullRound, raiseRound));
		resetGameDetail(player, info);
		logger.info("reset game " + gameState.getValue());
		getAllDeskInfo().forEach(e -> {
			logger.info(e.isGiveUp() + "," + e.isLeave() + "," + e.isWatch() + "," + e.isReady() + "," + e.isOpen() + "," + e.isLose());
		});
	}

	private void resetGameDetail(PlayerInfo player, ZjhDeskInfo info) {
		if (gameState == GameState.OVER) {
			Zjh.PBZJHResult.Builder builder = Zjh.PBZJHResult.newBuilder();
			builder.setResultType(resultType);
			builder.setWinPos(winPos);
			builder.setBeforeTax(post);
			builder.setAfterTax(getWinMoney());
			builder.setOverTime(MiscUtil.getCurrentSeconds() - over_time);
			player.write(ResponseCode.ZJH_RESET_GAME, zjhMsgeHelper.createResetGame(info, getLostTime(), currentPos.getValue(), lastPost, postList, maxRounds + 1, gameRounds, firstPos.getValue(), zhuangjia.getValue(), getDeskInfoMap(), builder.build()));
		} else {
			if (gameState == GameState.WAIT) {
				player.write(ResponseCode.ZJH_RESET_GAME, zjhMsgeHelper.createResetGame(info, getLostTime(), 0, lastPost, postList, 0, gameRounds, 0, 0, getDeskInfoMap(), null));
			} else {
				player.write(ResponseCode.ZJH_RESET_GAME, zjhMsgeHelper.createResetGame(info, getLostTime(), currentPos.getValue(), lastPost, postList, rounds + 1, gameRounds, firstPos.getValue(), zhuangjia.getValue(), getDeskInfoMap(), null));
			}

		}
	}

	@Override
	protected void playerMoneyChangeHook(PlayerInfo info) {
		syncAllPlayerMoney();
	}

	@Override
	protected Map<Integer, Integer> getAllPlayerMoney() {
		Map<Integer, Integer> result = new HashMap<>();
		for (Map.Entry<PlayerInfo, ZjhDeskInfo> entry : deskInfoMap.entrySet()) {
			result.put(entry.getValue().getPositionValue(), entry.getValue().getCoin());
		}
		return result;
	}

	// 判断是不是每个人都够钱
	private boolean isAllEnougthCoin() {
		return deskInfoMap.values().stream().filter(e -> e.isGameIng()).allMatch(
				e -> e.getCoin() >= getMaxOnePots() * (maxRounds - (e.getRound() + 1)) * 2
		);
	}

	/**
	 * 压满
	 *
	 * @param player
	 */
	public void fullPressure(PlayerInfo player) {
		if (gameState != GameState.START) {
			logger.debug("game is over ");
			return;
		}
		logger.debug("fullPressure " + player.getPlayerId());
		ZjhDeskInfo deskInfo = getDeskInfo(player);
		if (currentPos != deskInfo.getPostion()) {
			logger.debug(" is not your turn to discard");
			return;
		}
		if (rounds < fullRound - 1) {
			logger.debug("can't look at now " + rounds + "," + lookRound);
			return;
		}
		logger.info("{}|{}|玩家{}压满", this.deskId, this.gameId, player.getPlayerId());
		deskInfo.cleanPassiveTime();
		float needCoin = 0;
		if (!isFull) {
			// 判断是不是所有的人钱都够
			if (isAllEnougthCoin()) {
				logger.info("{}|{}|所有人都够钱，满压", this.deskId, this.gameId);
				needCoin = getMaxOnePots() * getHasRounds() * getOpenMultiple(deskInfo);
			} else {
				// 计算当前最大的押注金
//				logger.info("{}|{}|全压出发重新计算最大的单注", this.deskId, this.gameId);
//				calculateMaxFull();

				fullCoin = maxPots;
				needCoin = maxPots * getOpenMultiple(deskInfo);
				logger.info("{}|{}|有人不够钱满压，只压最少的{}", this.deskId, this.gameId, fullCoin);
			}
		} else {
			// 根据满压的值计算满压金额
			// 若fullCoin == 0 说明大家钱都够，走钱够的流程
			// 若fullCoin !=0,则记录的是满压的最大值
			if (fullCoin == 0) {
				//（房间底分x最大加注倍数）x该玩家剩余轮数
				needCoin = getMaxOnePots() * getHasRounds() * getOpenMultiple(deskInfo);
				logger.info("{}|{}|玩家{}全压钱够，需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
			} else {
				needCoin = fullCoin * getOpenMultiple(deskInfo);

				if (deskInfo.getCoin() <= needCoin) {
					needCoin = deskInfo.getCoin();
				}
				logger.info("{}|{}|玩家{}全压钱不够，需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
			}
		}

		//最多只能下最少人的一半金额
		isFull = true;
		deskInfo.setFull(true);
		stopOperationFuture();
		deskInfo.setRound(rounds);
		logger.info("{}|{}|玩家 {}设置轮数 {}", this.deskId, this.gameId, player.getPlayerId(), rounds);
		boolean nowRoundEnd = isNowRoundEnd();
		recordPost(deskInfo, (int) needCoin, false);
		record.addPots(deskInfo.getPositionValue(), (int) needCoin, rounds + 1, lastPost);
		int calRounds = rounds + 1;
		checkRounds();
		if (!isAllFull() && !isMaxRound()) {
			// 获取全压的钱
			int needFullCoin = getNeedFullCoin(currentPos);
			zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_FULL_PRESSURE, zjhMsgeHelper.createTriple(deskInfo.getPostion().getValue(), (int) needCoin, currentPos.getValue(), rounds + 1, needFullCoin));
		} else { // 都全压 或者 轮数到  都要结算   
			zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_FULL_PRESSURE, zjhMsgeHelper.createTriple(deskInfo.getPostion().getValue(), (int) needCoin, 0, calRounds, 0));
			logger.info("{}|{}|进入比牌结算 isAllFull:{},isMaxRound:{}", this.deskId, this.gameId, isAllFull(), isMaxRound());
			calculationWiner(calRounds);
			return;
		}
		// 孤注一掷本轮需要开牌
		if (isAllIn && nowRoundEnd) {
			logger.info("{}|{}|进入结算 isAllIn:{}", this.deskId, this.gameId, isAllIn && nowRoundEnd);
			stopOperationFuture();
			calculationWiner(calRounds);
			return;
		}
		callNextGiveUp(getPlayerByPos(currentPos));
	}

	private int getNeedFullCoin(ZjhPostion postion) {
		ZjhDeskInfo deskInfo = getDeskInfo(postion);
		float needCoin = 0;
		if (fullCoin == 0) {
			//（房间底分x最大加注倍数）x该玩家剩余轮数
			needCoin = getMaxOnePots() * getHasRounds() * getOpenMultiple(deskInfo);
			logger.info("{}|{}|计算玩家{}全压钱够，需要下注{}", this.deskId, this.gameId, deskInfo.playInfo.getPlayerId(), needCoin);
		} else {
			needCoin = fullCoin * getOpenMultiple(deskInfo);

			if (deskInfo.getCoin() <= needCoin) {
				needCoin = deskInfo.getCoin();
			}
			logger.info("{}|{}|计算玩家{}全压钱不够，需要下注{}", this.deskId, this.gameId, deskInfo.playInfo.getPlayerId(), needCoin);
		}
		return (int) needCoin;
	}

	/**
	 * 计算单注能下的最大金额
	 */
	private void calculateMaxFull() {
		PlayerInfo p = deskInfoMap.keySet().stream().filter(e -> getDeskInfo(e).isGameIng()).sorted((i1, i2) -> (
				getDeskInfo(i1).getCoin() > getDeskInfo(i2).getCoin() ? 1 : -1)).collect(Collectors.toList()).get(0);
		maxPots = getDeskInfo(p).getCoin() / 2f;
		logger.info("{}|{}|当前回合数为{},当前最大可下注金额为{}", this.deskId, this.gameId, rounds, maxPots);
	}


	/**
	 * 亮牌
	 *
	 * @param player
	 */
	public void showCars(PlayerInfo player) {
		logger.info("{}|{}|玩家{}亮牌", this.deskId, this.gameId, player.getPlayerId());
		ZjhDeskInfo info = this.getDeskInfo(player);
		info.setPush(true);
		info.cleanPassiveTime();
		zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_SHOW_CARDS, zjhMsgeHelper.handCards(info.getPositionValue(), info.getStarategyValue(), info.getHandCardKeyList()));
	}

	//拼牌大小
	public void compareCards(PlayerInfo player, int value) {
		if (gameState != GameState.START) {
			logger.debug("game is over ");
			return;
		}
		ZjhDeskInfo infoA = this.getDeskInfo(player);
		if (rounds < compareRound - 1) {
			logger.debug("can not compare card now" + rounds + "," + compareRound);
			return;
		}
		if (currentPos != infoA.getPostion()) {
			logger.debug(" is not your turn to discard");
			return;
		}
		infoA.cleanPassiveTime();
		int needCoin = infoA.isOpen() ? lastPost * 2 : lastPost;
		needCoin *= 2;
		if (infoA.getCoin() < needCoin) {
			logger.debug("coin is not enough!");
			return;
		}
		ZjhDeskInfo infoB = this.getDeskInfo(ZjhPostion.getByValue(value));
		if (infoB == null || infoB.isWatch()) {
			logger.debug("pos:" + value + " no people or people is watch:" + infoB.isWatch());
			return;
		}
		stopOperationFuture();
		//记录钱
		recordPost(infoA, needCoin, false);
		logger.debug("{}|{}|玩家{}比牌 ,位置为 {}", this.deskId, this.gameId, player.getPlayerId(), value);
		List<ZjhDeskInfo> list = new ArrayList<>();
		list.add(infoA);
		list.add(infoB);
		infoA.setPush(true);
		infoB.setPush(true);
		ZjhDeskInfo info = checkTheWinner(list);
		logger.info("{}|{}|玩家{}比牌胜利", this.deskId, this.gameId, getPlayerByPos(info.getPostion()).getPlayerId());
		record.compareCard(infoA.getPositionValue(), needCoin, getCompareList(list).toArray(), info.getPositionValue(), rounds + 1, lastPost);
		int calRounds = rounds + 1;
		checkRounds();

		zjhMsgeHelper.notifyIntIntList(ResponseCode.ZJH_COMPARE_CARD, needCoin, info.getPositionValue(), currentPos.getValue(), infoA.getPositionValue(), getCompareList(list), rounds + 1);
		//是否赢了   是否回合满了
		if (isOnlyOneLeave() || isMaxRound()) {
			logger.info("{}|{}|进入比牌结算 isOnlyOneLeave:{},isMaxRound:{} ", this.deskId, this.gameId, isOnlyOneLeave(),
					isMaxRound());
			calculationWiner(calRounds);
			return;
		}
		callNextGiveUp(getPlayerByPos(currentPos));
	}

	/**
	 * 弃牌
	 */
	public void giveUp(PlayerInfo player, boolean isPassive) {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		logger.info("{}|{}|玩家{}{}弃牌", this.deskId, this.gameId, player.getPlayerId(), isPassive ? "超时" : "主动");
		if (gameState != GameState.START) {
			logger.debug("game is over ");
			return;
		}
		ZjhDeskInfo info = this.getDeskInfo(player);
		if (info == null) {
			logger.debug("player not int the game ");
			return;
		}
		if (info.isGiveUp()) {
			logger.error(" 玩家{}已经弃牌了", currentPos.getValue());
			return;
		}
		info.setGiveUp(true);
		info.setPush(false);
		record.giveUp(info.getPositionValue(), rounds + 1, lastPost, lastPost);
		int calRounds = rounds + 1;
		if (isPassive) {
			info.addPassiveTime();
		} else {
			info.cleanPassiveTime();
		}
		if (currentPos == info.getPostion()) {
			stopOperationFuture();
			checkRounds();
			callNextGiveUp(getPlayerByPos(currentPos));
		}
		if (isOnlyOneLeave()) {
			stopOperationFuture();
//			gameState = GameState.OVER;
			logger.info("{}|{}|进入比牌结算 isOnlyOneLeave:{}", this.deskId, this.gameId, isOnlyOneLeave());
			zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_GIVE_UP, CommonCreator.createPBTriple(info.getPositionValue(), currentPos.getValue(), calRounds));
			calculationWiner(calRounds);
			return;
		}
		if (isAllFull() || isMaxRound() || (isAllIn && isNowRoundEnd())) {
			stopOperationFuture();
			logger.info("{}|{}|进入比牌结算 isMaxRound:{},isAllFull:{},isAllIn:{}  ", this.deskId, this.gameId, isMaxRound(),
					isAllFull(), (isAllIn && isNowRoundEnd()));
			zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_GIVE_UP, CommonCreator.createPBTriple(info.getPositionValue(), currentPos.getValue(), calRounds));
			calculationWiner(calRounds);
			return;
		}
		zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_GIVE_UP, CommonCreator.createPBTriple(info.getPositionValue(), currentPos.getValue(), rounds + 1));
	}

	/**
	 * 看牌
	 *
	 * @param player
	 */
	public void lookCard(PlayerInfo player) {
		logger.info("{}|{}|玩家{}看牌", this.deskId, this.gameId, player.getPlayerId());
		ZjhDeskInfo info = this.getDeskInfo(player);
		if (rounds < lookRound - 1) {
			logger.debug("can't look at now " + rounds + "," + lookRound);
			return;
		}
		if (info.isOpen()) {
			return;
		}
		info.cleanPassiveTime();
		info.setOpen(true);
		for (PlayerInfo playerInfo : deskInfoMap.keySet()) {
			if (playerInfo.getPlayerId() == player.getPlayerId()) {
				playerInfo.write(ResponseCode.ZJH_SEE_CARD, zjhMsgeHelper.handCards(info.getPositionValue(), info.getStarategyValue(), info.getHandCardKeyList()));
			} else {
				playerInfo.write(ResponseCode.ZJH_SEE_CARD, zjhMsgeHelper.handCards(info.getPositionValue(), null, null));
			}
		}
		record.lookCard(info.getPositionValue(), rounds + 1, lastPost, lastPost);
	}

	/**
	 * 加注
	 *
	 * @param player
	 * @param value
	 */
	public void addPots(PlayerInfo player, int value) {
		logger.info("{}|{}|玩家{}请求加注{}", this.deskId, this.gameId, player.getPlayerId(), value);
		if(value < 0){
			logger.error("下注错误");
			return;
		}
		if (gameState != GameState.START) {
			logger.debug("addPots game is over");
			return;
		}
		ZjhDeskInfo deskInfo = getDeskInfo(player);
		if (deskInfo == null) {
			logger.debug("player: " + player.getPlayerId() + " isn't  in the game");
			return;
		}
		if (currentPos.getValue() != deskInfo.getPostion().getValue()) {
			logger.error(" 当前操作玩家位置为 {}, 没轮到玩家 {} 加注", currentPos.getValue(), deskInfo.getPostion().getValue());
			return;
		}
		logger.info("{}|{}|玩家{}实际加注{}", this.deskId, this.gameId, player.getPlayerId(), value);
		float needCoin = 0;

		// 若是满压,则要计算
		if (isFull) {
			deskInfo.setFull(true);
			// 根据满压的值计算满压金额
			// 若fullCoin == 0 说明大家钱都够，走钱够的流程
			// 若fullCoin !=0,则记录的是满压的最大值
			if (fullCoin == 0) {
				//（房间底分x最大加注倍数）x该玩家剩余轮数
				needCoin = getMaxOnePots() * getHasRounds() * getOpenMultiple(deskInfo);
				logger.info("{}|{}|玩家{}满压钱够，需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
			} else {
				needCoin = fullCoin * getOpenMultiple(deskInfo);

				if (deskInfo.getCoin() <= needCoin) {
					needCoin = deskInfo.getCoin();
				}
				logger.info("{}|{}|玩家{}满压钱不够，需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
			}
		} else {
			needCoin = value;
			// 判断当前加注金额是否会触发孤注一掷 (当加注金额单注>=最大单注的时候)
			if (value >= maxPots) {
				// 触发了孤注一掷
				isAllIn = true;
				logger.info("{}|{}|玩家{}下注金额{}大于此轮最大可以下注的金额{},出发了孤注一掷", this.deskId, this.gameId, player.getPlayerId(), value, maxPots);
			}

			// 若孤注一掷,则玩家出的牌只能是最大的单注
			if (isAllIn) {
				needCoin = maxPots * getOpenMultiple(deskInfo);

				logger.info("{}|{}|玩家{}触发孤注一掷后,需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
			} else {
				// 必须大于等于前注下的钱,且玩家的钱够
				needCoin *= getOpenMultiple(deskInfo);
				if (value >= lastPost) {
					if (deskInfo.getCoin() < needCoin) {
						needCoin = lastPost * getOpenMultiple(deskInfo);
						logger.info("{}|{}|玩家{}正常下注,不过传的钱不够{}，自己处理掉{}", this.deskId, this.gameId, player.getPlayerId(), value, needCoin);
					} else {
						logger.info("{}|{}|玩家{}正常下注,需要下注{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
					}
				} else {
					needCoin = lastPost * getOpenMultiple(deskInfo);
					logger.info("{}|{}|玩家{}非正常下注,帮玩家纠正{}", this.deskId, this.gameId, player.getPlayerId(), needCoin);
				}
			}
		}

		stopOperationFuture();
		deskInfo.cleanPassiveTime();
		recordPost(deskInfo, (int) needCoin, false);
		if (!deskInfo.isFull()) {
			lastPost = (int) needCoin / getOpenMultiple(deskInfo);
		}
		//记录
		record.addPots(currentPos.getValue(), (int) needCoin, rounds + 1, lastPost);
		int calRounds = rounds + 1;
		deskInfo.setRound(rounds);
		logger.info("{}|{}|玩家{}设置轮数{}", this.deskId, this.gameId, player.getPlayerId(), rounds);
		boolean nowRoundEnd = isNowRoundEnd();
		checkRounds();
		//通知加注多少
		zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_ADD_GOLD, zjhMsgeHelper.createTriple(deskInfo.getPostion().getValue(), (int) needCoin, currentPos.getValue(), isMaxRound() ? rounds : rounds + 1, 0));

		//回合已满    满压       孤注一掷本轮需要开牌
		if (isMaxRound() || isAllFull() || (isAllIn && nowRoundEnd)) {
			logger.info("{}|{}|进入比牌结算 isMaxRound:{},isAllFull:{},isAllIn:{}  ", this.deskId, this.gameId, isMaxRound(),
					isAllFull(), (isAllIn && nowRoundEnd));
			stopOperationFuture();
			calculationWiner(calRounds);
			return;
		}
		callNextGiveUp(getPlayerByPos(currentPos));
	}

	/**
	 * 当前回合结束
	 *
	 * @return
	 */
	private boolean isNowRoundEnd() {
		return deskInfoMap.values().stream().filter(e -> e.isGameIng()).allMatch(e -> e.getRound() == rounds);
	}

	/**
	 * 获取比牌人员位置
	 *
	 * @param infos
	 * @return
	 */
	private List<Integer> getCompareList(List<ZjhDeskInfo> infos) {
		List<Integer> list = new ArrayList<>();
		infos.forEach(e -> {
			list.add(e.getPositionValue());
		});
		return list;
	}

	private void waitNewGame() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		stopOperationFuture();
		logger.info("start waitNewGame....");
//		if(isAllPlayerLeave()){  // 就算都离线 也要进行完该局才能踢人解散桌子
//			deskInfoMap.keySet().forEach(e ->{
//				DeskMgr.getInst().removePlayer(e,true);
//			});
//			onGameEnd();
//			DeskMgr.getInst().removeDesk(this);
//			return;
//		}
		List<PlayerInfo> leaveList = new ArrayList<PlayerInfo>();
		getPlayerList().forEach(e -> {
			ZjhDeskInfo info = getDeskInfo(e);
			// 移除离开玩家
			if (info.isLeave() || info.getPassiveTime() >= leaveCount) {
				zjhMsgeHelper.notifyMessage(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(e.getPlayerId(), 1));
				info.setLeave(true);
				logger.info("wait new game player leave " + e.getPlayerId());
				leaveList.add(e);
			}
		});

		if (isAllPlayerLeave()) {
			DeskMgr.getInst().removeDesk(this);
			return;
		}
		for (PlayerInfo p : leaveList) {
			leaveDesk(p);
		}
	    
		gameState = GameState.WAIT;
		resetGameStaus();
		//通知玩家再准备一次
		zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_START_READY, null);
		operationFuture = LogicActorManager.registerOneTimeTask(wait_game_start, this::allPlayerIsReady, getDeskId());
	}

	/**
	 * 计算赢家
	 * @param calRounds
	 *            结算回合以最后一次操作轮数为主 不能受到每轮结束添加轮数影响
	 */
	private void calculationWiner(int calRounds) {
		this.resultType = isOnlyOneLeave() ? 1 : 2;
		rounds = calRounds; // 最终回合以最后一个操作的玩家为主
		if (!isOnlyOneLeave()) { //  除开 剩余1人自动胜利外 都要开牌
			getAllDeskInfo().forEach(e -> {
				if (!e.isWatch() && !e.isGiveUp()) {
					e.setPush(true);
				}
				if (e.isLose()) {
					e.setPush(true);
				}
			});
		}
		List<ZjhDeskInfo> infoList = getALlPlayer();
		ZjhDeskInfo winnerInfo = checkTheWinner(infoList);
		PlayerInfo winner = getPlayerByPos(winnerInfo.getPostion());
		logger.info("{}|{}|玩家{}为该局赢家", this.deskId, this.gameId, winner.getPlayerId());
		updateWinerCoin(winner);
		if (this.resultType == 2) {
			List<Integer> list = getCompareList(infoList);
			record.compareCard(0, 0, list.toArray(), winnerInfo.getPostion().getValue(), rounds, lastPost);
			zjhMsgeHelper.notifyIntIntList(ResponseCode.ZJH_COMPARE_CARD, 0, winnerInfo.getPositionValue(), 0, null, list, rounds);
		}
		//推送比牌玩家相关信息
		pushCompareCards();
		serZhuang(winnerInfo.getPostion());
		zjhMsgeHelper.notifyResult(ResponseCode.ZJH_RESULT, resultType, winnerInfo.getPositionValue(), post, getWinMoney());

		record.callResult(winnerInfo.getPositionValue(), getWinMoney(), calRounds, lastPost);   
		Gson gson = JsonUtil.getGson();
		recordId = TaxRecordUtil.recordGamReply(gson.toJson(record), startTime, deskInfoMap.keySet(), getGameType(), String.valueOf(gameId));
		recordIdList.add(recordId);
		gameState = GameState.OVER;
		stopOperationFuture();
		over_time = MiscUtil.getCurrentSeconds();
		if(LogicApp.getInst().isStop()){
    	    DeskMgr.getInst().removeDesk(this);
    		return;
       }
		if (!isPersonal()) {
			if(isAllPlayerLeave()){  // 就算都离线 也要进行完该局才能踢人解散桌子
				deskInfoMap.keySet().forEach(e ->{
					DeskMgr.getInst().removePlayer(e,true);
				});
				onGameEnd();
				DeskMgr.getInst().removeDesk(this);
				return;
			}
			operationFuture = LogicActorManager.registerOneTimeTask(disbankTime, this::waitNewGame, getDeskId());
		} else {
			resetGameStaus();
			gameState = GameState.WAIT;
		}
	}


	public void onGameEnd() {
		logger.info("{}|{}结束此局", this.deskId, this.gameId);
		stopOperationFuture();
		clearGame();
	}


	private void clearGame() {
		deskInfoMap.values().forEach(e -> e.clear());
		deskInfoMap.clear();
		posInfoMap.clear();
		postList.clear();
		record.clearStatus(getConfId());
		this.post = 0;
		this.lastPost = 0;
		this.rounds = 0;
		this.isFull = false;
		this.isAllIn = false;
//		this.firstPos = null;
		gameState = GameState.OVER;
	}

	private List<ZjhDeskInfo> getALlPlayer() {
		List<ZjhDeskInfo> list = new ArrayList<ZjhDeskInfo>();
		deskInfoMap.values().forEach(e -> {
			if (e.isGameIng()) {
				list.add(e);
			}
		});
		return list;
	}

	/**
	 * 是否全部都压满
	 *
	 * @return
	 */
	private boolean isAllFull() {
		// 若存在 游戏进行中,且没有满压的则为 没满压
		return deskInfoMap.values().stream().filter(e->e.isGameIng()).allMatch(e->e.isFull());
	}

	/**
	 * 检查是否只剩一个人了
	 *
	 * @return
	 */
	private boolean isOnlyOneLeave() {
		return deskInfoMap.values().stream().filter(e -> e.isGameIng()).count() == 1;
	}

	private void callNextGiveUp(PlayerInfo info) {
		if (!isPersonal()) {
			stopOperationFuture();
			operationFuture = LogicActorManager.registerOneTimeTask(waitTime, () -> giveUp(info, true), getDeskId());
			lastOperatingTime = MiscUtil.getCurrentSeconds();
		}
	}

	private boolean isMaxRound() {
		return rounds + 1 > maxRounds;
	}

	/**
	 * 通知输家赢家的牌
	 */
	private void pushCompareCards() {
		deskInfoMap.values().forEach(e -> {
			if (e.isPush()) {
				zjhMsgeHelper.notifyMessage(ResponseCode.ZJH_SHOW_CARDS, zjhMsgeHelper.handCards(e.getPositionValue(), e.getStarategyValue(), e.getHandCardKeyList()));
			}
		});
	}

	private void stopOperationFuture() {
		if (operationFuture != null) {
			operationFuture.cancel(true);
			operationFuture = null;
		}
	}

	/**
	 * 更新赢家金币
	 *
	 * @param playerInfo
	 */
	private void updateWinerCoin(PlayerInfo playerInfo) {
		Map<ZjhPostion, Integer> scoreHis = new HashMap<>();
		long win_pre_coin = playerInfo.getCoin();
		deskInfoMap.forEach((e, f) -> {
			if (!f.isWatch()) {
				long pre_coin = e.getCoin();
				e.updateCoin(f.getPost(), false);
				f.addTotalPots(-f.getPost());
				scoreHis.put(f.getPostion(), -f.getPost());
				if (f.isGiveUp() || f.isLose()) {
					detail.addOneRecord(e.getPlayerId(), f.getPositionValue(), 0, 0, e.getChannel_id(), e.getPackage_id(), e.getDevice(), pre_coin, e.getCoin(), e.getIp());
					e.write(RequestCode.LOG_MONEY.getValue(), LogHelper.logLoseMoney(e.getPlayerId(), getLogMoneyType(false), getGameType().getValue(), f.getPost(), pre_coin, e.getCoin(), e.getIp(), e.getChannel_id(), String.valueOf(e.getPackage_id()), e.getDevice(), getGameId()));
					TaxRecordUtil.sendGamePlayerStatus(e, -f.getPost());
				}
			}
		});

		ZjhDeskInfo info = getDeskInfo(playerInfo);
		post -= info.getPost();
		int addCoin = info.getPost() + getWinMoney();
		playerInfo.updateCoin(addCoin, true);
		info.addTotalPots(addCoin);
		scoreHis.put(info.getPostion(), post);
		scoreList.add(new Pair<>(startTime, scoreHis)); // 总积分显示税前收入

		winPos = getPosByPlayer(playerInfo);
		//日志记录
		playerInfo.write(RequestCode.LOG_MONEY.getValue(), LogHelper.logGainMoney(playerInfo.getPlayerId(), getLogMoneyType(true), getGameType().getValue(), getWinMoney(), win_pre_coin, playerInfo.getCoin(), playerInfo.getIp(), playerInfo.getChannel_id(), String.valueOf(playerInfo.getPackage_id()), playerInfo.getDevice(), getGameId()));
		TaxRecordUtil.sendGamePlayerStatus(playerInfo, getWinMoney());
		//详情记录
		detail.addOneRecord(playerInfo.getPlayerId(), info.getPositionValue(), 1, getTaxRateMoney(), playerInfo.getChannel_id(), playerInfo.getPackage_id(), playerInfo.getDevice(), win_pre_coin, playerInfo.getCoin(), playerInfo.getIp());
		//扣税日志
		TaxRecordUtil.recordGameTaxInfo(startTime, deskInfoMap.values().size(), gameId, getGameType(), getConfId(), zhuangId, post, getTaxRateMoney(), detail, this);
		//同步金钱
		getPlayerList().forEach(e -> {
			ZjhDeskInfo deskInfo = getDeskInfo(e);
			if (deskInfo != null && !deskInfo.isLeave()) {
				e.write(ResponseCode.ACCOUNT_UPDATE_DESK_COIN, CommonCreator.createPBPairList(getPlayerMoneyResult()));
			}
		});
	}

	private void leaveDesk(PlayerInfo player) {
		posInfoMap.remove(getDeskInfo(player).getPostion());
		deskInfoMap.remove(player);
		DeskMgr.getInst().removePlayer(player,false);
		logger.info("{}|{}|玩家{}离开zjh桌子,剩余玩家个数{}", this.deskId, this.gameId, player.getPlayerId(), deskInfoMap.size());
	}

	/**
	 * 更新桌子上的钱
	 */
	public void syncAllPlayerMoney() {
		for (PlayerInfo e : getPlayerList()) {
			ZjhDeskInfo info = getDeskInfo(e);
			if (info == null || info.isLeave()) {
				continue;
			}
			e.write(ResponseCode.COUPLE_UPDATE_DESK_MONEY, CommonCreator.createPBPairList(getAllPlayerMoney()));
		}
	}

	protected Map<Integer, Integer> getPlayerMoneyResult() {
		Map<Integer, Integer> result = new HashMap<>();
		for (Map.Entry<PlayerInfo, ZjhDeskInfo> entry : deskInfoMap.entrySet()) {
			result.put(entry.getValue().getPositionValue(), entry.getKey().getCoin());
		}
		return result;
	}

	/**
	 * 赢家扣税后所得金钱
	 */
	private int getWinMoney() {
		float taxtRate = getTaxRate();
		return (int) (post * taxtRate);
	}

	/**
	 * 税钱
	 */
	private int getTaxRateMoney() {
		return post - getWinMoney();
	}

	/**
	 * 计算输赢
	 *
	 * @param infos
	 * @return
	 */
	private ZjhDeskInfo checkTheWinner(List<ZjhDeskInfo> infos) {
		infos.forEach(e -> logger.info("{}|{}|玩家{}牌为：{}", this.deskId, this.gameId,
				getPlayerByPos(e.getPostion()).getPlayerId(), e.getHandCards().toString()));
		boolean hasLeopard = ZjhCardCompare.hasLeopard(infos);
		ZjhDeskInfo winer = null;
		for (int i = 0; i < infos.size(); i++) {
			ZjhDeskInfo infoA = infos.get(i);
			winer = infoA;
			infoA.getHandCardKeyList().forEach(e -> System.out.println(e));
			for (int j = i + 1; j < infos.size(); j++) {
				ZjhDeskInfo infoB = infos.get(j);
				ZjhCompareStrategy strategyA = infoA.getStrategy();
				ZjhCompareStrategy strategyB = infoB.getStrategy();
				int result = ZjhCardCompare.compare(strategyA, strategyB, hasLeopard);
				if (result != 0) { //a输了
					winer = infoB;
					infoA.setLose(true);
					break;
				}
				infoB.setLose(true);
				i++;
			}
		}
		return winer;
	}

	private PlayerInfo getPlayerByPos(ZjhPostion pos) {
		ZjhDeskInfo info = getDeskInfo(pos);
		for (PlayerInfo player : deskInfoMap.keySet()) {
			if (deskInfoMap.get(player) == info) {
				return player;
			}
		}
		return null;
	}

	/**
	 * 检查回合
	 */
	private void checkRounds() {
		currentPos = getNextPos(currentPos.getNextPos());
	}

	/**
	 * 获取下一个执行位
	 *
	 * @param postion
	 * @return
	 */
	private ZjhPostion getNextPos(ZjhPostion postion) {
		if (firstPos != null) {
			if (postion.getValue() == firstPos.getValue()) {
				if (rounds < maxRounds) {
					rounds++;
					logger.info("{}|{}|轮数增加 变为{}", this.deskId, this.gameId, rounds);
				}
				calculateMaxFull();
			}
		}
		ZjhDeskInfo info = posInfoMap.get(postion);
		if (info == null || info.isGiveUp() || info.isWatch() || !info.isReady() || info.isLose()) {
			logger.info("{}|{}|当前位置为{}状态不对,获取下一个位置", this.deskId, this.gameId, postion);
			return getNextPos(postion.getNextPos());
		}
		logger.info("{}|{}|下一个操作位置为{}", this.deskId, this.gameId, info.getPositionValue());
		return info.getPostion();
	}
	
	/**
	 * 获取单次最大下注量 (正常下注)
	 * @return
	 */
	public int getMaxOnePots(){
		return minPost * raiseRound.get(raiseRound.size() - 1);
	}
	
	/**
	 * 获取剩余轮数
	 * @return
	 */
	public int getHasRounds(){
		return maxRounds - rounds;
	}
	
	/**
	 * 获取看牌倍数
	 * @param player
	 * @return
	 */
	public int getOpenMultiple(ZjhDeskInfo player) {
		return player.isOpen() ? 2 : 1;
	}
	
	/**
	 * 获取金钱log日志子类型
	 * @param isWin
	 * @return
	 */
	public int getLogMoneyType(boolean isWin) {
		if (isWin) {
			if (isPersonal()) {
				return MoneySubAction.ZJH_WIN_PERSONAL.getValue();
			} else {
				return MoneySubAction.ZJH_WIN.getValue();
			}
		} else {
			if (isPersonal()) {
				return MoneySubAction.ZJH_LOSE_PERSONAL.getValue();
			} else {
				return MoneySubAction.ZJH_LOSE.getValue();
			}
		}
	}

	public void disbandDesk() {
		if(gameState == GameState.START){
			 initPersonalScore();
		}
		onCallGameOver();    //通知center游戏结束
		DeskMgr.getInst().removeDesk(this);
	}

	protected void onCallGameOver() {
		stopOperationFuture();
		stopOverMyselfFuture();
		onRoomDeskGameEnd();
	}

	protected void onRoomDeskGameEnd() {
		CoupleMajiang.PBPairGameNo2Record.Builder totalBuilder = CoupleMajiang.PBPairGameNo2Record.newBuilder();
		totalBuilder.setGameNo(gameId+"");
		CoupleMajiang.PBOneRoomRecord.Builder builder = CoupleMajiang.PBOneRoomRecord.newBuilder();
		builder.setGameId(getGameType().getValue());
		builder.setDeskCreateTime(this.createTime);
		builder.setCreateId(this.createId);
		for (int i = 0, size = scoreList.size(); i < size; i++) {
			builder.addRecordList(createPBOneGameRecord(scoreList.get(i).getLeft()
					, getRecordId(i), scoreList.get(i).getRight()));
		}
		builder.setDeskId(getDeskId());
		totalBuilder.setRecord(builder.build());
		LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_ROOM_DESK_GAME_END, totalBuilder.build()));
	}

	private CoupleMajiang.PBOneGameRecord createPBOneGameRecord(int time, int recordId, Map<ZjhPostion, Integer> scoreMap) {
		CoupleMajiang.PBOneGameRecord.Builder builder = CoupleMajiang.PBOneGameRecord.newBuilder();
		builder.setTime(time);
		builder.setRecordId(recordId);
		if (scoreMap != null) {
			scoreMap.entrySet().forEach(e -> {
				CoupleMajiang.PBOnePosInfo.Builder subBuilder = CoupleMajiang.PBOnePosInfo.newBuilder();
				PlayerInfo info = getPlayerByPos(e.getKey());
				if (info == null) {
					subBuilder.setName("");
				} else {
					subBuilder.setName(info.getName());
				}
				subBuilder.setPos(e.getKey().getValue());
				subBuilder.setScore(e.getValue());
				builder.addInfoList(subBuilder.build());
			});
		}
		return builder.build();
	}

	private int getRecordId(int i) {
		if (i >= recordIdList.size()) {
			return 0;
		}
		return recordIdList.get(i);
	}

	private int getLostTime() {
		return MiscUtil.getCurrentSeconds() - lastOperatingTime;
	}

	private ZjhDeskInfo getDeskInfo(PlayerInfo playerInfo) {
		return deskInfoMap.get(playerInfo);
	}

	private int getPosByPlayer(PlayerInfo player) {
		return deskInfoMap.get(player).getPositionValue();
	}

	private ZjhDeskInfo getDeskInfo(ZjhPostion pos) {
		return posInfoMap.get(pos);
	}

	private List<ZjhDeskInfo> getAllDeskInfo() {
		return new ArrayList<>(deskInfoMap.values());
	}

	private float getTaxRate() {
		return 1 - (taxRate / 100f);
	}

	public Map<PlayerInfo, ZjhDeskInfo> getDeskInfoMap() {
		return deskInfoMap;
	}

	public List<ZjhPostion> getPosInfoList() {
		return new ArrayList<>(posInfoMap.keySet());
	}

	protected ZjhMsgeHelper zjhMsgeHelper = new ZjhMsgeHelper();

	public class ZjhMsgeHelper {

		protected Zjh.PBZJHTriple createTriple(int one, int two, int three, int four, int needCoin) {
			Zjh.PBZJHTriple.Builder builder = Zjh.PBZJHTriple.newBuilder();
			builder.setOne(one);
			builder.setTwo(two);
			builder.setThree(three);
			builder.setFour(four);
			if (needCoin != 0) {
				builder.setNeedCoin(needCoin);
			}
			return builder.build();
		}

		protected Zjh.PBZJHcards handCards(int pos, Integer type, List<Integer> list) {
			Zjh.PBZJHcards.Builder builder = Zjh.PBZJHcards.newBuilder();
			builder.setPos(pos);
			if (type != null) {
				builder.setType(type);
			}
			if (list != null) {
				builder.setCards(CommonCreator.createPBInt32List(list));
			}
			return builder.build();
		}

		protected Zjh.PBZJHDeskinfo deskInfo(int one, int two, int three, int four, int five, List<Integer> six) {
			Zjh.PBZJHDeskinfo.Builder builder = Zjh.PBZJHDeskinfo.newBuilder();
			builder.setOne(one);
			builder.setTwo(two);
			builder.setThree(three);
			builder.setFour(four);
			builder.setFive(five);
			builder.setSix(CommonCreator.createPBInt32List(six));
			return builder.build();
		}

		protected MessageLite getResult(int resultType, int winPos, int gold, int taxGold) {
			Zjh.PBZJHResult.Builder builder = Zjh.PBZJHResult.newBuilder();
			builder.setResultType(resultType);
			builder.setWinPos(winPos);
			builder.setBeforeTax(gold);
			builder.setAfterTax(taxGold);
			return builder.build();
		}

		protected void notifyGameStart() {
			Zjh.PBZJHDealCard.Builder message = Zjh.PBZJHDealCard.newBuilder();
			message.setPos(currentPos.getValue());
			message.setZhuang(zhuangjia.getValue());
			Common.PBInt32List.Builder builder = Common.PBInt32List.newBuilder();
			deskInfoMap.values().stream().filter(e -> !e.isWatch()).forEach(e -> builder.addValue(e.getPositionValue()));
			message.setWatchPos(builder);
			deskInfoMap.forEach((e, f) -> {
				e.write(ResponseCode.ZJH_DEAL_CARD, message.build());
			});
		}

		protected void notifyMessage(ResponseCode code, MessageLite message) {
			for (PlayerInfo player : deskInfoMap.keySet()) {
//				ZjhDeskInfo info = getDeskInfo(player);
//				if (!info.isLeave()) {
					player.write(code, message);
//				}
			}
		}
		
		public void notifyMessageNoSelf(ResponseCode code, MessageLite message,PlayerInfo selfPlayer) {
            for (PlayerInfo player : deskInfoMap.keySet()) {
            	if(player == selfPlayer){
            		continue;
            	}
                player.write(code, message);
            }
        }

		protected void notifyResult(ResponseCode code, int resultType, int winPos, int gold, int taxGold) {
			Zjh.PBZJHResult.Builder builder = Zjh.PBZJHResult.newBuilder();
			builder.setResultType(resultType);
			builder.setWinPos(winPos);
			builder.setBeforeTax(gold);
			builder.setAfterTax(taxGold);
			notifyMessage(code, builder.build());
		}

		protected void notifyIntIntList(ResponseCode code, int pots, int winpos, int nextPos, Integer ownPos, List<Integer> list, int round) {
			Common.PBInt32List.Builder listpb = Common.PBInt32List.newBuilder();
			list.forEach(e -> listpb.addValue(e));
			Zjh.PBZjhCompare.Builder builder = Zjh.PBZjhCompare.newBuilder();
			builder.setWinPos(winpos);
			builder.setNextPos(nextPos);
			builder.setPlayerPos(listpb);
			builder.setPots(pots);
			if (ownPos != null) {
				builder.setOwnPos(ownPos);
			}
			builder.setRound(round);
			notifyMessage(code, builder.build());
		}

		protected MessageLite createResetGame(ZjhDeskInfo self, int lostTime, int currentPos, int lastPost, List<Integer> posts, int round, int gameRounds, int start, int zhuangjia, Map<PlayerInfo, ZjhDeskInfo> infoMap, Zjh.PBZJHResult result) {
			Common.PBInt32List.Builder postBuilder = Common.PBInt32List.newBuilder();
			postBuilder.addAllValue(posts);
			Zjh.PBZjhResetGame.Builder builder = Zjh.PBZjhResetGame.newBuilder();
			builder.setSingleBet(lastPost);
			builder.setCurrentRound(round);   //  OVER 状态 发送 maxRounds + 1 告知客户端 不显示牌局信息
			builder.setGameRounds(gameRounds);  // 游戏轮数
			builder.setTotalBets(postBuilder.build());
			builder.setTotalBets(postBuilder.build());
			builder.setCurPos(currentPos);
			builder.setLoseTime(lostTime);
			builder.setRoundFlagPos(start);
			builder.setBankerPos(zhuangjia);
			if (!isAllFull()) {
				builder.setIsFullRess(isFull);
				builder.setIsSelfFullRess(self.isFull());
				// 获取全压的钱
				ZjhPostion zjh_postion = ZjhPostion.getByValue(currentPos);
				if (null != zjh_postion) {
					builder.setNeedCoin(getNeedFullCoin(zjh_postion));
				}
			}
			if (result != null) {
				builder.setResult(result);
			}
			infoMap.forEach((e, f) -> {
				if (gameState != GameState.WAIT) {
					if (!f.isWatch()) {
						Zjh.PBZjhPlayerState.Builder playerInfo = Zjh.PBZjhPlayerState.newBuilder();
						playerInfo.setPos(f.getPositionValue());
						playerInfo.setIsGiveUp(f.isGiveUp());
						playerInfo.setIsLose(f.isLose());
						playerInfo.setIsCheck(f.isOpen());
						playerInfo.setBetCoin(f.getPost());
						if (f.isOpen() && f == self) {
							playerInfo.addAllCards(f.getHandCardKeyList());
							playerInfo.setCardType(f.getStarategyValue());
						}
						if (gameState == GameState.OVER && f.isPush()) {
							playerInfo.addAllCards(f.getHandCardKeyList());
							playerInfo.setCardType(f.getStarategyValue());
						}
						builder.addPlayerState(playerInfo);
					}
				}
				builder.addPlayerInfoList(CoupleCreator.createZjhPlayerInfo(e, f));
			});
			return builder.build();
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
		ZjhDetail zjh_detail = (ZjhDetail) detail;
		for (OnePosDetail one : zjh_detail.getRecords()) {
			if (0 >= one.getTax()) {
				continue;
			}

			TaxRecordUtil.recordPlayerTaxInfoToDB(zjh_detail.getType(), one.getPlayerId(), roomId, one.getTax(), one.getChannel_id(), one.getPackage_id(), one.getDevice());
		}
	}

	@Override
	public void destroy() {
		isDestroyed.set(true);
		stopOperationFuture();
		stopOverMyselfFuture();
		deskInfoMap.clear();
		posInfoMap.clear();
	}
	
	  private boolean isPlayerMoneyEnough(PlayerInfo info) {
		  if (!isPersonal()) {
	            return checkRoomMoney(info);
	        } else {
	            return checkPrivateRoomMoney(info);
	        }
	    }

	    private boolean checkRoomMoney(PlayerInfo info) {
	        if (info == null) {
	            return false;
	        }
	        if (info.getCoin() < CoupleRoomInfoProvider.getInst().getEnterLimit(getConfId())) {
	            return false;
	        }
	        return true;
	    }

	    private boolean checkPrivateRoomMoney(PlayerInfo info) {
	        if (info == null) {
	            return false;
	        }
	        if (info.getCoin() <  minPost * enterTimes) {
	            return false;
	        }
	        return true;
	    }

	@Override
	public void disBankDeskTimeOver() {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
		logger.info("{}桌子{}期限到了,解散中.",getGameType(),getDeskId());
		disbandDesk();
	}

	@Override
	public void playerWantContinue(PlayerInfo player) {
		ZjhDeskInfo info = deskInfoMap.get(player);
    	if(info == null){
    		return;
    	}
    	//检查这个玩家是否能继续
    	if (!isPlayerMoneyEnough(player)) {
			player.write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
			return;
		}
		syncAllPlayerMoney();
		zjhMsgeHelper.notifyMessage(ResponseCode.XUENIU_CONTINUE, CommonCreator.createPBInt32(info.getPositionValue()));
	}

	@Override
	public boolean isPlayerLeave(PlayerInfo player) {
		ZjhDeskInfo info = getDeskInfo(player);
		if (info == null) {
			return true;
		}
		return info.isLeave();
	}
}
