package logic.poker.niuniu.zhuang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import config.CoupleRoomInfoProvider;
import config.JsonUtil;
import config.bean.ConfMatchCard;
import config.bean.CoupleRoom;
import config.bean.GrabNiuConfig;
import config.bean.PersonalConfRoom;
import config.provider.ConfNiuProvider;
import config.provider.PersonalConfRoomProvider;
import data.MoneySubAction;
import logic.DeskMgr;
import logic.define.GameType;
import logic.majiong.PlayerInfo;
import logic.poker.PokerCard;
import logic.poker.PokerDesk;
import logic.poker.PokerMatchCardUtil;
import logic.poker.PokerUtil;
import logic.poker.niuniu.NiuResult;
import logic.poker.niuniu.XianPosition;
import logic.record.TaxRecordUtil;
import logic.record.detail.GrabNiuDetail;
import logic.record.detail.OnePosDetail;
import logic.record.niu.GrabNiuRecord;
import packet.CocoPacket;
import protobuf.CoupleMajiang;
import protobuf.Oxc;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;

public abstract class ZhuangNiuDesk extends PokerDesk {
    private static final Logger logger = LoggerFactory.getLogger(ZhuangNiuDesk.class);

    static int minReadTime = 2;// 最小等待时间

    protected Map<PlayerInfo, RoomNiuNiuDeskInfo> deskInfoMap = new HashMap<>();
    protected Map<XianPosition, RoomNiuNiuDeskInfo> posInfoMap = new HashMap<>();

    protected RoomNiuNiuDeskInfo zhuangDesk;  // 庄的那桌

    protected ScheduledFuture<?> deskFuture = null;

    protected ZhuangNiuState state;
    protected int stateRemainTime;
    protected long gameId;

    protected int startTime;
    protected int curRound = 1;
    
    //--私房需要的变量--
    protected int baseScore;
    protected int enterTimes;
    protected int createId;
    protected int multi;//默认庄时倍数
    protected NiuNiuZhuangType rule;  // 庄类型
    /** 1经典 2看牌 */
    protected int model;//看牌还是经典

    protected GrabNiuDetail detail;
    /** 客户端回放记录 */
    protected GrabNiuRecord record;
    private int totalTax = 0;//总税收
    
    /** 战绩数据 */
    protected List<Integer> recordIdList = new ArrayList<>();
    protected List<Pair<Integer, Map<XianPosition, Integer>>> scoreList = new ArrayList<>();
    /** 本局输赢数据 */
    protected Map<XianPosition, Integer> scoreHis = new HashMap<>();
    
    private GrabNiuConfig config;
    private Map<Integer,Integer> niuMap;

    public ZhuangNiuDesk(int deskId, int roomId, List<PlayerInfo> playerList,int model,NiuNiuZhuangType rule) {
        super(deskId,roomId);
        niuMap = ConfNiuProvider.getInst().getConfNiu(0).getNiuResultMap();
        config = CoupleRoomInfoProvider.getInst().getGrabNiuConf(getConfId());
        
        this.model = model;
        this.rule = rule;
        baseScore = getBaseScore();
        multi = 1;
        
        initRoomNiuNiuDeskInfo(playerList);
        initRecord();
        beginDeskFuture();
    }
    
    /** 私房入口 */
    public ZhuangNiuDesk(int deskId, int creatorId, int maxRounds, int baseScore, boolean personal,
			List<PlayerInfo> playerList, int enterTimes,int rule,int mulit,int model) {
    	super(deskId,0);
    	niuMap = ConfNiuProvider.getInst().getConfNiu(0).getNiuResultMap();
    	config = CoupleRoomInfoProvider.getInst().getGrabNiuConf(-getGameType().getValue());
    	
    	setPersonal(personal);
    	this.model = model;
    	this.baseScore = baseScore;
    	this.enterTimes = enterTimes;
    	this.createId = creatorId;
    	this.multi = mulit;
    	this.rule = NiuNiuZhuangType.getByValue(rule);
    	
    	if(this.rule == NiuNiuZhuangType.GrabZhuang){
    		mulit = 1;
    	}
    	
    	initRoomNiuNiuDeskInfo(playerList);
    	initRecord();
    	setBegain();
	}
    
    private void initRecord(){
    	record = new GrabNiuRecord(getGameType().getValue(), isPersonal() ?  getDeskId() : getConfId(), baseScore,model,rule.getValue(),config.getGrabZhuang().get(multi-1));
        detail = new GrabNiuDetail(getGameType().getValue());
    }
	
	private void setBegain(){
		this.gameId = geneGameNo();
        deskInfoMap.values().forEach(e -> e.clear());
        setState(ZhuangNiuState.WATT);
        startTime = MiscUtil.getCurrentSeconds();
        totalTax = 0;
        initRecord();
	}

    private void beginDeskFuture() {
    	stopOperationFuture();
    	setBegain();
        deskFuture = LogicActorManager.getTimer().register(1000, 1000, () -> onUpdate(), LogicActorManager.getDeskActor(getDeskId()), "grabNiu_update");
    }
    
    private void stopOperationFuture() {
		if (deskFuture != null) {
			deskFuture.cancel(true);
			deskFuture = null;
		}
	}
    
    @Override
	public boolean isGameing() {
		return false;
	}
    
