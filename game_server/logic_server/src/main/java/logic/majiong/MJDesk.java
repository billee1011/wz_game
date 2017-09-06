package logic.majiong;

import static logic.majiong.MajongState2.UN_KNOW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import config.CoupleRoomInfoProvider;
import config.JsonUtil;
import config.bean.CoupleRoom;
import config.bean.PersonalConfRoom;
import config.provider.PersonalConfRoomProvider;
import logic.AbstractDesk;
import logic.DeskMgr;
import logic.define.GameType;
import logic.majiong.define.MJPosition;
import logic.record.MjRecord;
import logic.record.TaxRecordUtil;
import logic.record.detail.MjDetail;
import logic.record.detail.OnePosDetail;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;
import util.Three;

/**
 * Created by Administrator on 2016/12/12.
 */
public abstract class MJDesk extends AbstractDesk {
    public static int time_dealy = 1 * 1000;
    public static int time1 = 1 * 1000;
    public static int time8 = 8 * 1000 + time_dealy;
    public static int time12 = 12 * 1000 + time_dealy;
    public static int time5 = 5 * 1000 + time_dealy;
    public static int huanSanZhang = 4800;      // 换三张的时间

    private static final Logger logger = LoggerFactory.getLogger(CoupleMJDesk.class);
    private int rounds;                          //多少手
    protected Map<PlayerInfo, PlayerDeskInfo> player2InfoMap = new HashMap<>();
    protected Map<MJPosition, PlayerDeskInfo> postion2InfoMap = new HashMap<>();
    protected Queue<Integer> paiPool;
    private AtomicBoolean isGameing = new AtomicBoolean(false);
    private Pair<Integer, Integer> diceNum = new Pair<>();
    protected MJPosition currentTurn;
    protected MJPosition preTurn;
    protected MJPosition currOperationTurn;
//    protected MajongState gameState = MajongState.UN_KNOW;
    protected MajongState2 gameState2 = UN_KNOW;
    protected ScheduledFuture<?> operationFuture = null;
    protected ScheduledFuture<?> autoChuPaiBeginFuture = null;      // 遊戲開始時超時自動出牌
    public Map<MJPosition, List<Integer>> operationList = new HashMap<>();
    private long gameId;
    protected MjRecord record = null;
    protected MjDetail detail = null;
    private boolean huIng = false;
    protected boolean isGangIng = false;
    protected int startTime;
    private int maxRounds;
    protected int createId;
    protected boolean stop;
    protected List<Pair<Integer, Map<MJPosition, Integer>>> scoreList = new ArrayList<>();
    protected List<Integer> recordIdList = new ArrayList<>();
    private boolean tiyan;
    public int opBeginTime = 0;
    private boolean isTimeOver = false;
    protected MJPosition dianPaoPos = null;
    public List<HuInfo> huList = new ArrayList<>();
    protected MJPosition lastDianGangPos = null;
    protected MJPosition pengWating = null; // 碰牌等待位置
    protected MJPosition gangWating = null; // 杠牌等待位置
    protected int gangWatingValue;  // 杠牌等待牌值

    protected PersonalConfRoom personalConfRoom = null;
    protected CoupleRoom coupleRoom = null;