    public void disBankDeskTimeOver() {
    	if(isDestroyed.get()){
    		logger.info("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
    	logger.info("{}桌子{}期限到了,解算中.",getGameType(),getDeskId());
    	disbandDesk();
    }

    /**
     * 加入人员
     *
     * @param player
     */
    public void enterPlayer(PlayerInfo player) {
    	if(isPersonal()){
    		 logger.info("玩家{}进入到{}桌子{},当前是私房不能进入", player.getPlayerId(),getGameName(), this.deskId, state);
    		return;
    	}
        int enterTime = MiscUtil.getCurrentSeconds();
        RoomNiuNiuDeskInfo info = new RoomNiuNiuDeskInfo();

        info.setPostion(XianPosition.getFree(posInfoMap));
        info.setPlayerInfo(player);
        info.setEnterTime(enterTime);
        player.setPosition(info.getPositionValue());
        info.clear();
        if(state != null && state != ZhuangNiuState.WATT && state != ZhuangNiuState.PRE_REDAY){
        	info.setWatch(true);
        }
        deskInfoMap.put(player, info);
        posInfoMap.put(info.getPostion(), info);
        DeskMgr.getInst().registerDesk(player.getPlayerId(), this);

        logger.info("玩家{}进入到{}桌子{},当前状态{}", player.getPlayerId(),getGameName(), this.deskId, state);
        //推送其他玩家我进入桌
        msgHelper.notifyMessageNoSelf(ResponseCode.ZJH_ENTER_DESK, CoupleCreator.createGrabNiuPlayerInfo(player, info),player);
        //返回当前桌上玩家，客户端会请求重新进入游戏playerReLogin
  		player.write(ResponseCode.COUPLE_ENTER_DESK, CoupleCreator.createPBPlayerInfoListGradNiu(deskInfoMap));
    }

    @Override
	public boolean isAllPlayerLeave() {
        boolean isAllLeave = true;
        for (RoomNiuNiuDeskInfo deskInfo : deskInfoMap.values()) {
			if(!deskInfo.isLeave()){
				isAllLeave = false;
				break;
			}
		}
        return isAllLeave;
	}

    private void leavePlayer(RoomNiuNiuDeskInfo info) {
    	PlayerInfo player = info.getPlayerInfo();
    	logger.info("玩家{}移除{} 房间{},观察{}",player.getPlayerId(),getGameName(),getDeskId(),info.isWatch());
    	posInfoMap.remove(getDeskInfo(player).getPostion());
    	deskInfoMap.remove(player);
    	DeskMgr.getInst().removePlayer(player,false);
//      LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_LEAVE_ROOM, null, player.getPlayerId()));
    }
    
	public void playerLeave(PlayerInfo player) {
        RoomNiuNiuDeskInfo info = deskInfoMap.get(player);
        if (info == null) {
            return;
        }
        info.setLeave(true);
       
        logger.info("玩家{}离开{} 房间{},观察{}",player.getPlayerId(),getGameName(),getDeskId(),info.isWatch());
        
        boolean canLeave = false;
        if(!isPersonal()){
        	 //是否能离开桌
            if(info.isWatch()){
            	canLeave = true;
            }
            boolean gameNoStart = false;
            if(state == null || state == ZhuangNiuState.WATT || state == ZhuangNiuState.PRE_REDAY){
            	canLeave = true;
            	gameNoStart = true;
            }else if(state == ZhuangNiuState.CALCULATE){
            	gameNoStart = true;
            }
            if(gameNoStart && isAllPlayerLeave()){
        		DeskMgr.getInst().removeDesk(this);
        		return;
        	}
        }else{
        	canLeave = false;
        }
        
        info.setLeave(false);
        if(canLeave){
        	msgHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(info.getPostion().getValue()));
        	info.setLeave(true);
        	leavePlayer(info);
        }else{
        	msgHelper.notifyMessage(ResponseCode.LOBBY_PLAYER_LEAVE, CommonCreator.createPBPair(info.getPostion().getValue(), 0));
        	info.setLeave(true);
        }
    }

    private RoomNiuNiuDeskInfo getDeskInfo(PlayerInfo playerInfo) {
        return deskInfoMap.get(playerInfo);
    }


    public XianPosition getFreePos() {
        return XianPosition.getFree(posInfoMap);
    }

    // 是否可以准备好了
    private boolean isReaday() {
        // 遍历玩家,若玩家个数大于2个,则可以开始
        return deskInfoMap.size() >= 2;
    }

    // 检查并开始游戏
    private void checkAndBegin() {
        if (isReaday()) {
            setState(ZhuangNiuState.PRE_REDAY);
        } else {
            setState(ZhuangNiuState.WATT);
        }
    }


    private void onUpdate() {
    	if(isDestroyed.get()){
    		logger.info("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
    	stateRemainTime--;
        if (stateRemainTime > 0) {
            return;
        }
        stateRemainTime = 0;
        switch (state) {
            case WATT:
                checkAndBegin();
                break;
            case PRE_REDAY:
                onReady();
                break;
            case DEAL_CARD1:
                onDealCard1();
                break;
            case GRAB_ZHUANG:
                onGrabZhuang();
                break;
            case BET:
                onBet();
                break;
            case DEAL_CARD2:
                oneTimeDealCard2();
                break;
            case PLAYER_CAL:        // 玩家算牌
                onTimePlayerCal();
                break;
            case CALCULATE:
                onTimeCalculate();
                break;
            default:
                break;
        }
    }

    private void onTimeCalculate() {
       if(LogicApp.getInst().isStop()){
    	    DeskMgr.getInst().removeDesk(this);
    		return;
       }
       // 时间到了重新开
       beginDeskFuture();
       //游戏中途退出
       List<RoomNiuNiuDeskInfo> leavePlayer = new ArrayList<RoomNiuNiuDeskInfo>();
	   deskInfoMap.values().forEach(
	            e -> {
	            	//玩家离开
	            	if(!e.isLeave()){
	            		 if(e.getNoGrabAndBetRound() >= 2){//两局都没操作直接踢出
	            			 e.getPlayerInfo().write(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(e.getPlayerInfo().getPlayerId(), 1));
	            			 e.setLeave(true);
	            			 leavePlayer.add(e);
	            		 }else if (!isPlayerMoneyEnough(e.getPlayerInfo())) {
		        			 e.getPlayerInfo().write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
		        			 e.setLeave(true);
		        			 leavePlayer.add(e);
		        	     }else{
		        	    	 e.getPlayerInfo().write(ResponseCode.GRADNIU_STATE,CommonCreator.createPBInt32(ZhuangNiuState.WATT.getSecond()));
		        	     }
	            	}else{
	            		leavePlayer.add(e);
	            	}
	            }
	    );
	   if(isAllPlayerLeave()){
		   DeskMgr.getInst().removeDesk(this);
		   return;
	   }
        //玩家可以离开了
       leavePlayer.forEach( 
    		e -> {
       			msgHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(e.getPostion().getValue()));
    			leavePlayer(e);
    		}
       );
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
        if (info.getCoin() < baseScore * enterTimes) {
            return false;
        }
        return true;
    }

    private void onTimePlayerCal() {
        // 时间到了设置结算状态
        setState(ZhuangNiuState.CALCULATE);

        // 开始结算
        calculate();
        
        if(!isPersonal()){
        	//玩家这局是否操作
            deskInfoMap.values().forEach(
                    e -> {
                    	//玩家是否没抢庄和
                    	if(e.getPlayerGrabMulti() == 0 && e.getPlayerBetMulti() == 0 && !e.isRevcPlayerCal()){
                    		e.setNoGrabAndBetRound(e.getNoGrabAndBetRound() + 1);
                    	}
                    }
            );
        }
        
        curRound++;
    }

    //  结算金额=房间底注*抢庄倍数*玩家下注倍数*胜方牌型倍数
    private void calculate() {
        Oxc.PBOXCGameEnd.Builder builder = Oxc.PBOXCGameEnd.newBuilder();

        Oxc.PBOXCItemResult.Builder zhuangRltBuilder = Oxc.PBOXCItemResult.newBuilder();
        zhuangRltBuilder.setPos(zhuangDesk.getPositionValue());
        zhuangDesk.getHandCards().forEach(
                e -> zhuangRltBuilder.addCardsMain(e.getKey())
        );

        zhuangRltBuilder.addAllCardsAdd(zhuangDesk.getTowCards());
        zhuangRltBuilder.setTyped(zhuangDesk.getResult().getLeft().getValue());
        
        // 参与玩家总数
        int playerCount = 1;//庄家一个

        // 抢庄的倍数
        int grabTimes = config.getGrabZhuang().get(zhuangDesk.getGrabZhuangPos() - 1);

        // 闲家实际输钱
        int xianLostMoney = 0;

        // 闲家应该赢钱总数
        int xianWinTemp = 0;
        
        // 闲家赢的钱存起来
        Map<RoomNiuNiuDeskInfo, Integer> xianWinMap = new HashMap<RoomNiuNiuDeskInfo, Integer>();
        
        // 闲家输的钱存起来
        Map<RoomNiuNiuDeskInfo, Integer> xianLoseMap = new HashMap<RoomNiuNiuDeskInfo, Integer>();
        
        for (RoomNiuNiuDeskInfo deskInfo : deskInfoMap.values()) {
            if (!deskInfo.isWatch() && !deskInfo.equals(zhuangDesk)) {
            	playerCount++;
                // 加注倍数
                int betTimes = getBetVal(deskInfo);

                // 参与的玩家
                if (PokerUtil.isOneGtTwo(deskInfo.getResult(), zhuangDesk.getResult())) {
                    // 闲家赢
                    int times = getNiuTimes(deskInfo.getResult().getLeft());
                    int calMoney = (int) (baseScore * grabTimes * betTimes * times);
                    xianWinTemp += calMoney;

                    xianWinMap.put(deskInfo, calMoney);
                } else {
                    // 庄家赢
                    int times = getNiuTimes(zhuangDesk.getResult().getLeft());
                    int calMoney = (int) (baseScore * grabTimes * betTimes * times);
                    
                    int temp = 0;
                    // 判断闲家钱够不够
                    if (deskInfo.getPlayerInfo().getCoin() >= calMoney) {
                        temp = calMoney;
                    } else {
                        temp = deskInfo.getPlayerInfo().getCoin();
                    }
                    xianLostMoney += temp;
                    xianLoseMap.put(deskInfo, calMoney);
                }
            }
        }
        
        int zhuangWinMoney = xianLostMoney; 
        boolean zhuangMoneyEnough = false;
        if(zhuangDesk.getPlayerInfo().getCoin() < xianLostMoney){
        	zhuangWinMoney = zhuangDesk.getPlayerInfo().getCoin();
        	zhuangMoneyEnough = true;
        }
        int xianLostMoneyTotal = xianLostMoney;
        xianLostMoney = 0;
        
        //庄家不够钱赢
        for (Map.Entry<RoomNiuNiuDeskInfo, Integer> entry : xianLoseMap.entrySet()) {
        	RoomNiuNiuDeskInfo deskInfo = entry.getKey();
    		Integer calMoney = entry.getValue();
    		
    		 int temp = 0;
             // 判断闲家钱够不够
             if (deskInfo.getPlayerInfo().getCoin() >= calMoney) {
                 temp = calMoney;
             } else {
                 temp = deskInfo.getPlayerInfo().getCoin();
             }
             
             if(zhuangMoneyEnough){
            	 float rate = calMoney * 1f / xianLostMoneyTotal;
            	 int lose =  (int) (zhuangWinMoney * rate);
            	 if(lose < temp){
            		 temp = lose;
            	 }
    	     }
             
             xianLostMoney += temp;
             
             //后台数据
             long preCoin = deskInfo.getPlayerInfo().getCoin();
             
             // 闲家输的钱
             deskInfo.getPlayerInfo().updateCoin(temp, false, getLoseMoneySubAction(), getGameType().getValue(), this.gameId);
             logger.info("闲家 {} 输钱 {} ", deskInfo.getPlayerInfo().getPlayerId(), temp);
            
             TaxRecordUtil.sendGamePlayerStatus(deskInfo.getPlayerInfo(), -temp);
             scoreHis.put(deskInfo.getPostion(), -temp);
             record.addWinResult(deskInfo.getPositionValue(),-temp);
             detail.addPlayerRecord(deskInfo, 0, preCoin, 0, -temp);
             

             // 输钱的闲家的result
             Oxc.PBOXCItemResult.Builder lostXian = Oxc.PBOXCItemResult.newBuilder();
             lostXian.setPos(deskInfo.getPositionValue());
             deskInfo.getHandCards().forEach(
                     e -> lostXian.addCardsMain(e.getKey())
             );

             lostXian.addAllCardsAdd(deskInfo.getTowCards());
             lostXian.setTyped(deskInfo.getResult().getLeft().getValue());
             lostXian.setWinCoin(-temp);
             builder.addResult(lostXian.build());
        }
        
        int xianWinTotal = xianWinTemp;
        xianWinTemp = 0;
        for (Entry<RoomNiuNiuDeskInfo, Integer> entry : xianWinMap.entrySet()) {
    		RoomNiuNiuDeskInfo e = entry.getKey();
    		Integer f = entry.getValue();
    		
    		//闲家钱够不够
    		if(e.getPlayerInfo().getCoin() < f){
    			f = e.getPlayerInfo().getCoin();
    		}
    		xianWinTemp += f;
        }

        // 庄家现在的钱够不够输
        if ((zhuangDesk.getPlayerInfo().getCoin() + xianLostMoney * (getGainRate() / 100f)) >= xianWinTemp) {
            // 若够输,给闲家加钱，且闲家要扣税
        	for (Entry<RoomNiuNiuDeskInfo, Integer> entry : xianWinMap.entrySet()) {
        		RoomNiuNiuDeskInfo e = entry.getKey();
        		Integer f = entry.getValue();
        		
           	    //后台数据
                long preCoin = e.getPlayerInfo().getCoin();
                
                if(preCoin < f){
                	//下注的时候没钱下最小注
                	logger.info("闲家 {}身上{}钱不够 赢{}的钱,",e.getPlayerInfo().getPlayerId(),preCoin,f);
                	f = (int) preCoin;
                }
                int c = (int) (f * (getGainRate() / 100f));
                
                e.getPlayerInfo().updateCoin(c, true, getWinMoneySubAction(), getGameType().getValue(), this.gameId);
                logger.info("{} 闲家 {} 赢钱 {} ，扣税后为{}",getGameName(), e.getPlayerInfo().getPlayerId(), f, c);
                
                int costTax = f - c;
                totalTax += costTax;
                
                TaxRecordUtil.sendGamePlayerStatus(e.getPlayerInfo(), c);
                scoreHis.put(e.getPostion(), f);//赢了战绩显示时不扣税
                record.addWinResult(e.getPositionValue(), c);
                detail.addPlayerRecord(e, costTax, preCoin, 1, c);
                
                // 赢钱的的闲家的result
                Oxc.PBOXCItemResult.Builder winXian = Oxc.PBOXCItemResult.newBuilder();
                winXian.setPos(e.getPositionValue());
                e.getHandCards().forEach(
                        a -> winXian.addCardsMain(a.getKey())
                );

                winXian.addAllCardsAdd(e.getTowCards());
                winXian.setTyped(e.getResult().getLeft().getValue());
                winXian.setWinCoin(c);
                builder.addResult(winXian.build());
			}
        	
            // 给庄家扣钱，若庄家还是赢钱的，赢的部分要扣税
            int change = xianLostMoney - xianWinTemp;
            if (change >= 0) {
            	 //后台数据
                long preCoin = zhuangDesk.getPlayerInfo().getCoin();
                // 庄赢钱了，要扣税
                int f = (int) (change * (getGainRate() / 100f));
                zhuangDesk.getPlayerInfo().updateCoin(f, true, getWinMoneySubAction(), getGameType().getValue(), this.gameId);
                logger.info("{}  庄家 {} 赢钱 {} ，扣税后为{}",getGameName(), zhuangDesk.getPlayerInfo().getPlayerId(), change, f);
                zhuangRltBuilder.setWinCoin(f);
                
                int costTax = change - f;
                totalTax += costTax;
                
                TaxRecordUtil.sendGamePlayerStatus(zhuangDesk.getPlayerInfo(), f);
                scoreHis.put(zhuangDesk.getPostion(), change);//赢了战绩显示时不扣税
                record.addWinResult(zhuangDesk.getPositionValue(), f);
                detail.addPlayerRecord(zhuangDesk, costTax, preCoin, 1, f);
            } else {
            	 //后台数据
                long preCoin = zhuangDesk.getPlayerInfo().getCoin();
                // 庄家输了
                zhuangDesk.getPlayerInfo().updateCoin(-change, false, getLoseMoneySubAction(), getGameType().getValue(), this.gameId);
                logger.info("{}  庄家 {} 输钱 {} ，不扣税", getGameName(),zhuangDesk.getPlayerInfo().getPlayerId(), change);
                zhuangRltBuilder.setWinCoin(change);
               
                TaxRecordUtil.sendGamePlayerStatus(zhuangDesk.getPlayerInfo(), change);
                scoreHis.put(zhuangDesk.getPostion(), change);
                record.addWinResult(zhuangDesk.getPositionValue(), change);
                detail.addPlayerRecord(zhuangDesk, 0, preCoin, 0, change);
            }
        } else {
            int zhuangCoin = zhuangDesk.getPlayerInfo().getCoin();
            //后台数据
            long preCoin = zhuangCoin;
            
            // 给庄家扣钱
            zhuangDesk.getPlayerInfo().updateCoin(zhuangCoin, false, getLoseMoneySubAction(), getGameType().getValue(), this.gameId);
            logger.info("{} 庄家 {} 钱不够输,全部输完 {}",getGameName(), zhuangDesk.getPlayerInfo().getPlayerId(), zhuangCoin);

            zhuangRltBuilder.setWinCoin(-zhuangCoin);
            
            int winRateAfter = (int) (xianLostMoney * (getGainRate() / 100f));
            zhuangCoin += winRateAfter;
            
            int costTax = (int) (xianLostMoney  - winRateAfter);
            totalTax += costTax;
            
            TaxRecordUtil.sendGamePlayerStatus(zhuangDesk.getPlayerInfo(), -zhuangCoin);
            scoreHis.put(zhuangDesk.getPostion(), -zhuangCoin);
            record.addWinResult(zhuangDesk.getPositionValue(), -zhuangCoin);
            detail.addPlayerRecord(zhuangDesk, costTax, preCoin, 0, -zhuangCoin);

            // 若不够输,按比例分给闲家，且闲家要扣税
//            int i = 0;
//            int tmp = 0;
//            int size = xianWinMap.size();
            for (Map.Entry<RoomNiuNiuDeskInfo, Integer> entry : xianWinMap.entrySet()) {
//                i++;
                int change = 0;
//                if (size == i) {
//                    change = zhuangCoin - tmp;
//                } else {
                     float rate = entry.getValue() * 1f / xianWinTotal;
                     change =  (int) (zhuangCoin * rate);
//                }
//                tmp += change;
                 
                if(entry.getKey().getPlayerInfo().getCoin() < change){
                	//下注的时候没钱下最小注
                	logger.info("闲家 {}身上{}钱不够 赢{}的钱,",entry.getKey().getPlayerInfo().getPlayerId(),preCoin,change);
                	change = entry.getKey().getPlayerInfo().getCoin();
            	}

                int f = (int) (change * (getGainRate() / 100f));
                preCoin = entry.getKey().getPlayerInfo().getCoin();
                
                entry.getKey().getPlayerInfo().updateCoin(f, true,getWinMoneySubAction(), getGameType().getValue(), this.gameId);
                logger.info("{} 闲家 {} 赢钱 {} ，扣税后为{}",getGameName(), entry.getKey().getPlayerInfo().getPlayerId(), change, f);
                
                costTax = change - f;
                totalTax += costTax;
                
                TaxRecordUtil.sendGamePlayerStatus(entry.getKey().getPlayerInfo(), f);
                scoreHis.put(entry.getKey().getPostion(), change);//赢了战绩显示时不扣税
                record.addWinResult(entry.getKey().getPositionValue(), f);
                detail.addPlayerRecord(entry.getKey(), costTax, preCoin, 1, f);

                // 赢钱的的闲家的result
                Oxc.PBOXCItemResult.Builder winXian = Oxc.PBOXCItemResult.newBuilder();
                winXian.setPos(entry.getKey().getPositionValue());
                entry.getKey().getHandCards().forEach(
                        a -> winXian.addCardsMain(a.getKey())
                );

                winXian.addAllCardsAdd(entry.getKey().getTowCards());
                winXian.setTyped(entry.getKey().getResult().getLeft().getValue());
                winXian.setWinCoin(f);
                builder.addResult(winXian.build());
            }
        }

        builder.addResult(zhuangRltBuilder.build());
        builder.setCountDown(ZhuangNiuState.CALCULATE.getSecond());
        msgHelper.notifyMessage(ResponseCode.GRADNIU_RESULT, builder.build());
        
        scoreList.add(new Pair<>(startTime, scoreHis));
        scoreHis = new HashMap<>();
        
        TaxRecordUtil.recordGameTaxInfo(startTime, playerCount, gameId, getGameType(), getConfId()
				, zhuangDesk.getPlayerInfo().getPlayerId() , 0, totalTax, detail, this);
        
        Gson gson = JsonUtil.getGson();
        int recordId = TaxRecordUtil.recordGamReply(gson.toJson(record), startTime, deskInfoMap.keySet(), getGameType(), String.valueOf(gameId));
        recordIdList.add(recordId);
    }

    //实际获得赢钱的百分比
    protected int getGainRate() {
        if (isPersonal()) {
        	int gameTypeId = GameType.GRAB_NIU.getValue();
        	if(model == 1){//经典玩法
        		gameTypeId =  GameType.CLASS_NIU.getValue();
        	}
        	PersonalConfRoom personalConfRoom = PersonalConfRoomProvider.getInst().getPersonalConfRoomById(gameTypeId);
        	if(personalConfRoom != null){
        		return 100 - personalConfRoom.getTax_rate();
        	}
            return 100 - 2;
        }
        CoupleRoom room = CoupleRoomInfoProvider.getInst().getRoomConf(getConfId());
        if(room == null){
        	 return 100 - 5;
        }
        return 100 - room.getTax_rate();
    }

    private void oneTimeDealCard2() {
        // 设置算牌状态
        setState(ZhuangNiuState.PLAYER_CAL);
    }
    
    /** 获取牛倍数 */
	private int getNiuTimes(NiuResult niuResult){
		return niuMap.get(niuResult.getValue());
	}

    // 下注
    private void onBet() {
        // 没有加注的设置加注

        // 若时间到了,自动设置没有加注的下标为1
        if (!checkAllPlayerBet()) {
            deskInfoMap.values().forEach(e -> {
                if (!e.isWatch() && zhuangDesk != e &&  e.getBetPos() == 0) {
                    e.setBetPos(1);
                    msgHelper.notifyMessage(ResponseCode.GRADNIU_NOTIFY_BET, CommonCreator.createPBPair(e.getPositionValue(), e.getBetPos()));
                    logger.info("{}  玩家{} 自动 加注下标{}",getGameName(), e.getPlayerInfo().getPlayerId(), e.getBetPos());
                }
            });
        }

        // 若所有玩家都下注,进入到发牌阶段2
        if (checkAllPlayerBet()) {
            // 发牌2
           onBetOver();
        }
    }
    
    private int getBetVal(RoomNiuNiuDeskInfo info){
//    	if(info.getBetPos() > 0){
    		return config.getAddBet().get(info.getBetPos() - 1);
//    	}else{
//    		return config.getAddBet2().get(-info.getBetPos() - 1);
//    	}
    }

    /** 下注 */
    public void bet(PlayerInfo player, int pos) {  // 下标，从1开始
    	if(player == null){
    		return;
    	}
    	if(state != ZhuangNiuState.BET){
    		logger.info("{} 玩家 {}非下注{}状态不能下注{}",getGameName(),player.getPlayerId(),state,getDeskId());
    		return;
    	}
        RoomNiuNiuDeskInfo deskInfo = deskInfoMap.get(player);
        if (this.zhuangDesk == deskInfo) {
            logger.info("{} 庄 {} 不能加注",getGameName(), player.getPlayerId());
            return;
        }

//        if(pos > 0){
        	 if (pos == 0 || config.getAddBet().size() < pos) {
                 logger.info("{} 玩家 {} 加注下标不存在{}",getGameName(), player.getPlayerId(),pos);
                 return;
             }
//        }else{
//        	if (pos == 0 || config.getAddBet2().size() < -pos) {
//                logger.info("{} 玩家 {} 加注下标不存在{}",getGameName(), player.getPlayerId(),-pos);
//                return;
//            }
//        }

        if (deskInfo.getBetPos() != 0) {
            // 已经下注过
            logger.info("{} 玩家 {} 已经加注过",getGameName(), player.getPlayerId());
            return;
        }

        int betMutli = 0;
//        if(pos > 0){
        	betMutli = config.getAddBet().get(pos - 1);
//        }else{
//        	betMutli = config.getAddBet2().get(-pos - 1);
//        }
        
        //自身金币数量>底注*庄家坐庄倍数*4*我想要加倍的倍数，则对应的加倍按钮按钮可用，否则不可用
        int betMinNeedCoin = baseScore * config.getGrabZhuang().get(zhuangDesk.getGrabZhuangPos() - 1) * getNiuTimes(NiuResult.FIVE_SMALL_NIU) * betMutli;
        if(player.getCoin() < betMinNeedCoin){
    		if(betMutli != config.getAddBet().get(0)){//当钱不够的时候 还是可以下注5 但是赢钱的时候，只能赢身上所带的钱
    			return;
        	}
    		logger.info("玩家{}下注{}倍,但金币不够,需要{},当前{} ", player.getPlayerId(),betMutli,betMinNeedCoin,player.getCoin());
        }
        
        deskInfo.setBetPos(pos);
        deskInfo.setPlayerBetMulti(betMutli);
        logger.info("{} 玩家{}加注下标{}->{}倍",getGameName() ,player.getPlayerId(), pos,betMutli);

        msgHelper.notifyMessage(ResponseCode.GRADNIU_NOTIFY_BET, CommonCreator.createPBPair(deskInfo.getPositionValue(), pos));

        // 若所有玩家都下注,进入到发牌阶段2
        if (checkAllPlayerBet()) {
            onBetOver();
        }
    }
    
    /** 玩家客户端算牌 */
    public void playerCal(PlayerInfo player) {
    	if(player == null){
    		return;
    	}
		if(state != ZhuangNiuState.PLAYER_CAL){
    		logger.info("{} 玩家 {}非算牌阶段{}状态不能算牌{}",getGameName(),player.getPlayerId(),state,getDeskId());
    		return;
    	}
    	
    	RoomNiuNiuDeskInfo info = deskInfoMap.get(player);
    	if(info.isWatch()){
    		logger.info("观察者不能算牌 {},{},{}",getGameName(),player.getPlayerId(),state,getDeskId());
    		return;
    	}
    	if(info.isRevcPlayerCal()){
    		return;
    	}
    	info.setRevcPlayerCal(true);
    	msgHelper.notifyPlayerCal(info);
    	
    	boolean isAllPlayerCal = true;
		for (RoomNiuNiuDeskInfo deskInfo : deskInfoMap.values()) {
			if (!deskInfo.isWatch() && !deskInfo.isLeave() && !deskInfo.isRevcPlayerCal()) {
				isAllPlayerCal = false;
			}
		}
		
		//所有人都发送了算牌 是否有牛 结算
		if(isAllPlayerCal){
			onTimePlayerCal();
			if(isPersonal()){
				setBegain();
			}
		}
    }
    
    /** 日志 */
    private MoneySubAction getWinMoneySubAction(){
    	if(model == 1){
    		if (isPersonal()){
    			return MoneySubAction.CLASS_NIU_WIN_PERSONAL;
    		} else {
    			return MoneySubAction.CLASS_NIU_WIN;
    		}
    	}else{
    		if (isPersonal()){
    			return MoneySubAction.GRAD_NIU_WIN_PERSONAL;
    		} else {
    			return MoneySubAction.GRAD_NIU_WIN;
    		}
    	}
    }
    
    /** 日志 */
    private MoneySubAction getLoseMoneySubAction(){
		if (model == 1) {
			if (isPersonal()){
				return MoneySubAction.CLASS_NIU_LOST_PERSONAL;
			} else {
				return MoneySubAction.CLASS_NIU_LOST;
			}
		} else {
			if (isPersonal()){
				return MoneySubAction.GRAD_NIU_LOST_PERSONAL;
			} else {
				return MoneySubAction.GRAD_NIU_LOST;
			}
		}
    }
    
    /** 当下注结束 */
    private void onBetOver(){
    	setState(ZhuangNiuState.PLAYER_CAL);
		deskInfoMap.forEach((e, f) -> {
			f.calNiuResult();
			
			record.addNiuResult(f.getPositionValue(),f.getResult().getLeft(),f.get5HandCardKeyList(),f.getTowCards());
		});
		if(model == 1){
			msgHelper.notifyDealCard2_5();
		}else{
			msgHelper.notifyDealCard2_1();
		}
		
		deskInfoMap.values().forEach(e->{ 
        	if(!e.isWatch() && zhuangDesk != e){
        		record.addBetMulti(e.getPositionValue(), getBetVal(e),e.getPlayerBetMulti());
        	}
		});
    }

    // 抢庄
    private void onGrabZhuang() {
        // 若时间到了,自动设置没有抢庄的下标为1
        if (!checkAllPlayerGrad()) {
            deskInfoMap.values().forEach(e -> {
                if (!e.isWatch() && e.getGrabZhuangPos() == 0) {
                    e.setGrabZhuangPos(multi);
                }
            });
        }

        // 计算谁是庄
        caluZhuang(true);

        setState(ZhuangNiuState.BET);
    }

    // 抢庄
    public void grab(PlayerInfo player, int pos) {
    	if(player == null){
    		return;
    	}
    	if(state != ZhuangNiuState.GRAB_ZHUANG){
    		logger.info("{}非抢庄{}状态不能抢庄{}",player.getPlayerId(),state,getDeskId());
    		return;
    	}
        RoomNiuNiuDeskInfo deskInfo = deskInfoMap.get(player);

        if (pos == 0 || config.getGrabZhuang().size() < pos) {
            logger.info("玩家 {} 抢庄加倍下标不存在", player.getPlayerId());
            return;
        }

        if (deskInfo.getGrabZhuangPos() > 0) {
            // 已经抢庄过
            logger.info("玩家 {} 抢庄过", player.getPlayerId());
            return;
        }
        
        int grabMulti = config.getGrabZhuang().get(pos - 1);
        
        //当玩家金币数量<底注*25*抢庄倍数，则对应的抢庄倍数按钮不可用
        int grabMinNeedCoin = baseScore * config.getAddBet().get(config.getAddBet().size() - 1) * grabMulti;
        if(player.getCoin() < grabMinNeedCoin){
        	logger.info("玩家{}抢庄{}倍,但金币不够,需要{},当前{} ", player.getPlayerId(),grabMulti,grabMinNeedCoin,player.getCoin());
            return;
        }

        deskInfo.setGrabZhuangPos(pos);
        deskInfo.setPlayerGrabMulti(grabMulti);

        logger.info("玩家{} 抢庄加倍 {}->{}倍", player.getPlayerId(), pos,grabMulti);

        // 判断若所有的玩家都抢庄了,进入到下注阶段
        if (checkAllPlayerGrad()) {
            // 计算谁是庄
            caluZhuang(true);

            setState(ZhuangNiuState.BET);
        }
    }

    // 计算庄
    protected void caluZhuang(boolean noZhuang) {
    	if(noZhuang){
    		List<RoomNiuNiuDeskInfo> listZhuang = deskInfoMap.values().stream().filter(e -> !e.isWatch())
    	            .sorted((i1, i2) -> (i1.getGrabZhuangPos() > i2.getGrabZhuangPos() ? -1 : 1))
    	            .collect(Collectors.toList());
    		List<RoomNiuNiuDeskInfo> randListZhuang = new ArrayList<>();
    		for (RoomNiuNiuDeskInfo info : listZhuang) {
				if(randListZhuang.size() == 0 || info.getGrabZhuangPos() == randListZhuang.get(0).getGrabZhuangPos()){
					randListZhuang.add(info);
				}else{
					break;
				}
			}
    	    zhuangDesk = randListZhuang.get(Randomizer.nextInt(randListZhuang.size()));
    	}
        
        logger.info("庄的位置为{} ,id 为 {}", zhuangDesk.getPositionValue(), zhuangDesk.getPlayerInfo().getPlayerId());

        //回放数据
        deskInfoMap.values().forEach(e->{ 
        	if(!e.isWatch()){
        		record.addZhuangMulti(e.getPositionValue(), config.getGrabZhuang().get(e.getGrabZhuangPos() - 1),e.getPlayerGrabMulti());
        	}
        });
        record.setZhuangPos(zhuangDesk.getPositionValue());
        
        // 通知谁是庄
        msgHelper.notifyZhuang();
    }


    // 检查是否所有玩家都抢过庄了
    private boolean checkAllPlayerGrad() {
        return !posInfoMap.values().stream().anyMatch(e -> !e.isWatch() && e.getGrabZhuangPos() == 0);
    }

    // 检查是否所有玩家都下过注了
    private boolean checkAllPlayerBet() {
        return !posInfoMap.values().stream().anyMatch(e -> zhuangDesk != e && !e.isWatch() && e.getBetPos() == 0);
    }


    // 时间到了设置到发牌状态
    private void onReady() {
        // 判断人数够的话，且加入时间够的玩家，开牌。
        int now = MiscUtil.getCurrentSeconds();
        List<RoomNiuNiuDeskInfo> list = new ArrayList<RoomNiuNiuDeskInfo>();
        //游戏中途退出
        List<RoomNiuNiuDeskInfo> leavePlayer = new ArrayList<RoomNiuNiuDeskInfo>();
        deskInfoMap.values().forEach(
                e -> {
            		//未离开的人需要知道能否离开
            		if ((now - e.getEnterTime()) > minReadTime) {
            			if(e.isLastIsWatch()){
            				e.setWatch(false);
                			list.add(e);
            			}else{
            				if(e.isReady()){
            					e.setWatch(false);
                    			list.add(e);
            				}else{
            					e.getPlayerInfo().write(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(e.getPlayerInfo().getPlayerId(), 0));
            					e.setLeave(true);
            					leavePlayer.add(e);
            				}
            			}
            		} else {
            			e.setWatch(true);
            		}
                }
       );
        
       if(isAllPlayerLeave()){
 		   DeskMgr.getInst().removeDesk(this);
 		   return;
 	   }
        //玩家可以离开了
        leavePlayer.forEach(
     		e -> {
     			msgHelper.notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(e.getPostion().getValue()));
     			leavePlayer(e);
     		}
        );

        if (list.size() >= 2) {
        	deskInfoMap.values().forEach(e -> {
        		e.setLastIsWatch(e.isWatch());
            });
        	startGame(now,list);
        } else {
            list.forEach(e -> {
                e.setWatch(false);
            });
            list.clear();
            // 通知重新等待
            setState(ZhuangNiuState.WATT);
        }
    }
    
    /** 获取参与游戏人数 */
    private int getJoinPlayerCount(){
    	int count = 0;
    	for (RoomNiuNiuDeskInfo info : deskInfoMap.values()) {
    		if(!info.isWatch()){
    			count++;
    		}
		}
    	return count;
    }
    
    /** 游戏开始 */
    private void startGame(int now,List<RoomNiuNiuDeskInfo> list){
		this.gameId = geneGameNo();
		// 给这些玩家发牌
		startTime = now;
		int roomId = getConfId();
		if(isPersonal()){
			roomId = -getGameType().getValue();
		}
		ConfMatchCard confMatchNiuCard = CoupleRoomInfoProvider.getInst().getConfMatchCard(roomId);
		Queue<PokerCard> mixedInitCard = PokerUtil.mixAllCard(GameType.NIUNIU);
		if(confMatchNiuCard != null && confMatchNiuCard.isOpen()){
			logger.error("庄牛{}开启配牌",getConfId());
			if(model == 1){
				mixedInitCard = PokerMatchCardUtil.matchNiuCard(gameId, mixedInitCard, confMatchNiuCard, getJoinPlayerCount());
			}else{
				mixedInitCard = PokerMatchCardUtil.matchNiuCard2(gameId, mixedInitCard, confMatchNiuCard, getJoinPlayerCount());
			}
		}
		Queue<PokerCard> mixedCard = mixedInitCard ;
		
		record.initPlayer(list);
		record.setCurRound(curRound);
		
		if(rule == NiuNiuZhuangType.GrabZhuang){
			setState(ZhuangNiuState.GRAB_ZHUANG);
			deskInfoMap.values().stream().sorted((i1, i2) -> ( i2.getPlayerInfo().getPlayerId()  - i1.getPlayerInfo().getPlayerId())).forEach(o ->{
				RoomNiuNiuDeskInfo f = o;
				PlayerInfo e = o.getPlayerInfo();
				if (!f.isWatch()) {
					f.getHandCards().clear();
					for (int i = 0; i < 5; i++) {
						f.addHandCards(mixedCard.poll());
					}
					logger.info("玩家{} 获得手牌 {}", e.getPlayerId(), JsonUtil.getJsonString(f.getHandCards()));
				}
				if(model == 1){
					msgHelper.startGameClassNiu(list, f);
				}else{
					msgHelper.startGameGrabNiu(list, f);
				}
			});
		}else if(rule == NiuNiuZhuangType.TrunZhuang){
			deskInfoMap.values().forEach(e -> {
                if (!e.isWatch() && e.getGrabZhuangPos() == 0) {
                    e.setGrabZhuangPos(1);
                }
            });
			//轮庄 先是房主 然后轮换
			if(zhuangDesk != null){
				//按照顺序轮换
				Collections.sort(list,new Comparator<RoomNiuNiuDeskInfo>() {

					@Override
					public int compare(RoomNiuNiuDeskInfo o1, RoomNiuNiuDeskInfo o2) {
						return o2.getPositionValue() - o1.getPositionValue();
					}
				});
				
				int curIndex = 0;
				for (RoomNiuNiuDeskInfo info : list) {
					if(info == zhuangDesk){
						break;
					}
					curIndex++;
				}
				curIndex++;
				if(curIndex >= list.size()){
					zhuangDesk = list.get(0);
				}else{
					zhuangDesk = list.get(curIndex);
				}
			}else{
				for (RoomNiuNiuDeskInfo info : list) {
					if(info.getPlayerInfo().getPlayerId() == createId){
						zhuangDesk = info;
						break;
					}
				}
				if(zhuangDesk == null){
					logger.info("轮庄，房主info未找到,{},{}",createId,getDeskId());
				}
			}
			
			zhuangDesk.setGrabZhuangPos(multi);
			
			setState(ZhuangNiuState.BET);
			stateRemainTime = 0;
			
			deskInfoMap.values().stream().sorted((i1, i2) -> ( i2.getPlayerInfo().getPlayerId()  - i1.getPlayerInfo().getPlayerId())).forEach(o ->{
				RoomNiuNiuDeskInfo f = o;
				PlayerInfo e = o.getPlayerInfo();
				if (!f.isWatch()) {
					f.getHandCards().clear();
					for (int i = 0; i < 5; i++) {
						f.addHandCards(mixedCard.poll());
					}
					logger.info("玩家{} 获得手牌 {}", e.getPlayerId(), JsonUtil.getJsonString(f.getHandCards()));
				}
				if(model == 1){
					msgHelper.startGameClassNiu(list, f);
				}else{
					msgHelper.startGameGrabNiu(list, f);
				}
			});
			caluZhuang(false);
		} else if(rule == NiuNiuZhuangType.OccupiedZhuang){
			deskInfoMap.values().forEach(e -> {
                if (!e.isWatch() && e.getGrabZhuangPos() == 0) {
                    e.setGrabZhuangPos(1);
                }
            });
			
			for (RoomNiuNiuDeskInfo info : list) {
				if(info.getPlayerInfo().getPlayerId() == createId){
					zhuangDesk = info;
					break;
				}
			}
			if(zhuangDesk == null){
				logger.info("轮庄，房主info未找到,{},{}",createId,getDeskId());
			}
			
			zhuangDesk.setGrabZhuangPos(multi);
			
			setState(ZhuangNiuState.BET);
			stateRemainTime = 0;
			
			deskInfoMap.values().stream().sorted((i1, i2) -> ( i2.getPlayerInfo().getPlayerId()  - i1.getPlayerInfo().getPlayerId())).forEach(o ->{
				RoomNiuNiuDeskInfo f = o;
				PlayerInfo e = o.getPlayerInfo();
				if (!f.isWatch()) {
					f.getHandCards().clear();
					for (int i = 0; i < 5; i++) {
						f.addHandCards(mixedCard.poll());
					}
					logger.info("玩家{} 获得手牌 {}", e.getPlayerId(), JsonUtil.getJsonString(f.getHandCards()));
				}
				if(model == 1){
					msgHelper.startGameClassNiu(list, f);
				}else{
					msgHelper.startGameGrabNiu(list, f);
				}
			});
			caluZhuang(false);
		}
    }

    // 发牌阶段1
    private void onDealCard1() {
        // 设置抢庄状态
        setState(ZhuangNiuState.GRAB_ZHUANG);
    }

    /** 初始化玩家桌面信息  */
    private void initRoomNiuNiuDeskInfo(List<PlayerInfo> playerList) {
        int enterTime = MiscUtil.getCurrentSeconds();
        for (PlayerInfo player : playerList) {
            RoomNiuNiuDeskInfo info = new RoomNiuNiuDeskInfo();
            if(isPersonal()){
            	info.setPostion(XianPosition.getByValue(player.getPosition()));
            }else{
            	info.setPostion(XianPosition.getFree(posInfoMap));
            }
            player.setPosition(info.getPositionValue());
            info.setPlayerInfo(player);
            info.setEnterTime(enterTime);
            deskInfoMap.put(player, info);
            posInfoMap.put(info.getPostion(), info);

            DeskMgr.getInst().registerDesk(player.getPlayerId(), this);
        }
    }

    protected void setState(ZhuangNiuState state) {
        this.state = state;
        this.stateRemainTime = state.getSecond();

        logger.info("{} 桌子 {} 设置状态 state : {}",getGameName(), getDeskId(), state);
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return new ArrayList<PlayerInfo>(deskInfoMap.keySet());
    }

    @Override
    public void playerLogout(PlayerInfo player) {
    	playerLeave(player);
    }

    @Override
    public void playerReady(PlayerInfo player) {
    	if (state != ZhuangNiuState.WATT) {
    		return;
    	}
    	
		RoomNiuNiuDeskInfo info = deskInfoMap.get(player);
		if (info == null) {
			return;
		}
    	
		if (!isPlayerMoneyEnough(player)) {
			player.write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
			return;
		}
		
		if(info.isReady()){
			return;
		}
		
		info.setReady(true);
    	logger.info("{} {} | {} |玩家 {} 准备",getGameName(), this.deskId, this.gameId, player.getPlayerId());
    	
		if(isPersonal()){
			msgHelper.notifyMessage(ResponseCode.COUPLE_GAME_READY, CommonCreator.createPBInt32(info.getPositionValue()));
			List<RoomNiuNiuDeskInfo> list = new ArrayList<RoomNiuNiuDeskInfo>();
			 
			boolean allReady = true;
			 for (RoomNiuNiuDeskInfo deskInfo : deskInfoMap.values()) {
				list.add(deskInfo);
				if (!deskInfo.isReady()) {
					allReady = false;
				}
			}
			 
			if (allReady) {
				int now = MiscUtil.getCurrentSeconds();
				list.forEach(e -> {
					scoreHis.put(e.getPostion(), 0);
				});
				startGame(now, list);
			}
		}
    }

    @Override
    public void playerReLogin(PlayerInfo player) {
    	  RoomNiuNiuDeskInfo info = deskInfoMap.get(player);
    	  if(info == null){
    		  return;
    	  }
    	  info.setLeave(false);
    	  
    	  //广播此玩家重新进入了游戏
    	  getPlayerList().forEach(e -> e.write(ResponseCode.COUPLE_PLAYER_ENTER_DESK, CommonCreator.createPBInt32(player.getPlayerId())));
    	  //返回当前桌上游戏数据
    	  player.write(ResponseCode.GRADNIU_DESK_INFO, createDeskInfo(player));
    }
    
    protected Oxc.PBOXCDeskInfo createDeskInfo(PlayerInfo selfPlayer){
    	if(model == 1){
    		Oxc.PBOXCDeskInfo.Builder builder = Oxc.PBOXCDeskInfo.newBuilder();
    		builder.setPlayerList(CoupleCreator.createPBPlayerInfoListGradNiu(deskInfoMap));
    		builder.setRoomId(getConfId());
    		builder.setGameState(state.getId());
    		if(isPersonal()){
    			builder.setRemainTime(0);
    		}else{
    			builder.setRemainTime(stateRemainTime);
    		}
    		builder.setRound(curRound);
    		
    		if(state.getId() >= ZhuangNiuState.GRAB_ZHUANG.getId() && state.getId() <= ZhuangNiuState.PLAYER_CAL.getId()){
    			for (RoomNiuNiuDeskInfo info : deskInfoMap.values()) {
        			if(!info.isWatch()){
        				Oxc.PBOXCPlayerDeskInfo.Builder pdi = Oxc.PBOXCPlayerDeskInfo.newBuilder();
        				pdi.setPos(info.getPositionValue());
        				int cardsNum = 0;
    					if(state == ZhuangNiuState.GRAB_ZHUANG){
    						if(selfPlayer == info.getPlayerInfo()){
    		            		pdi.setBankerTimes(info.getGrabZhuangPos());
    		            	}
    					}
    					if(state.getId() >= ZhuangNiuState.BET.getId()){
    						pdi.setZhuang(zhuangDesk == info);
    						if(pdi.getZhuang()){
    		            		pdi.setBankerTimes(info.getGrabZhuangPos());
    		            	}
    						pdi.setUserTimes(info.getBetPos());
    						if(state.getId() >= ZhuangNiuState.DEAL_CARD2.getId()){
    							cardsNum = 5;
    							pdi.setPlayerCaled(info.isRevcPlayerCal());
    							if(selfPlayer == info.getPlayerInfo() || info.isRevcPlayerCal()){
    								pdi.addAllCards(info.get5HandCardKeyList());
    								pdi.setTyped(info.getResult().getLeft().getValue());
    								pdi.addAllCardsAdd(info.getTowCards());
    	    					}
    						}
    					}
        				
        				pdi.setCardsNum(cardsNum);
        				builder.addPlayerDeskInfo(pdi);
        			}
        		}
    		}
    		return builder.build();
    	}else{
			Oxc.PBOXCDeskInfo.Builder builder = Oxc.PBOXCDeskInfo.newBuilder();
			builder.setPlayerList(CoupleCreator.createPBPlayerInfoListGradNiu(deskInfoMap));
			builder.setRoomId(getConfId());
			builder.setGameState(state.getId());
			if(isPersonal()){
				builder.setRemainTime(0);
			}else{
				builder.setRemainTime(stateRemainTime);
			}
			builder.setRound(curRound);
			
			if(state.getId() >= ZhuangNiuState.DEAL_CARD1.getId() && state.getId() <= ZhuangNiuState.PLAYER_CAL.getId()){
				for (RoomNiuNiuDeskInfo info : deskInfoMap.values()) {
	    			if(!info.isWatch()){
	    				Oxc.PBOXCPlayerDeskInfo.Builder pdi = Oxc.PBOXCPlayerDeskInfo.newBuilder();
	    				pdi.setPos(info.getPositionValue());
	    				int cardsNum = 0;
	    				
	    				if(state.getId() >= ZhuangNiuState.DEAL_CARD1.getId()){
	    					cardsNum += 4;
	    					if(selfPlayer == info.getPlayerInfo()){
	    						pdi.addAllCards(info.get4HandCardKeyList());
	    					}
	    					if(state == ZhuangNiuState.GRAB_ZHUANG){
	    						if(selfPlayer == info.getPlayerInfo()){
	    		            		pdi.setBankerTimes(info.getGrabZhuangPos());
	    		            	}
	    					}
	    					if(state.getId() >= ZhuangNiuState.BET.getId()){
	    						pdi.setZhuang(zhuangDesk == info);
	    						if(pdi.getZhuang()){
	    		            		pdi.setBankerTimes(info.getGrabZhuangPos());
	    		            	}
	    						pdi.setUserTimes(info.getBetPos());
	    						if(state.getId() >= ZhuangNiuState.DEAL_CARD2.getId()){
	    							pdi.setPlayerCaled(info.isRevcPlayerCal());
	    							cardsNum++;
	    							if(selfPlayer == info.getPlayerInfo() || info.isRevcPlayerCal()){
	    								pdi.addCards(info.getRemainHandCardKey());
	    								pdi.setTyped(info.getResult().getLeft().getValue());
	    	    						pdi.addAllCardsAdd(info.getTowCards());
	    	    					}
	    						}
	    					}
	    				}
	    				pdi.setCardsNum(cardsNum);
	    				builder.addPlayerDeskInfo(pdi);
	    			}
	    		}
			}
			return builder.build();
    	}
    }

    @Override
    public void recordPlayerTaxInfo(Object detail, int roomId) {
    	 GrabNiuDetail grab_detail = (GrabNiuDetail) detail;
         for (OnePosDetail one : grab_detail.getRecords()) {
             if (0 >= one.getTax()) {
                 continue;
             }

             TaxRecordUtil.recordPlayerTaxInfoToDB(grab_detail.getType(), one.getPlayerId(), roomId, one.getTax(), one.getChannel_id(), one.getPackage_id(), one.getDevice());
         }
    }

    @Override
    protected Map<Integer, Integer> getAllPlayerMoney() {
    	 Map<Integer, Integer> result = new HashMap<>();
         for (Map.Entry<PlayerInfo, RoomNiuNiuDeskInfo> entry : deskInfoMap.entrySet()) {
             result.put(entry.getValue().getPositionValue(), entry.getKey().getCoin());
         }
         return result;
    }

    @Override
    protected void playerMoneyChangeHook(PlayerInfo info) {
        syncAllPlayerMoney();
    }

    @Override
	public void disbandDesk() {
		if(scoreHis.size() > 0){
			 scoreList.add(new Pair<>(startTime, scoreHis));
		}
		onRoomDeskGameEnd();
		DeskMgr.getInst().removeDesk(this);
    }
    
    @Override
	public void playerWantContinue(PlayerInfo player) {
    	RoomNiuNiuDeskInfo info = deskInfoMap.get(player);
    	if(info == null){
    		return;
    	}
    	//检查这个玩家是否能继续
    	if (!isPlayerMoneyEnough(player)) {
			player.write(ResponseCode.COUPLE_MONEY_NOT_ENOUGH, null);
			return;
		}
		syncAllPlayerMoney();
		msgHelper.notifyMessage(ResponseCode.XUENIU_CONTINUE, CommonCreator.createPBInt32(info.getPositionValue()));
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
	
	 private CoupleMajiang.PBOneGameRecord createPBOneGameRecord(int time, int recordId, Map<XianPosition, Integer> scoreMap) {
	        CoupleMajiang.PBOneGameRecord.Builder builder = CoupleMajiang.PBOneGameRecord.newBuilder();
	        builder.setTime(time);
	        builder.setRecordId(recordId);
	        if (scoreMap != null) {
	            scoreMap.entrySet().forEach(e -> {
	                CoupleMajiang.PBOnePosInfo.Builder subBuilder = CoupleMajiang.PBOnePosInfo.newBuilder();
	                PlayerInfo info = posInfoMap.get(e.getKey()).getPlayerInfo();
	                
	                subBuilder.setName(info.getName());
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
    
    public abstract String getGameName();

    public Map<PlayerInfo, RoomNiuNiuDeskInfo> getDeskInfoMap() {
        return deskInfoMap;
    }

    protected RoomNiuNiuMsgHelper msgHelper = new RoomNiuNiuMsgHelper();

    class RoomNiuNiuMsgHelper {
    	
    	/** 经典牛开始 */
        public void startGameClassNiu(List<RoomNiuNiuDeskInfo> list, RoomNiuNiuDeskInfo info) {
            Oxc.PBOXCGameStart.Builder builder = Oxc.PBOXCGameStart.newBuilder();
            builder.setFirstPos(info.getPositionValue());
            list.forEach(e -> {
                builder.addPlayPos(e.getPositionValue());
            });
            
            builder.setCountDown(stateRemainTime);
            info.getPlayerInfo().write(ResponseCode.GRADNIU_SEND_CARD, builder.build());
        }

		/** 抢庄牛开始 */
        public void startGameGrabNiu(List<RoomNiuNiuDeskInfo> list, RoomNiuNiuDeskInfo info) {
            Oxc.PBOXCGameStart.Builder builder = Oxc.PBOXCGameStart.newBuilder();
            builder.setFirstPos(info.getPositionValue());
            list.forEach(e -> {
                builder.addPlayPos(e.getPositionValue());
            });

            if (!info.isWatch()) {
                builder.addAllCards(info.get4HandCardKeyList());
            } 
            builder.setCountDown(stateRemainTime);
            info.getPlayerInfo().write(ResponseCode.GRADNIU_SEND_CARD, builder.build());
        }

        /** 通知谁是庄 */
        public void notifyZhuang() {
            Oxc.PBOXCBanker.Builder builder = Oxc.PBOXCBanker.newBuilder();
            builder.setBankerPos(zhuangDesk.getPositionValue());
            deskInfoMap.values().stream().filter(e -> !e.isWatch()).forEach(
                    e -> builder.addTimes(CommonCreator.createPBPair(e.getPositionValue(), e.getGrabZhuangPos()))
            );

            deskInfoMap.values().stream().forEach(
                    e -> {
                        builder.setCountDown(ZhuangNiuState.BET.getSecond());

                        e.getPlayerInfo().write(ResponseCode.GRADNIU_NOTIFY_ZHUANG, builder.build());
                    }
            );
        }

        /** 发牌阶段2 */
        public void notifyDealCard2_1() {
            Oxc.PBOXCDealCards.Builder builder = Oxc.PBOXCDealCards.newBuilder();

            deskInfoMap.forEach((e, f) -> {
            	builder.clear();
                if (!f.isWatch()) {
                	builder.addCards(f.getRemainHandCardKey());
                    builder.setTyped(f.getResult().getLeft().getValue());
                }
                builder.setCountDown(ZhuangNiuState.PLAYER_CAL.getSecond());
                builder.setCardNum(1);
                
                e.write(ResponseCode.GRADNIU_DEAL_CARD, builder.build());
            });
        }
        
        /** 发牌阶段2--发五张 */
        public void notifyDealCard2_5() {
            Oxc.PBOXCDealCards.Builder builder = Oxc.PBOXCDealCards.newBuilder();

            deskInfoMap.forEach((e, f) -> {
            	builder.clear();
                if (!f.isWatch()) {
                	builder.addAllCards(f.get5HandCardKeyList());
                    builder.setTyped(f.getResult().getLeft().getValue());
                }
                builder.setCountDown(ZhuangNiuState.PLAYER_CAL.getSecond());
                builder.setCardNum(5);
                
                e.write(ResponseCode.GRADNIU_DEAL_CARD, builder.build());
            });
        }
        
        public void notifyPlayerCal(RoomNiuNiuDeskInfo info) {
        	Oxc.PBOXCPlayerCal.Builder builder = Oxc.PBOXCPlayerCal.newBuilder();
        	builder.setPos(info.getPositionValue());
        	info.getHandCards().forEach(
                    e -> builder.addCardsMain(e.getKey())
            );
        	builder.addAllCardsAdd(info.getTowCards());
        	builder.setTyped(info.getResult().getLeft().getValue());
        	
        	notifyMessage(ResponseCode.GRADNIU_PLAYER_CAL, builder.build());
		}

        public void notifyMessage(ResponseCode code, MessageLite message) {
            for (PlayerInfo player : deskInfoMap.keySet()) {
                player.write(code, message);
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
    }

	@Override
	public void destroy() {
		isDestroyed.set(true);
		stopOperationFuture();
		stopOverMyselfFuture();
		deskInfoMap.clear();
		posInfoMap.clear();
	}
	
	@Override
	public boolean isPlayerLeave(PlayerInfo player) {
		RoomNiuNiuDeskInfo info = getDeskInfo(player);
		if (info == null) {
			return true;
		}
		return info.isLeave();
	}
}