    public MJDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
        this(deskId,0, 99999, false, roomConfId, playerList);
    }

    public MJDesk(int deskId,int creatorId, int maxRounds, boolean personal, int roomConfId, List<PlayerInfo> playerList) {
        super(deskId,roomConfId);
        this.rounds = 0;
        this.createId = creatorId;
        setPersonal(personal);
        this.maxRounds = maxRounds;
        initMjGameDeskInfo(playerList);
        //初始化之后就可以记录玩家的信息了啊
        this.record = new MjRecord(player2InfoMap, roomConfId);
        this.detail = new MjDetail(getGameType() == null ? 0 : getGameType().getValue());

        logger.debug("{}|{} the desk created and the time is {}",getDeskId(),getGameId(), createTime);
    }

    public void startNewGame() {
        logger.info("{}|{} 開始麻將遊戲,當前deskId為{}",getDeskId(),getGameId(), getDeskId());
        if(isPersonal()) {
            personalConfRoom = PersonalConfRoomProvider.getInst().getPersonalConfRoomById(getGameType().getValue());
        } else {
            coupleRoom = CoupleRoomInfoProvider.getInst().getRoomConf(getConfId());         /// 这个地方 getConfId 在私房的情况下传递的不正确 （正常下是roomConfId 私房传递的是modelId）
            if(coupleRoom!=null){
                personalConfRoom = PersonalConfRoomProvider.getInst().getPersonalConfRoomById(coupleRoom.getMode());
            }
        }
        opBeginTime = 0;
        gameState2 = MajongState2.BEGIN;
        isGangIng = false;
        dianPaoPos = null;
        pengWating = null; 
        gangWating = null; 
        gangWatingValue = 0; 
        setHuIng(false);
        record.clear();
        isGameing.set(true);
        clearDeskInfo();
        player2InfoMap.values().forEach(e -> e.clear());
        rounds++;
        startTime = MiscUtil.getCurrentSeconds();
        if (record != null) {
            record.addSelfRoomRecord(getGameType().getValue(), maxRounds, rounds, getBaseScore(), isPersonal() ?  getDeskId() : getConfId());
        }
        this.gameId = geneGameNo();
        randomDiceNum();
        mixAllCard();
        if (rounds == 1) {
            selectZhuang();
        }
        record.setBanker_id(getZhuangId());
        dealCard();                                //发牌之后要通知两边发牌的情况 门风,圈风等等
        checkHuapai();
        record.addHandAndHuaCard(postion2InfoMap);

        logger.info("桌子{}发牌",getDeskId());
        msgHelper.notifyGameStart();
        beforeGame();
    }

    protected void onRoomDeskGameEnd() {
        stopOverMyselfFuture();

        CoupleMajiang.PBPairGameNo2Record.Builder totalBuilder = CoupleMajiang.PBPairGameNo2Record.newBuilder();
        totalBuilder.setGameNo(getGameId()+"");
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
        DeskMgr.getInst().removeDesk(this);
    }

    private int getRecordId(int i) {
        if (i >= recordIdList.size()) {
            return 0;
        }
        return recordIdList.get(i);
    }

    // 过牌后其他玩家的思考
    public void thinkAftGuo() {
        if (isGangIng) {
            // 大家都点过才能补杠
            if (this.operationList.size() == 0) {
                PlayerInfo playerInfo = getPlayerByPosition(currentTurn);
                msgHelper.notifyGang(playerInfo, getCurrentTurnInfo().getLastGangValue());
                isGangIng = false;
                getCurrentTurnInfo().setGangIng(false);
                calculateGang(playerInfo, getCurrentTurnInfo().getLastGangValue());
                setGameState2(MajongState2.GANG);
            }
            startDispatcherGang();
        } else {
            // 玩家过牌后，如果后面没有操作，就轮到带牌人的下家摸牌
            if (this.operationList.size() == 0) {
            	if (gangWating != null) { // 没人操作 如果发现有人等杠 就杠
            		currOperationTurn = gangWating;
            		gangPai(getPlayerByPosition(gangWating), gangWatingValue);
					return;
				} else if (pengWating != null) { // 没人操作 如果发现有人等碰 就碰
					currOperationTurn = pengWating;
					pengpai(getPlayerByPosition(pengWating));
					return;
				} 
                logger.info("{}|{} 当前没有可操作行为，状态为{}",getDeskId(),getGameId(), gameState2);
                setGangFalse();
                opBeginTime = MiscUtil.getCurrentSeconds();
                stopOperationFuture();
                removePositionOperation(getCurrentTurn());
                // 轮到打牌人的下家摸牌了
                currentTurn = nextPosition();
                currOperationTurn = currentTurn;
                logger.info("{}|{} 1s后下一个玩家摸牌",getDeskId(),getGameId());
                
                operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
            } else {
                // 后面还有人操作
                dispatcherOneOperation();
            }
        }
    }

    // 碰牌后其他玩家的思考
    public void thinkAftPeng() {
        // 碰了之后，可以打牌，可以听
        PlayerDeskInfo deskInfo = getCurrentTurnInfo();
        PlayerInfo playerInfo = getPlayerByPosition(currentTurn);

        // 碰之后玩家就直接出牌了
        if (!isPersonal()) {
            sortHandCard(playerInfo);
            opBeginTime = MiscUtil.getCurrentSeconds();
            logger.info("{}|{} 碰牌后9s自动打牌",getDeskId(),getGameId());
            stopOperationFuture();
            operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> discardCard(playerInfo, deskInfo.getLastHandCard()), getDeskId());
        }
    }

    // 吃牌后其他玩家的思考
    public void thinkAftChi() {
        PlayerDeskInfo deskInfo = getCurrentTurnInfo();
        PlayerInfo playerInfo = getPlayerByPosition(currentTurn);
        sortHandCard(playerInfo);

        // 要判断其他玩家能干什么
        if (!isPersonal()) {
            stopOperationFuture();
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> discardCard(playerInfo, deskInfo.getLastHandCard()), getDeskId());
        }
    }

    // 出牌后其他玩家的思考
    public void thinkAftChu() {
        // 要判断其他玩家能干什么
        operationList.clear();
        for (Map.Entry<MJPosition, PlayerDeskInfo> entry : postion2InfoMap.entrySet()) {
			if (entry.getValue().isDefeat()) {
				continue;
			}
            if (entry.getValue().isLeave()) {
                continue;
            }
            if (entry.getKey() == currentTurn) {
                continue;
            }
            if (getGameType() == GameType.XUEZHAN && entry.getValue().isHuPai()) {
                continue;
            }
            List<Integer> typeList = GameUtil.getOperationList(entry.getValue()
                    , getCurrentTurnInfo().getDeskPaiStack().peek(), canChi(), getIgnoreType(entry.getKey()), entry.getValue().ignorePengAndChi());
            if (typeList != null) {
                operationList.put(entry.getKey(), typeList);
                logger.info("{}|{} 位置{}具有操作{}",getDeskId(),getGameId(), entry.getKey(), JsonUtil.getJsonString(typeList));
            }
        }
        // 分发行为
        startDispatcherOperation();
    }

    // 摸牌后的思考
    public void thinkAftMo(int huaNum) {
        // 摸牌需要判断有没有自摸，如果自摸，超时自摸。 若没有自摸，超时打牌
        PlayerDeskInfo deskInfo = getCurrentTurnInfo();
        PlayerInfo playerInfo = getPlayerByPosition(currentTurn);
        // 要判斷玩家當前的牌能不能gang 或者 hu
        List<Integer> typeList = GameUtil.getOperationList(deskInfo
                , deskInfo.getLastHandCard(), canChi(), getIgnoreType(deskInfo.getPosition()), deskInfo.ignorePengAndChi());
        operationList.clear();
        if (typeList != null) {
            operationList.put(deskInfo.getPosition(), typeList);
            logger.info("{}|{} 位置{}具有操作{}",getDeskId(),getGameId(), deskInfo.getPosition(), JsonUtil.getJsonString(typeList));
        }

        if (!isPersonal()) {
            if (checkZiMo(playerInfo,huaNum)) {
                return;
            }
            stopOperationFuture();
//            sortHandCard(playerInfo);
            operationFuture = LogicActorManager.registerOneTimeTask(time8 + huaNum * GameConst.MO_HUA_TIME, () -> discardCard(playerInfo, deskInfo.getLastHandCard()), getDeskId());
        }
    }

    @Override
    public boolean isAllPlayerLeave() {
        for (PlayerDeskInfo deskInfo : player2InfoMap.values()) {
            if (!deskInfo.isLeave()) {
                return false;
            }
        }
        return true;
    }

    public MJPosition getCurrOperationTurn() {
        return currOperationTurn;
    }

    public void setCurrOperationTurn(MJPosition currOperationTurn) {
        this.currOperationTurn = currOperationTurn;
    }

    private Common.PBKeyPairList createScoreList(int time, Map<MJPosition, Integer> list) {
        Common.PBKeyPairList.Builder builder = Common.PBKeyPairList.newBuilder();
        builder.setKey(time);
        if (list != null) {
            list.entrySet().forEach(e -> builder.addList(CommonCreator.createPBPair(e.getKey().getValue(), e.getValue())));
        }
        return builder.build();
    }

    private CoupleMajiang.PBOneGameRecord createPBOneGameRecord(int time, int recordId, Map<MJPosition, Integer> scoreMap) {
        CoupleMajiang.PBOneGameRecord.Builder builder = CoupleMajiang.PBOneGameRecord.newBuilder();
        builder.setTime(time);
        builder.setRecordId(recordId);
        if (scoreMap != null) {
            scoreMap.entrySet().forEach(e -> {
                CoupleMajiang.PBOnePosInfo.Builder subBuilder = CoupleMajiang.PBOnePosInfo.newBuilder();
                PlayerInfo info = getPlayerByPosition(e.getKey());
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

    protected PlayerInfo getRoomCreator() {
        if (this.createId == 0) {
            return null;
        }
        return getPlayerInfo(createId);
    }


    @Override
    protected void playerMoneyChangeHook(PlayerInfo info) {
        syncAllPlayerMoney();
    }

    public void setCurrentTurn(MJPosition currentTurn) {
        this.preTurn = currentTurn;
        this.currentTurn = currentTurn;
    }

    //在玩家开始出牌之前的一些动作.....
    protected abstract void beforeGame();

    private void randomDiceNum() {
        int dice1 = Randomizer.nextInt(6) + 1;
        int dice2 = Randomizer.nextInt(6) + 1;
        diceNum.setLeft(dice1);
        diceNum.setRight(dice2);
        record.addDiceInfo(diceNum);
    }

    public int getDiceNum1() {
        return diceNum.getLeft() == null ? 0 : diceNum.getLeft();
    }

    public int getDiceNum2() {
        return diceNum.getRight() == null ? 0 : diceNum.getRight();
    }

    public void playerLeave(PlayerInfo player) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        info.setLeave(true);
        if (getRoomCreator() == null) {
            if (!isGameing()) {
//            	notifyPlayerLeave(player);
                logger.info("匹配房{}不在游戏中有玩家{}离开",getDeskId(),player.getPlayerId());
                player2InfoMap.forEach((e, f) -> {
//                    LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, e.getPlayerId()));
                    logger.info("玩家{} 离开房间{}成功",e.getPlayerId(),getDeskId());
//                    e.write(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(f.getPositionValue()));
                    DeskMgr.getInst().removePlayer(e,true);
                });
                stopOperationFuture();
                stopAutoChuPaiBeginFuture();
                DeskMgr.getInst().removeDesk(this);
                return;
            } else {
//                if (getGameType() == GameType.XUEZHAN && info.isHuPai()) {
//                    LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, player.getPlayerId()));
//                    postion2InfoMap.remove(info.getPosition());
//                    player2InfoMap.remove(player);
//                }
//                if (info.getTotalCardCotainGangChike() == 14) {
//                    discardCard(player, info.getLastHandCard());
//                }
            	//这里客户端连续点击桌子解散弹框之后实际没有退出房间,导致客户端停留在桌子上实际我们服务器已经离开,下面这个踢出是修改代码，暂时客户端只能点一次所以暂时屏蔽
//            	player.write(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(player.getPlayerId(), 0));
                // 若不在游戏中,
                if (isAllPlayerLeave()) {
                    logger.info("所有的玩家离开桌子{}",this.getDeskId());
                    onGameEnd();
                    return;
                }
            }
        }
        info.setLeave(false);
        msgHelper.notifyMessage(ResponseCode.LOBBY_PLAYER_LEAVE, CommonCreator.createPBPair(info.getPositionValue(), 0));
        info.setLeave(true);
    }

//    public void notifyPlayerLeave(PlayerInfo player) {
//        for (PlayerInfo player1 : player2InfoMap.keySet()) {
//            if (getPlayerDeskInfo(player1).isLeave()) {
//                continue;
//            }
//            player1.write(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(getPlayerPosition(player)));
//        }
//    }

    protected int getPlayerPosition(PlayerInfo player) {
        return player2InfoMap.get(player).getPosition().getValue();
    }

    protected MJPosition getPlayerPositionType(PlayerInfo player) {
        return player2InfoMap.get(player).getPosition();
    }

    @Override
    public void playerLogout(PlayerInfo player) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (info == null) {
            return;
        }
        playerLeave(player);
        info.setLogout(true);
    }

    @Override
    public void playerReLogin(PlayerInfo player) {
        PlayerDeskInfo info = player2InfoMap.get(player);
        if (info == null) {
            return;
        }
        info.setLogout(false);
        info.setLeave(false);
        getPlayerList().forEach(e -> e.write(ResponseCode.COUPLE_PLAYER_ENTER_DESK, CommonCreator.createPBInt32(player.getPlayerId())));
        player.write(ResponseCode.COUPLE_RESET_GAME, CoupleCreator.createPBCoupleResetGame(this, player, !isGameing(), info.getPositionValue(), getOperationListByPosition(info.getPosition())));
    }

    //发牌
    public void dealCard() {
        PlayerDeskInfo zhuangInfo = getZhuangInfo();
        List<PlayerDeskInfo> xianList = getXianList();
        for (int i = 0; i < 14; i++) {
            zhuangInfo.addHandCard(paiPool.poll());
        }
        logger.info("{}|{} 庄 的手牌个数{} : {}", getDeskId(), getGameId(), zhuangInfo.getHandCards().size(), JsonUtil.getJsonString(zhuangInfo.getHandCards()));

    	xianList.forEach(e -> {
    		for (int i = 0; i < 13; i++) {
    			e.addHandCard(paiPool.poll());
    		}
    		logger.info("{}|{} 其他玩家 的手牌个数{} : {}", getDeskId(), getGameId(), e.getHandCards().size(), JsonUtil.getJsonString(e.getHandCards()));
    	});
    }

    public long getGameId() {
        return this.gameId;
    }

    public abstract void doTing(PlayerInfo player, int value);

    public PlayerDeskInfo getCurrentTurnInfo() {
        return postion2InfoMap.get(currentTurn);
    }

    public PlayerDeskInfo getPreTurnInfo() {
        return postion2InfoMap.get(preTurn);
    }

    public PlayerInfo getCurrentPlayer() {
        PlayerDeskInfo info = getCurrentTurnInfo();
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            if (entry.getValue() == info) {
                return entry.getKey();
            }
        }
        return null;
    }

    public PlayerInfo getPreTurnPlayer() {
        PlayerDeskInfo info = getPreTurnInfo();
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            if (entry.getValue() == info) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean validOperation(PlayerInfo player, MajiangOperationType type) {
        MJPosition position = getPlayerPositionType(player);
        if (position == null) {
            return false;
        }
        if (currOperationTurn != position) {
            logger.debug("{}|{} the current operation  position is not  equal the need one",getDeskId(),getGameId());
            return false;
        }
        if (isSelfTurn(player)) {
            return true;
        }
        if (pengWating == getPlayerPositionType(player)){
        	return true;
        }
        for (Map.Entry<MJPosition, List<Integer>> operation : operationList.entrySet()) {
            if (operation.getKey() == position) {
                if (GameUtil.containOperation(operation.getValue(), type)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isHuIng() {
        return huIng;
    }

    public void setHuIng(boolean huIng) {
        this.huIng = huIng;
    }

    public void pengpai(PlayerInfo player) {
        if (!validOperation(player, MajiangOperationType.KE)) {
            return;
        }
        if (stop) {
			return;
		}
        if (!((gameState2 == MajongState2.CHU ||gameState2 == MajongState2.GUO))) {
            logger.info("{}|{} 玩家{} 当前状态 {} 不能碰牌",getDeskId(),getGameId(),player.getPlayerId(), gameState2);
            return;
        }

        // 誰打的牌
        PlayerDeskInfo info = getCurrentTurnInfo();
        if (info == null) {
            return;
        }
        int pengValue = info.getDeskPaiStack().peek();

        PlayerDeskInfo deskInfo = getPlayerDeskInfo(player);
        if (currOperationTurn != deskInfo.getPosition()) {
            logger.error("{}|{} 玩家{} 沒輪到該位置{}碰牌",getDeskId(),getGameId(),player.getPlayerId(), deskInfo.getPosition());
            return;
        }

        List<Integer> ops = operationList.get(currOperationTurn);

        if ((ops == null || !ops.contains(MajiangOperationType.KE.getValue())) && pengWating != deskInfo.getPosition()) {
            logger.error("{}|{} 玩家{} 該位置{}沒有操作列表,不能碰",getDeskId(),getGameId(),player.getPlayerId(), currOperationTurn);
            return;
        }

        if (!GameUtil.checkPeng(deskInfo, pengValue)) {
            return;
        }
        setGangFalse();
        removePositionOperation(deskInfo.getPosition());
        stopOperationFuture();
        if (hupaiRemain()){ // 如果后面有玩家可以胡牌 要确定后没人胡牌才能操作
        	pengWating = deskInfo.getPosition();
        	startDispatcherOperation();
        	return;
        }

        deskInfo.addHandTimes();
        deskInfo.addKe(pengValue);
        info.getDeskPaiStack().pop();
        msgHelper.notifyKe(player, pengValue);
        setCurrentTurn(getPlayerPositionType(player));

        setCurrentTurn(deskInfo.getPosition());
        currOperationTurn = currentTurn;

        logger.info("{}|{}玩家{}碰牌{}", getDeskId(), getGameId(), player.getPlayerId(), +pengValue);
        pengWating = null;
        setGameState2(MajongState2.PENG);
        thinkAftPeng();
    }

    public void gangPai(PlayerInfo player, int value) {
        stopAutoChuPaiBeginFuture();
        if (stop) {
            return;
        }
        if (!((gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.MO || gameState2 == MajongState2.CHU || gameState2 == MajongState2.GUO))) {
            logger.error("{}|{} 玩家{} 状态{}不对，不能杠牌",getDeskId(),getGameId(),player.getPlayerId(), gameState2);
            return;
        }

        if (gameState2 == MajongState2.GANG || isGangIng) {
            logger.error("{}|{} 状态{}不对，不能杠牌",getDeskId(),getGameId(), gameState2);
            return;
        }

        PlayerDeskInfo info = getPlayerDeskInfo(player);
        logger.info("{}|{} 位置{}杠牌{},當前狀態為{}",getDeskId(),getGameId(), info.getPosition(), value, gameState2);

        removePositionOperation(info.getPosition());
        if (hupaiRemain()){ // 如果后面有玩家可以胡牌 要确定后没人胡牌才能操作
        	gangWating = info.getPosition();
        	gangWatingValue = value;
        	startDispatcherOperation();
        	return;
        }
        
        if (currentTurn == info.getPosition()) {
            // 當前玩家為自己的時候
            if (info.getTotalCardCotainGangChike() != 14) {
                logger.error("{}|{} 玩家位置{}狀態出錯,當前輪到玩家槓,但牌的個數為{}",getDeskId(),getGameId(), info.getPosition(), info.getTotalCardCotainGangChike());
                return;
            } else {
                // 判斷牌能不能gang
                if (!GameUtil.checkGang(info, value, true)) {
                    return;
                }
                if (info.getKeQueue().contains(value)) {
                    info.addGang(value, true);            // 补杠
                } else {
                    value += GameConst.AN_MASK;         // 暗杠
                    info.addGang(value, true);
                }
            }
        } else {
            if (currOperationTurn != info.getPosition()) {
                logger.info("{}|{} 玩家狀態不對,沒輪到該位置{}槓牌",getDeskId(),getGameId(), info.getPosition());
                return;
            }
            if (!GameUtil.checkGang(info, getCurrentTurnInfo().getDeskPaiStack().peek(), false)) {
                return;
            }
            info.addGang(getCurrentTurnInfo().getDeskPaiStack().pop(), false);      // 点杠
            lastDianGangPos = currentTurn;
            operationList.clear();
        }

        setGangFalse();
        stopOperationFuture();
        removePositionOperation(info.getPosition());

        info.addHandTimes();
        info.setGangIng(true);
        logger.info("{}|{}| 玩家{}杠牌{}", getDeskId(), getGameId(), player.getPlayerId(), value);

        if (value > GameConst.AN_MASK || getPlayerPositionType(player) != currentTurn) {
            msgHelper.notifyGang(player, value);
//            info.setGangIng(false);
            setGameState2(MajongState2.GANG);
            calculateGang(player, value);
            logger.info("{}|{} 暗杠了或者点杠了{},直接发牌",getDeskId(),getGameId(),value);
            
            //value > GameConst.AN_MASK   lastGangType = 暗杠
            // lastGangType = 点杠
            
            setCurrentTurn(currOperationTurn);
            currOperationTurn = currentTurn;
            dealOneCard(getPlayerPositionType(player));
        } else {
            // 补杠暂时不结算，过的时候结算并且通知
            logger.info("{}|{}| 玩家{}补杠了{}", getDeskId(), getGameId(), player.getPlayerId(), value);
            logger.info("{}|{}| 玩家{}位置{}补杠,等待其他玩家操作",getDeskId(),getGameId(),player.getPlayerId(), info.getPosition());
            isGangIng = true;
            onPlayerGang(getPlayerPositionType(player), GameUtil.getRealValue(value));
        }
    }

    // 处理被抢杠胡的玩家
    public void dealGangTurnPao() {
        // 获取被抢杠的玩家
        PlayerInfo playerInfo = getGangTrunPaoPlayerDeskInfo();
        PlayerDeskInfo deskInfo = getPlayerDeskInfo(playerInfo);
        if (playerInfo != null && deskInfo.isGangIng()) {
            deskInfo.setGangIng(false);
            int value = deskInfo.getLastGangValue();
            deskInfo.removeBuGang();

            // 通知其他玩家被抢杠的玩家删除一张牌
//            msgHelper.notifyDelPai(deskInfo.getPosition(), value);
        }
    }

    // 获取被抢杠胡的玩家
    private PlayerInfo getGangTrunPaoPlayerDeskInfo() {
        Iterator<Map.Entry<PlayerInfo, PlayerDeskInfo>> its = player2InfoMap.entrySet().iterator();
        while (its.hasNext()) {
            Map.Entry<PlayerInfo, PlayerDeskInfo> it = its.next();
            if (it.getValue() != null && it.getValue().isGangIng()) {
                return it.getKey();
            }
        }
        return null;
    }


    protected void setGangFalse() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            info.setGangIng(false);
        }
    }
    
	protected void setOtherGangFalse(PlayerDeskInfo player) {
		for (PlayerDeskInfo info : player2InfoMap.values()) {
			if (player != info) {
				info.setGangIng(false);
			}
		}
	}
    
    protected void setHujiaoFalse() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            info.setHujiaoIng(false);
        }
    }

    protected abstract void calculateGang(PlayerInfo player, int value);


    public abstract void chiPai(PlayerInfo player, int type);

    public void guoPai(PlayerInfo player) {
//        logger.info("{}|{} 玩家{} 過牌的stack,{}",getDeskId(),getGameId(),player.getPlayerId(),getClassPath());
        logger.info("{}|{} 位置{}过牌",getDeskId(),getGameId(), getPlayerDeskInfo(player).getPosition());

//        // 只有碰/吃/杠/胡/听的状态才可以过
        if (!((gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.MO || gameState2 == MajongState2.GUO || gameState2 == MajongState2.CHU || gameState2 == MajongState2.CHI || isGangIng || gameState2 == MajongState2.GANG || isGangIng || gameState2 == MajongState2.HU || gameState2 == MajongState2.TING))) {
            logger.info("{}|{} 玩家{} 当前状态 {} 不能過牌",getDeskId(),getGameId(),player.getPlayerId(), gameState2);
            return;
        }
        logger.info("{}|{} 玩家{} 过牌当前状态 {} ",getDeskId(),getGameId(),player.getPlayerId(), gameState2);

        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (info == null) {
            return;
        }
        if (currOperationTurn != info.getPosition()) {
            logger.error("{}|{} 玩家{} 沒輪到該位置{}過牌，當前輪到{}操作",getDeskId(),getGameId(),player.getPlayerId(), info.getPosition(), currOperationTurn);
            return;
        }

        MJPosition position = getPlayerPositionType(player);
        if (position == null) {
            return;
        }
        if (position != currOperationTurn) {
            return;
        }

//        if (!operationList.containsKey(position)) {
//            logger.error("位置{}不包含操作，不能過牌", position);
//            return;
//        }

        logger.info("{}|{} 玩家{}过牌{}", getDeskId(), getGameId(),  player.getPlayerId(), getDeskId());

        if (isSelfTurn(player)) {
            // 玩家的回合,摸牌後，可以听，槓或胡，選擇過牌
            logger.info("{}|{} 自己回合內過牌{}",getDeskId(),getGameId(), position);

            long delay = 0;
            if (operationFuture != null) {
                delay = operationFuture.getDelay(TimeUnit.MILLISECONDS);
                logger.info("{}|{} 当前定时器的delay时间为:{}",getDeskId(),getGameId(),delay);
            }

            stopOperationFuture();
            removePositionOperation(position);

            setGameState2(MajongState2.GUO);

            if (!isPersonal()) {
                int useTime = (MiscUtil.getCurrentSeconds() - opBeginTime);

                int time = time8 - useTime * 1000;
                time = time > 0 ? time : 1000;
                if (delay > 0 && delay < time12) {      // 做一下时间控制,断点的时候时间可能不正确
                    time = (int) delay;
                }

                logger.info("{}|{} 玩家{} 自己回合內{}過牌,{} 毫秒後出牌",getDeskId(),getGameId(),player.getPlayerId(), position, time);
                opBeginTime = MiscUtil.getCurrentSeconds();
                sortHandCard(player);
                operationFuture = LogicActorManager.registerOneTimeTask(time, () -> discardCard(player, info.getLastHandCard()), getDeskId());
            }
            return;
        }

        setGameState2(MajongState2.GUO);

        stopOperationFuture();
        removePositionOperation(position);

        thinkAftGuo();
    }

    public boolean checkZiMo(PlayerInfo player,int huaNum) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        MJPosition position = getPlayerPositionType(player);
        if (info == null || position == null || position != currentTurn) {
            return false;
        }

//        List<Integer> ops = operationList.get(position);
//        if (ops != null && ops.contains(MajiangOperationType.HU.getValue())) {
////        }
//
        List<Integer> copyList = new ArrayList<>(info.getHandCards());
        if (MajongRule.checkHupai(copyList)) {
            stopOperationFuture();
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time8 + huaNum * GameConst.MO_HUA_TIME, () -> autoHu(player), getDeskId());
            return true;
        } else {
            return false;
        }
    }

    public void autoHu(PlayerInfo player) {
    	if(isDestroyed.get()){
    		logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
        logger.info("{}|{} 玩家{} 位置{}自动胡牌", getDeskId(),getGameId(),player.getPlayerId(), getPlayerDeskInfo(player).getPosition());

        PlayerDeskInfo info = getPlayerDeskInfo(player);
        MJPosition position = info.getPosition();
        if (info == null || position == null) {
            return;
        }

        List<Integer> cardList = new ArrayList<>(info.getHandCards());
        // 如果是自摸,不管。 如果是點炮的，需要獲取到胡的牌
        if (position != currentTurn) {
            // 單廂炮
            int dropCard;
            if (isHuIng()) {
                //  双向或者三向
                dropCard = GameUtil.getRealHuValue(getCurrentTurnInfo().getLastHuPaiCard()); // 現在存帶掩碼的值 自用需處理掩碼
            } else {
                dropCard = getCurrentTurnInfo().getDeskPaiStack().peek();
            }

            cardList.add(dropCard);
        }

        Pair<Integer, List<XueniuFanType>> maxType = null;
        Three<List<Integer>, List<Integer>, List<Integer>> maxHuList = null;
        List<Three<List<Integer>, List<Integer>, List<Integer>>> huResultList = MajongRule.getHuPaiResult(cardList);
        if (huResultList == null || huResultList.size() == 0) {
            return;
        }
        maxHuList = huResultList.get(0);
        hupai(player, maxHuList.getA(), maxHuList.getB(), maxHuList.getC());
    }

    public void removePositionOperation(MJPosition position) {
        operationList.remove(position);
    }

    //玩家准备
    @Override
    public void playerReady(PlayerInfo player) {
        logger.info("玩家准备{}",player.getPlayerId());
    	if (isGameing()){ // 游戲中的準備請求無需做操作
    		return;
    	}
        if (!isPersonal()) {
            if (!isDeskRemain()) {
//			LobbyGameManager.getInst().playerEnterRoom(player, player.getRoomId());
                return;
            }
        }
        if (!isPlayerMoneyEnough(player)) {
            player.write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
            return;
        }
        getPlayerDeskInfo(player).setReady(true);
        msgHelper.notifyReady(player);
        boolean allReady = true;
        for (PlayerDeskInfo playerDeskInfo : player2InfoMap.values()) {
            if (!playerDeskInfo.isReady()) {
                allReady = false;
                break;
            }
        }
        if (allReady && !isGameing.get()) {
            logger.info("准备开始游戏");
        	for (PlayerDeskInfo playerDeskInfo : player2InfoMap.values()) {
                playerDeskInfo.setReady(false); // 應客戶端要求  游戲重連需要拉到false  在游戲開始之後該值至成false
            }
            startNewGame();
        }
    }

    private boolean isDeskRemain() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            if (info.isLeave()) {
                return false;
            }
        }
        return true;
    }


    private List<Integer> getOperationListByPosition(MJPosition position) {
        for (Map.Entry<MJPosition, List<Integer>> value : operationList.entrySet()) {
            if (position == value.getKey()) {
                return value.getValue();
            }
        }
        return null;
    }

    protected void startDispatcherOperation() {
        if (this.operationList.size() == 0) {
            logger.info("{}|{} 当前没有可操作行为，状态为{}",getDeskId(),getGameId(), gameState2);
//            if (gameState == MajongState.DROP_CARD) {
            setGangFalse();
            stopOperationFuture();
            removePositionOperation(getCurrentTurn());
            setCurrentTurn(nextPosition());
            currOperationTurn = currentTurn;
            logger.info("{}|{} 1s后下一个玩家摸牌",getDeskId(),getGameId());
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
//            }
        } else {
            dispatcherOneOperation();
        }
    }

    private Map<MJPosition, MajiangOperationType> getMaxPriorityOperationOfPosition() {
        logger.info("判断操作的优先级");
        Map<MJPosition, MajiangOperationType> maxPriority = new HashMap<>();
        for (Map.Entry<MJPosition, List<Integer>> info : operationList.entrySet()) {
            MajiangOperationType type = GameUtil.getMaxPriorityType(info.getValue());
            if (type == MajiangOperationType.GUO_GUO) {
                continue;
            }
            maxPriority.put(info.getKey(), GameUtil.getMaxPriorityType(info.getValue()));
            logger.info("位置{}的最高优先级操作为{}", info.getKey(), maxPriority.get(info.getKey()));
        }
        return maxPriority;
    }

    @Override
    protected void onGameEnd() {
        if (isGameing()) {
            Gson gson = JsonUtil.getGson();
            int recordId = TaxRecordUtil.recordGamReply(gson.toJson(record), startTime, player2InfoMap.keySet(), getGameType(), String.valueOf(getGameId()));
            recordIdList.add(recordId);
        }
        player2InfoMap.values().forEach(e -> e.setReady(false));
        stopOperationFuture();
        isGameing.set(false);                                            //游戏结束之后要做的事情完全不同了
        //要销毁就全部销毁了啊, 没有说有一个有一个没有的啊
        if (!isPersonal() && destroyDesk()) {
            for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
//               LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, entry.getKey().getPlayerId()));
            	 DeskMgr.getInst().removePlayer(entry.getKey(),true);
            }
            stopOperationFuture();
            stopAutoChuPaiBeginFuture();
            DeskMgr.getInst().removeDesk(this);
        }
        //什么时候销毁这个桌子 DeskMgr.removeDesk              当这个桌子不需要存在的时候, 这个桌子就没存在的必要了
        if (rounds == maxRounds || isTimeOver) {
            //要解散房间了啊
            onRoomDeskGameEnd();
        }
        
        if(LogicApp.getInst().isStop()){
    	    DeskMgr.getInst().removeDesk(this);
    		return;
       }
    }

    private boolean isPlayerMoneyEnough(PlayerInfo info) {
        if (this.createId == 0) {
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
        if (info.getCoin() < getBaseScore() * getEnterTimes()) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean destroyDesk() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            if (info.isLeave()) {
                return true;
            }
        }
        return false;
    }

    protected abstract void clearDeskInfo();

    public boolean isGameing() {
        return isGameing.get();
    }

    protected abstract void dealOneCard(MJPosition position);

    public int getRounds() {
        return rounds;
    }

    public PlayerInfo getNextPlayer(PlayerInfo player) {
        PlayerDeskInfo oppositeInfo = getNexPlayerDeskInfo(player);
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            if (entry.getValue() == oppositeInfo)
                return entry.getKey();
        }
        return null;
    }

    public PlayerDeskInfo getPlayerDeskInfo(PlayerInfo player) {
        return player2InfoMap.get(player);
    }

    //出牌
    public void discardCard(PlayerInfo player, int value) {
    	if(isDestroyed.get()){
    		logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
        // 關閉掉開始自動出牌的定時器
        stopAutoChuPaiBeginFuture();
//        logger.info("{}|{}玩家{} 出牌的stack,{}",getDeskId(),getGameId(),player.getPlayerId(),getClassPath());

        if (stop) {
            logger.info("{}|{} the game was stop  and you can't discard card ",getDeskId(),getGameId());
            return;
        }
        // 上一个状态为摸牌，碰牌，吃牌，听牌，才能出牌
        if (!((gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.GUO || gameState2 == MajongState2.MO || isGangIng || gameState2 == MajongState2.PENG || gameState2 == MajongState2.CHI || gameState2 == MajongState2.TING))) {
            logger.error("{}|{} 当前状态为{}，不能出牌，位置為{}", getDeskId(), getGameId(), gameState2, getPlayerDeskInfo(player).getPosition());
            return;
        }

        if (!canDropCard(player, value)) {
            logger.info(" can't drop card ");
            return;
        }
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (info.getTotalCardCotainGangChike() != 14) {
            return;
        }

        if (currentTurn != info.getPosition() || currOperationTurn != info.getPosition()) {
            logger.error("{}|{} 沒有輪到該位置{}操作，當前操作位置為{}", getDeskId(), getGameId(), info.getPosition(), currentTurn);
            return;
        }

        // 判斷當期那玩家有沒有此牌型
        if (!info.haveCard(value)) {
            logger.error("{}|{} 玩家{}沒有該手牌{}", getDeskId(), getGameId(), player.getPlayerId(), value);
            return;
        }

        stopOperationFuture();
        removePositionOperation(info.getPosition());
        pengWating = null;
        info.setSelfGang(false);
        info.discardCard(value);        //if the target player is login out then  help him
        msgHelper.notifyDropCard(player, value);
        logger.info("{}|{} 玩家 {}出牌 {}", getDeskId(), getGameId(), player.getPlayerId(), value);

        setGameState2(MajongState2.CHU);
        thinkAftChu();
    }

    protected void setGameState2(MajongState2 state){
        gameState2 = state;
    }

    private void onPlayerGang(MJPosition position, int value) {
        operationList.clear();
        for (Map.Entry<MJPosition, PlayerDeskInfo> entry : postion2InfoMap.entrySet()) {
        	if (entry.getValue().isDefeat()) {
        		continue;
        	}
            if (entry.getValue().isLeave()) {
                continue;
            }
            if (entry.getKey() == position) {
                continue;
            }
            if (getGameType() == GameType.XUEZHAN && entry.getValue().isHuPai()) {
                continue;
            }
            List<Integer> copyList = new ArrayList<>(entry.getValue().getHandCards());
            copyList.add(value);
            if (MajongRule.checkHupai(copyList)) {
                operationList.put(entry.getKey(), Arrays.asList(MajiangOperationType.HU.getValue()));
                logger.info("{}|{} 杠后位置{}能胡牌",getDeskId(),getGameId(), entry.getKey());
            }
        }
        startDispatcherBuGang();
    }

    private void startDispatcherBuGang() {
        // 進入到補槓的方法一定是自己的回合
        if (this.operationList.size() == 0) {
            isGangIng = false;
            setGameState2(MajongState2.GANG);
            // 其他人无操作,补杠
            PlayerInfo playerInfo = getPlayerByPosition(currentTurn);
//            getCurrentTurnInfo().setGangIng(false);
            getCurrentTurnInfo().setSelfGang(true); // 添加 自己杠参数 用来判断 自己杠完自己胡的 刚上开花
            msgHelper.notifyGang(playerInfo, getCurrentTurnInfo().getLastGangValue());
            calculateGang(playerInfo, getCurrentTurnInfo().getLastGangValue());

            logger.info("{}|{} 杠后其他玩家没有操作，1s 后 杠的玩家摸牌", getDeskId(),getGameId());
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
        } else {

            logger.info("{}|{} 杠后其他玩家有操作", getDeskId(),getGameId());
            dispatcherOneBuGangOperation();
        }
    }

    private void dispatcherOneBuGangOperation() {
        Map<MJPosition, MajiangOperationType> priorityMap = getMaxPriorityOperationOfPosition();
        if (priorityMap == null || priorityMap.size() == 0) {
            logger.info("{}|{} 杠后这里能出现？？？？？？？？？？？？？？", getDeskId(),getGameId());
            stopOperationFuture();
            removePositionOperation(getCurrentTurn());
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(getCurrentTurn()), getDeskId());
            return;
        }
        MJPosition operationPosition = GameUtil.switchPositionToOperation(currentTurn, priorityMap);
        if (operationPosition == null) {
            return;
        }
        currOperationTurn = operationPosition;
        List<Integer> opers = getOperationListByPosition(operationPosition);
        logger.info("{}|{} 杠后位置{}可以有操作...", getDeskId(),getGameId(),currOperationTurn,JsonUtil.getJsonString(opers));

        int huValue = 0;
        if (opers != null && opers.size() > 0 && opers.stream().anyMatch(e -> e.intValue() == MajiangOperationType.HU.getValue())) {
            huValue = getCurrentTurnInfo().getLastGangValue();
            logger.info("{}|{} 杠后位置{}可以胡牌{}...", getDeskId(),getGameId(), currOperationTurn,huValue);
        }

        msgHelper.notifyWantOperation(operationPosition, opers, huValue);

        if (huValue != 0) {
            // 判断是否胡牌
            logger.info("{}|{} 杠后 9s后位置{}自动胡牌", getDeskId(),getGameId(), operationPosition);
            stopOperationFuture();
            if (!isPersonal()) {
                opBeginTime = MiscUtil.getCurrentSeconds();
                operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> autoHu(getPlayerByPosition(operationPosition)), getDeskId());
            }
            return;
        }

        logger.info("{}|{} 杠后 9s后位置{}自动过牌", getDeskId(),getGameId(),operationPosition);
        stopOperationFuture();
        if (!isPersonal()) {
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> guoPai(getPlayerByPosition(operationPosition)), getDeskId());
        }
    }


    private void startDispatcherGang() {
        if (this.operationList.size() == 0) {
            logger.info("{}|{} 杠后没操作了，杠的玩家摸牌", getDeskId(),getGameId());

            setCurrentTurn(getGangPosition());
            currOperationTurn = currentTurn;
            stopOperationFuture();
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
        } else {
            logger.info("{}|{} 杠后其他玩家有操作", getDeskId(),getGameId());
            dispatcherOneOperation();
        }
    }

    private void dispatcherOneOperation() {
        Map<MJPosition, MajiangOperationType> priorityMap = getMaxPriorityOperationOfPosition();
        if (priorityMap == null || priorityMap.size() == 0) {
            logger.info("{}|{} 居然没有操作了？？？？？？", getDeskId(),getGameId());
            stopOperationFuture();
            removePositionOperation(getCurrentTurn());
            setCurrentTurn(nextPosition());
            currOperationTurn = currentTurn;
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
            return;
        }
        MJPosition operationPosition = GameUtil.switchPositionToOperation(currentTurn, priorityMap);
        if (operationPosition == null) {
            return;
        }

        logger.info("{}|{} 根据当前位置{}判断可以獲取最近切優先級高的操作位{}", getDeskId(),getGameId(), currentTurn, operationPosition);
        currOperationTurn = operationPosition;

        List<Integer> opers = getOperationListByPosition(operationPosition);

        int huValue = 0;
        if (opers != null && opers.size() > 0 && opers.stream().anyMatch(e -> e.intValue() == MajiangOperationType.HU.getValue())) {
            huValue = getHuValue(getPlayerByPosition(operationPosition));
            logger.info("{}|{} 获取当前操作位{}可以胡的值{}", getDeskId(),getGameId(), currOperationTurn, huValue);
        }
        if (isHuIng()){ // 已經有人胡了  後面只可能有胡的操作
        	Iterator<Integer> it = opers.iterator();
        	while (it.hasNext()){
        		if (it.next() != MajiangOperationType.HU.getValue()){
        			it.remove();
        		}
        	}
        }
        msgHelper.notifyWantOperation(operationPosition, opers, huValue);

        if (isPersonal()) {
            return;
        }
        opBeginTime = MiscUtil.getCurrentSeconds();
        logger.info("{}|{} 判断当前操作位{}是否可以胡牌{}", getDeskId(),getGameId(), currOperationTurn, huValue > 0 ? "是" : "否");
        if (huValue > 0) {
            // 判断是否胡牌
            if (!isPersonal()) {
                logger.info("{}|{} 开启胡牌定时器,9s后位置{}自动胡牌", getDeskId(),getGameId(), operationPosition);
                stopOperationFuture();
                operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> autoHu(getPlayerByPosition(operationPosition)), getDeskId());
                return;
            }
        }
        if (!isPersonal()) {
            logger.info("{}|{} 开启过牌定时器,9s后位置{}自动过牌", getDeskId(),getGameId(), operationPosition);
            stopOperationFuture();
            operationFuture = LogicActorManager.registerOneTimeTask(time8, () -> guoPai(getPlayerByPosition(operationPosition)), getDeskId());
        }
    }


    public void disbandDesk() {
        //这个位置先直接通知center游戏结束
        onAllCardOver(true);
        onRoomDeskGameEnd();
    }

    public void disBankDeskTimeOver() {
    	if(isDestroyed.get()){
    		logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
    	logger.info("{}桌子{}期限到了,解算中.",getGameType(),getDeskId());
        if (isAllPlayerLeave() || !isGameing()) {
            onRoomDeskGameEnd();
        } else {
            // 下一把結束
            isTimeOver = true;
        }
    }


    protected boolean validHuPai(PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> jiang, int huValue) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        List<Integer> handCards = info.getHandCardsCopy();
        if (info.getTotalCardCotainGangChike() == 13) {
            handCards.add(huValue);
        }
        for (Integer value : chi) {
            if (!handCards.remove(value) || !handCards.remove((Object) (value + 1)) || !handCards.remove((Object) (value + 2))) {
                return false;
            }
        }
        for (Integer value : ke) {
            if (!handCards.remove(value) || !handCards.remove(value) || !handCards.remove(value)) {
                return false;
            }
        }
        if (info.getTotalCardCotainGangChike() == 14) {
            for (Integer value : jiang) {
                if (!handCards.remove(value) || !handCards.remove(value)) {
                    return false;
                }
            }
        } else {
            for (Integer value : jiang) {
                if (!handCards.remove(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void stopOperationFuture() {
        if (operationFuture != null) {
            operationFuture.cancel(true);
            operationFuture = null;
        }
    }

    protected void stopAutoChuPaiBeginFuture() {
        if (autoChuPaiBeginFuture != null) {
            autoChuPaiBeginFuture.cancel(true);
            autoChuPaiBeginFuture = null;
        }
    }

    public void hupaiRequest(PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> jiang) {
        if (!isGameing()) {
            logger.debug("not in game  you hu what ?");
            return;
        }
        if (!validHuPai(player, chi, ke, jiang, getHuValue(player))) {
            return;
        }
        if (!validOperation(player, MajiangOperationType.HU)) {
            return;
        }
        stopOperationFuture();
        hupai(player, chi, ke, jiang);
    }

    protected boolean isZhuangjia(PlayerInfo player) {
        return player2InfoMap.get(player).isZhuang();
    }

    protected boolean isSelfTurn(PlayerInfo player) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (info == null) {
            return false;
        }
        return info.getTotalCardCotainGangChike() == 14;
    }

    protected void switchZhuangPlayer(MJPosition position) {
        for (Map.Entry<MJPosition, PlayerDeskInfo> entry : postion2InfoMap.entrySet()) {
            if (entry.getKey() == position) {
                entry.getValue().setZhuang(true);
                setCurrentTurn(entry.getKey());
                currOperationTurn = entry.getKey();
            } else {
                entry.getValue().setZhuang(false);
            }
        }
    }

    public MJPosition getCurrentTurn() {
        return this.currentTurn;
    }

    public int getHuValue(PlayerInfo player) {
        if (getPlayerDeskInfo(player).getTotalCardCotainGangChike() == 14) {
            return getPlayerDeskInfo(player).getLastHandCard();
        } else {
            if (isHuIng()) {
                return GameUtil.getRealHuValue(getCurrentTurnInfo().getLastHuPaiCard()); // 現在存帶掩碼的值 自用需處理掩碼
            } else {
                if (isGangIng) {
                    return getCurrentTurnInfo().getLastGangValue();
                } else {
                    return getCurrentTurnInfo().getDeskPaiStack().peek();
                }
            }
        }
    }

    protected PlayerDeskInfo getZhuangInfo() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            if (info.isZhuang()) {
                return info;
            }
        }
        return null;
    }

    @Override
    public boolean isPlayerLeave(PlayerInfo player) {
        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (info == null) {
            return true;
        }
        return info.isLeave();
    }

    public int getZhuangId() {
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            if (entry.getValue().isZhuang()) {
                return entry.getKey().getPlayerId();
            }
        }
        return 0;
    }

    public PlayerInfo getPlayerByPosition(MJPosition position) {
        PlayerDeskInfo info = postion2InfoMap.get(position);
        if (info == null) {
            return null;
        }
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            if (entry.getValue() == info) {
                return entry.getKey();
            }
        }
        return null;
    }


    private List<PlayerDeskInfo> getXianList() {
        List<PlayerDeskInfo> infoList = new ArrayList<>();
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            if (info.isZhuang()) {
                continue;
            }
            infoList.add(info);
        }
        return infoList;
    }

    public Map<PlayerInfo, PlayerDeskInfo> getPlayer2InfoMap() {
        return this.player2InfoMap;
    }

    @Override
    public boolean isTiyan() {
        return tiyan;
    }

    public void setTiyan(boolean tiyan) {
        this.tiyan = tiyan;
    }

    protected Map<Integer, Integer> getAllPlayerMoney() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : player2InfoMap.entrySet()) {
            result.put(entry.getValue().getPositionValue(), entry.getKey().getCoin());
        }
        return result;
    }


    public Collection<PlayerDeskInfo> getPlayerDeskInfList() {
        return new ArrayList<>(player2InfoMap.values());
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return new ArrayList<>(player2InfoMap.keySet());
    }

    public void notifyPlayerEnter() {
        msgHelper.notifyPlayerEnter(this);
    }

    protected MajongMsgHelper msgHelper = new MajongMsgHelper();

    public class MajongMsgHelper {

        protected void notifyPlayerEnter(MJDesk desk) {
            notifyMessage(ResponseCode.COUPLE_ENTER_DESK, CoupleCreator.createPBPlayerInfoList(player2InfoMap));
        }

        protected void notifyReady(PlayerInfo player) {
            notifyMessage(ResponseCode.COUPLE_GAME_READY, CommonCreator.createPBInt32(getPlayerPosition(player)));
        }

        protected void notifyKe(PlayerInfo player, int value) {
            operationList.clear();
            notifyMessage(ResponseCode.COUPLE_KE, CommonCreator.createPBPair(getPlayerPosition(player), value));
            record.addPlayerStep(2, value, getPlayerPosition(player));
        }

        protected void notifyGang(PlayerInfo player, int value) {
            record.addPlayerStep(1, value, getPlayerPosition(player));
            if (value > GameConst.AN_MASK) {
                for (PlayerInfo player1 : player2InfoMap.keySet()) {
                    if (getGameType() != GameType.COUPLE_MJ) {
                        player1.write(ResponseCode.COUPLE_GANG, CommonCreator.createPBPair(getPlayerPosition(player), value));
                    } else {
                        if (player == player1) {
                            player1.write(ResponseCode.COUPLE_GANG, CommonCreator.createPBPair(getPlayerPosition(player), value));
                        } else {
                            player1.write(ResponseCode.COUPLE_GANG, CommonCreator.createPBPair(getPlayerPosition(player), 0));
                        }
                    }
                }
            } else {
                notifyMessage(ResponseCode.COUPLE_GANG, CommonCreator.createPBPair(getPlayerPosition(player), value));
            }
        }

        protected void notifyDelPai(MJPosition position, int value) {
            for (PlayerInfo player1 : player2InfoMap.keySet()) {
                if (getPlayerPositionType(player1) == position) {
                    player1.write(ResponseCode.COUPLE_DEL_VALUE, CommonCreator.createPBPair(position.getValue(), value));
                } else {
//                    player1.write(ResponseCode.COUPLE_DEL_VALUE, CommonCreator.createPBPair(position.getValue(), 0));
                }
            }
        }

        protected void notifyMoPai(MJPosition position, List<Integer> pairList, Integer pai) {
            for (PlayerInfo player1 : player2InfoMap.keySet()) {
                if (getPlayerPositionType(player1) == position) {
                    player1.write(ResponseCode.COUPLE_DEAL_ONE_CARD, CoupleCreator.createPBDealOneCardRes(position.getValue(), pairList));
                } else {
                    List<Integer> result = new ArrayList<>(pairList);
                    result.remove(pai);
                    result.add(0);
                    player1.write(ResponseCode.COUPLE_DEAL_ONE_CARD, CoupleCreator.createPBDealOneCardRes(position.getValue(), result));
                }
            }
            record.addPlayerStep(8, pairList, position.getValue());
        }

        protected void notifyLiuju() {
            notifyMessage(ResponseCode.COUPLE_LIUJU, null);
        }

        protected void notifyChi(PlayerInfo player, int chi, int value) {
            operationList.clear();
            notifyMessage(ResponseCode.COUPLE_CHI, CommonCreator.createPBTriple(getPlayerPosition(player), chi, value));
            record.addPlayerStep(3, Arrays.asList(chi, value), getPlayerPosition(player));
        }

        protected void notifyDropCard(PlayerInfo player, int value) {
            notifyMessage(ResponseCode.COUPLE_DROP_CARD, CommonCreator.createPBPair(getPlayerPosition(player), value));
            record.addPlayerStep(7, Arrays.asList(value), getPlayerPosition(player));
        }

        protected void notifyTing(PlayerInfo player) {
            notifyMessage(ResponseCode.COUPLE_DO_TING, CommonCreator.createPBInt32(getPlayerPosition(player)));
            record.addPlayerStep(4, 0, getPlayerPosition(player));
        }

        protected void notifyWantOperation(MJPosition position, List<Integer> operationList, int huValue) {
            for (PlayerInfo player1 : player2InfoMap.keySet()) {
                if (getPlayerPositionType(player1) == position) {
                    player1.write(ResponseCode.COUPLE_WANT_OPERATION, CoupleCreator.createPBWatOperationRes(position.getValue(), operationList, huValue));
                } else {
                    player1.write(ResponseCode.COUPLE_WANT_OPERATION, CoupleCreator.createPBWatOperationRes(position.getValue(), null, 0));
                }
            }
        }

        protected void notifyGameStart() {
            player2InfoMap.forEach((e, f) -> {
                CoupleMajiang.PBCoupleMJStartRes builder = CoupleCreator.createPBCoupleMJStartRes(MJDesk.this, f);
                e.write(ResponseCode.COUPLE_DEAL_CARD, builder);
            });
        }

        protected void notifyMessage(ResponseCode code, MessageLite message) {
            for (PlayerInfo player : player2InfoMap.keySet()) {
                player.write(code, message);
            }
        }
    }

    protected abstract int getEnterTimes();

    protected abstract void onAllCardOver(boolean disband);

    protected abstract void checkHuapai();

    public abstract PlayerDeskInfo getNexPlayerDeskInfo(PlayerInfo player);

    public abstract MJPosition nextPosition();

    public abstract void tingpai(PlayerInfo player, CoupleMajiang.PBTingReq request);

    protected abstract void initMjGameDeskInfo(List<PlayerInfo> playerList);

    public abstract void hupai(PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> jiang);

    //洗牌
    protected abstract void mixAllCard();

    protected abstract void selectZhuang();

    protected abstract void selectMenfeng();

    protected abstract MajongType getIgnoreType(MJPosition player);

    protected abstract boolean canDropCard(PlayerInfo player, int card);

    protected abstract boolean canChi();

    public abstract void sortHandCard(PlayerInfo player);
    
    public abstract boolean dealHuInfo();
    
    protected void afterHuPai(MJPosition position, boolean zimo) {
        removePositionOperation(currOperationTurn);
        setCurrentTurn(currOperationTurn);
        currOperationTurn = currentTurn;

        if (hupaiRemain()) {
            if (isGangIng) {
                logger.info("{}|{} 胡牌后还可以胡?杠上开", getDeskId(),getGameId());
                startDispatcherGang();
            } else {
                logger.info("{}|{} 胡牌后还可以胡牌?非杠上开", getDeskId(),getGameId());
                startDispatcherOperation();
            }
        } else {
            logger.info("{}|{} 胡牌后没有操作了，换下一步", getDeskId(),getGameId());
            logger.info("{}|{} 當前的位置為{}，當前操作位置為{}", getDeskId(),getGameId(),currentTurn,currOperationTurn);

            //这个位置应该是放炮的时候才会有这一步操作，如果不是放炮，胡的子应该是在玩家手中
            if (!isSelfTurn(getPlayerByPosition(position)) && !zimo) {        // 若是自摸的，不参与
                if (dianPaoPos != null && !isGangIng) {
                    // 點炮的移除牌
                    getPlayerDeskInfo(getPlayerByPosition(dianPaoPos)).getDeskPaiStack().pop();
//                    getCurrentTurnInfo().getDeskPaiStack().pop();
                }
            }
            
            if(dealHuInfo()){         // 全部胡完之后处理结算
            	return;   // 游戲結束
            }
            isGangIng = false;
            setGangFalse();
            stopOperationFuture();
            setCurrentTurn(nextPosition());
            currOperationTurn = currentTurn;
            logger.info("{}|{} 1s后下一个玩家摸牌", getDeskId(),getGameId());
            opBeginTime = MiscUtil.getCurrentSeconds();
            operationFuture = LogicActorManager.registerOneTimeTask(time1, () -> dealOneCard(currentTurn), getDeskId());
        }
    }

    public MJPosition getGangPosition() {
        for (PlayerDeskInfo info : player2InfoMap.values()) {
            if (info.isGangIng()) {
                return info.getPosition();
            }
        }
        return null;
    }

    //检查里面是否还有可以胡牌的操作

    private boolean hupaiRemain() {
        for (List<Integer> operList : operationList.values()) {
            if (GameUtil.containOperation(operList, MajiangOperationType.HU)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void recordPlayerTaxInfo(Object detail, int roomId) {
        MjDetail mj_detail = (MjDetail) detail;
        for (OnePosDetail one : mj_detail.getRecords()) {
            if (0 >= one.getTax()) {
                continue;
            }

            TaxRecordUtil.recordPlayerTaxInfoToDB(mj_detail.getType(), one.getPlayerId(), roomId, one.getTax(), one.getChannel_id(), one.getPackage_id(), one.getDevice());
        }
    }


    public static String getClassPath() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return JsonUtil.getJsonString(stackTraceElements);
    }
    
    public void addHuInfo(HuInfo huInfo){
    	huList.add(huInfo);
    }

	public List<HuInfo> getHuList() {
		return huList;
	}
    
	@Override
	public void destroy() {
		isDestroyed.set(true);
		stopOperationFuture();
		stopAutoChuPaiBeginFuture();
		stopOverMyselfFuture();
		player2InfoMap.clear();
		postion2InfoMap.clear();
	}
	
	@Override
	public void playerWantContinue(PlayerInfo player){
		
	}

}
