package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.CenterActorManager;
import actor.IActor;
import actor.IActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.JsonUtil;
import config.bean.ChannelConfig;
import config.provider.AgentInfoProvider;
import config.provider.BaseProvider;
import config.provider.ChannelInfoProvider;
import config.provider.ConfNiuProvider;
import config.provider.DynamicPropertiesPublicProvider;
import config.provider.RankInfoProvider;
import database.DBManager;
import database.DBUtil;
import database.DataQueryResult;
import define.AppId;
import define.GameType;
import handle.CenterHandler;
import logic.DailyFlush;
import logic.name.PlayerNameManager;
import logic.room.LobbyGameManager;
import network.NetServer;
import network.ServerManager;
import network.ServerSession;
import network.codec.MessageDecoder;
import network.codec.MessageEncoder;
import network.handler.CenterMessageHandler;
import packet.CocoPacket;
import protobuf.Account;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import timer.ActTimer;
import util.ASObject;
import util.MiscUtil;
import util.Pair;
import util.XProperties;

/**
 * Created by Administrator on 2017/2/4.
 */
public class CenterServer extends BaseApp {
	private static Logger logger = LoggerFactory.getLogger(CenterServer.class);

	private static CenterServer instance = new CenterServer();

	public static final int SAVE_INTERNAL = 5 * 60 * 1000;                            //mills

	private XProperties dynamicProps = null;

	//一些动态的配置文件
	private boolean chargeOpen = false;

	private boolean exchangeOpen = false;

	private boolean rankReward = false;

	private boolean iosChargeOpen = false;

	private int gameTaxRate = 0;

	private int niuniuBannerWinRate = 0;

	private int exchangeTaxRate = 0;

	private int gameInitMoney = 0;

	private int upgradeCoin = 0;

	private boolean iosInnerCharge = false;

	private int roomEnterLimit = 0;

	private int xueniu8Need = 0;

	private int xueniu16Need = 0;

	private int xuezhan8Need = 0;

	private int xuezhan16Need = 0;

	private int exchangeRate = 0;

	private boolean stop = false;

	private String payUrl = "";

	private String returnUrl = "";

	private String exchangeUrl = "";

	private String exchangeKey = "";

	private String exchangeResultKey = "";

	private String backUrl = "";

	private String charge_secret_key = "";

//	private int niuniuServerId = 0;
	
	private int reviewChannel = 0;
	
	private int reviewPackage = 0;
	
	private int exchangeRetain = 0;
	
	private int agentPayRetain = 0;
	
	private int agent_transfer_page_count = 0;
	private boolean switch1;
	private boolean switch2;
	private boolean switch3;
	private boolean switch4;
	private boolean switch5;
	private boolean switch6;
	private int queueSize;
	private int mem_remove_player_time;
	private String onlineNumLogFilePath;
	private String lineSeparator;//临时系统变量
	private boolean useSlb;
	private Map<String, List<String>> gate2Slb = new HashMap<String, List<String>>();//临时系统变量
	private boolean mj_continue_is_change_desk;
	private String complaint_agent_url;
	private String complaint_cs_url;

	private CenterServer() {

	}

	public static CenterServer getInst() {
		return instance;
	}

	private NetServer server;

	public void reloadDynamicProps() {
        logger.info("  check the reload file and server begin reload dynamic.properties ");
		dynamicProps = new XProperties();
		dynamicProps.load(getConfDir() + "dynamic.properties");
		niuniuBannerWinRate = dynamicProps.getInteger("niuniu.banner.win", 20);
		chargeOpen = dynamicProps.getBoolean("charge.open", false);
		exchangeOpen = dynamicProps.getBoolean("exchange.open", false);
		gameTaxRate = dynamicProps.getInteger("taxRate", 5);
		rankReward = dynamicProps.getBoolean("rankRewardOpen", false);
		exchangeTaxRate = dynamicProps.getInteger("exchange.tax.rate", 5);
		gameInitMoney = dynamicProps.getInteger("gameInitCoin", 0);
		upgradeCoin = dynamicProps.getInteger("upgradeCoin", 0);
		iosChargeOpen = dynamicProps.getBoolean("iosChargeOpen", false);
		iosInnerCharge = dynamicProps.getBoolean("iosInnerCharge", false);
		roomEnterLimit = dynamicProps.getInteger("roomEnterLimit", 200);
		xueniu8Need = dynamicProps.getInteger("xueniu8Need", 0);
		xueniu16Need = dynamicProps.getInteger("xueniu16Need", 0);
		xuezhan8Need = dynamicProps.getInteger("xuezhan8Need", 0);
		xuezhan16Need = dynamicProps.getInteger("xuezhan16Need", 0);
		exchangeRate = dynamicProps.getInteger("exchangeRate", 2);
		payUrl = dynamicProps.getString("payUrl", "http://www.baidu.com");
		agent_transfer_page_count = dynamicProps.getInteger("agent_transfer_page_count", 6);
		returnUrl = dynamicProps.getString("returnUrl", "http://www.baidu.com");
		exchangeUrl = dynamicProps.getString("exchangeUrl", "http://192.168.0.110:10086/exchange/exchange");
		exchangeKey = dynamicProps.getString("exchangeKey", "jnbrjwhb0oek5yuzp059c54eci84suqb");
		exchangeResultKey = dynamicProps.getString("exchangeResultKey", "14786585c479befc69c186d77792f540");
		backUrl = dynamicProps.getString("backUrl", "http://127.0.0.1:10086");
		charge_secret_key = dynamicProps.getString("charge_secret_key", "");
		queueSize = dynamicProps.getInteger("match.queue.size", 10);
		switch1 = dynamicProps.getBoolean("match.switch1", true);
		switch2 = dynamicProps.getBoolean("match.switch2", true);
		switch3 = dynamicProps.getBoolean("match.switch3", true);
		switch4 = dynamicProps.getBoolean("match.switch4", true);
		switch5 = dynamicProps.getBoolean("match.switch5", true);
		switch6 = dynamicProps.getBoolean("match.switch6", true);
		mem_remove_player_time = dynamicProps.getInteger("mem_remove_player_time", 3 * 24 * 60 * 60);
		onlineNumLogFilePath = dynamicProps.getString("onlineNumLogFilePath","").trim();
		useSlb = dynamicProps.getBoolean("useSlb", false);
		reviewChannel = dynamicProps.getInteger("reviewChannel", 0);
		reviewPackage = dynamicProps.getInteger("reviewPackage", 0);
		exchangeRetain = dynamicProps.getInteger("exchangeRetain", 0);
		agentPayRetain = dynamicProps.getInteger("agentPayRetain", 0);
		mj_continue_is_change_desk = dynamicProps.getBoolean("mj_continue_is_change_desk", false);
		complaint_agent_url = dynamicProps.getString("complaint_agent_url", "http://192.168.10.93:8521/send/index.html");
		complaint_cs_url = dynamicProps.getString("complaint_cs_url", "http://192.168.10.93:8521/send");
	}

	public Account.PBDynamicConfig createPBDynamicConfig(int channelId, int packageId, Player player) {
		Account.PBDynamicConfig.Builder builder = Account.PBDynamicConfig.newBuilder();
		builder.setRoomEnterLimit(roomEnterLimit);
		builder.setMjRoomConf(CoupleRoomInfoProvider.getInst().getConfString());
		builder.setRankConf(RankInfoProvider.getInst().getConfString());
		builder.setAgentInfo(AgentInfoProvider.getInst().getAgentInfoByPlan(player.getPlatform_id(), player.getAgent_plan()));
		ChannelConfig config = ChannelInfoProvider.getInst().getChannelConfig(channelId, packageId, player.isReview());
		builder.setBroadcastMethod(config.getBroadcastMethod());
		builder.setInvite(config.getInviteMethod());
		builder.setShareMethod(config.getShareMethod());
		builder.setChargeOpen(true);

//		if (true == config.isExchange()) {
//			if (null != player && 1 == player.getIs_open_exchange()) {
//				builder.setExchangeOpen(config.isExchange());
//			} else {
//				builder.setExchangeOpen(false);
//			}
//		} else {
			builder.setExchangeOpen(true);
//		}

		builder.setRankOpen(true);
		builder.setBackOpen(true);
		builder.setAnnounceOepn(true);
		builder.setCustonOepn(true);
		builder.setPersonalRoom(true);
		builder.setUrlShare(null == config.getUrl_share() ? "" : config.getUrl_share());
		builder.setXueliuNeed1(xueniu8Need);
		builder.setXueliuNeed2(xueniu16Need);
		builder.setXuezhanNeed1(xuezhan8Need);
		builder.setXuezhanNeed2(xuezhan16Need);
//		builder.setNiuniuBannerCoin(DynamicInfoProvider.getInst().getBankCoin());
//		builder.setNiuniuBannerLimit(DynamicInfoProvider.getInst().getBannerLimit());
//		builder.setNiuniuBannerRounds(DynamicInfoProvider.getInst().getBannerTimes());
		builder.setQqIcon(config.getQq_icon());
		builder.setComplaintTotal(config.getComplaint_total());
		builder.setUrlQq(null == config.getUrl_qq() ? "" : config.getUrl_qq());
		builder.setUrlAgentRequest(null == config.getUrl_agent_request() ? "" : config.getUrl_agent_request());
		builder.setIsAgent(config.is_agent());
		builder.setIsAgentCharge(config.is_agent_charge());
		builder.setIsTranferOpen(config.is_tranfer_open());
//		builder.setNiuniuSmallChip(DynamicInfoProvider.getInst().getNiuniu_small_chip());
		builder.setGrabNiu(CoupleRoomInfoProvider.getInst().getGrabNiuConf());
		builder.setConfNiu(ConfNiuProvider.getInst().getConfString());
		builder.setExchangeConfig(DynamicInfoProvider.getInst().createPBDynamicConfig());
		builder.setMjContinueIsChangeDesk(mj_continue_is_change_desk);
		builder.setConfigChannelId(String.valueOf(config.getId()));
		builder.setConfigPlatformId("android");
//		builder.setConfigPlatformId(config.getPlatform_id());
		builder.setPayTypeStatus(DynamicPropertiesPublicProvider.getInst().getPayTypeStatus());
		builder.setGameTypeLimit("");
		builder.setGameTypeLimit("1");
		return builder.build();
	}

	public int getUpgradeCoin() {
		return upgradeCoin;
	}

	public void setUpgradeCoin(int upgradeCoin) {
		this.upgradeCoin = upgradeCoin;
	}

//	public int getNiuniuServerId() {
//		return niuniuServerId;
//	}

//	public ServerSession getNiuniuSession() {
//		ServerSession session = ServerManager.getInst().getNiuniuSession(niuniuServerId);
//		session.setLoadFactor(100000);
//		niuniuServerId = session.getServerId();
//		return session;
//	}

	//同IP 不匹配
	public boolean isMatchSwitchIp() {
		return false;
	}

	//每个玩家每天匹配一次
	public boolean isMatchSwitchOne() {
		return false;
	}

	public boolean isMatchSwitchQueueSize() {
		return false;
	}

	public boolean isMatchSwitchDevice() {
		return switch4;
	}

	public boolean isMatchSwitchOld() {
		return switch5;
	}

	public boolean isMatchSwitchPay() {
		return switch6;
	}

	public int getAgent_transfer_page_count() {
		return agent_transfer_page_count;
	}

	public void setAgent_transfer_page_count(int agent_transfer_page_count) {
		this.agent_transfer_page_count = agent_transfer_page_count;
	}

//	public void setNiuniuServerId(int niuniuServerId) {
//		this.niuniuServerId = niuniuServerId;
//	}

	public String getCharge_secret_key() {
		return charge_secret_key;
	}

	public void setCharge_secret_key(String charge_secret_key) {
		this.charge_secret_key = charge_secret_key;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public String getExchangeResultKey() {
		return exchangeResultKey;
	}

	public void setExchangeResultKey(String exchangeResultKey) {
		this.exchangeResultKey = exchangeResultKey;
	}

	public String getExchangeUrl() {
		return exchangeUrl;
	}

	public void setExchangeUrl(String exchangeUrl) {
		this.exchangeUrl = exchangeUrl;
	}

	public String getExchangeKey() {
		return exchangeKey;
	}

	public void setExchangeKey(String exchangeKey) {
		this.exchangeKey = exchangeKey;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getPayUrl() {
		return payUrl;
	}

	public void setPayUrl(String payUrl) {
		this.payUrl = payUrl;
	}

	public int getExchangeRate() {
		return exchangeRate;
	}

	public int getRoomEnterLimit() {
		return roomEnterLimit;
	}

	public int getXueniu8Need() {
		return xueniu8Need;
	}

	public int getXueniu16Need() {
		return xueniu16Need;
	}

	public int getXuezhan8Need() {
		return xuezhan8Need;
	}

	public int getXuezhan16Need() {
		return xuezhan16Need;
	}

	public boolean isChargeOpen() {
		return chargeOpen;
	}

	public boolean isExchangeOpen() {
		return exchangeOpen;
	}

	public boolean isRankReward() {
		return rankReward;
	}

	public boolean isIosChargeOpen() {
		return iosChargeOpen;
	}

	public int getGameTaxRate() {
		return gameTaxRate;
	}

	public int getNiuniuBannerWinRate() {
		return niuniuBannerWinRate;
	}

	public int getExchangeTaxRate() {
		return exchangeTaxRate;
	}

	public int getGameInitMoney() {
		return gameInitMoney;
	}

	public boolean isIosInnerCharge() {
		return iosInnerCharge;
	}

	public boolean isStop() {
		return stop;
	}

	public boolean isUseSlb() {
		return useSlb;
	}
	
	public Map<String, List<String>> getGate2Slb() {
		return gate2Slb;
	}

	public void setGate2Slb(Map<String, List<String>> gate2Slb) {
		this.gate2Slb = gate2Slb;
	}
	
	public int getReviewChannel() {
		return reviewChannel;
	}

	public void setReviewChannel(int reviewChannel) {
		this.reviewChannel = reviewChannel;
	}

	public int getReviewPackage() {
		return reviewPackage;
	}

	public void setReviewPackage(int reviewPackage) {
		this.reviewPackage = reviewPackage;
	}
	
	public int getExchangeRetain() {
		return exchangeRetain;
	}

	public void setExchangeRetain(int exchangeRetain) {
		this.exchangeRetain = exchangeRetain;
	}

	public int getAgentPayRetain() {
		return agentPayRetain;
	}

	public void setAgentPayRetain(int agentPayRetain) {
		this.agentPayRetain = agentPayRetain;
	}
	
	public String getComplaint_agent_url() {
		return complaint_agent_url;
	}

	public void setComplaint_agent_url(String complaint_agent_url) {
		this.complaint_agent_url = complaint_agent_url;
	}

	public String getComplaint_cs_url() {
		return complaint_cs_url;
	}

	public void setComplaint_cs_url(String complaint_cs_url) {
		this.complaint_cs_url = complaint_cs_url;
	}

	private void initDataBase() {
		DBManager.setProps(props);
		DBManager.setDefaultDatabase("yc_game");
		try {
			DBManager.touch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	class RandomName {
		private String name1;
		private String name2;


		public String getName1() {
			return name1;
		}

		public void setName1(String name1) {
			this.name1 = name1;
		}

		public String getName2() {
			return name2;
		}

		public void setName2(String name2) {
			this.name2 = name2;
		}
	}

	public void initRandomName() {
		//insert random name
		BaseProvider.CONF_PATH = getConfDir();
		RandomName[] list = JsonUtil.getGson().fromJson(JsonUtil.getJsonString("name.json"), RandomName[].class);

		Map<String, Object> data = new HashMap<>();
		for (RandomName s : list) {
			data.clear();
			if (s == null) {
				continue;
			}
			data.put("first", s.getName1());
			data.put("last", s.getName2());
			try {
				DBUtil.executeInsert("random_name", data);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean checkRandomName() {
		try {
			int count = DBUtil.executeCount("random_name", null);
			if (count < 100) {
				logger.info(" init the random name table ");
				initRandomName();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void initServer() {
		lineSeparator = System.getProperty("line.separator");
		initDataBase();
		checkRandomName();
		PlayerNameManager.getInst().loadNameInfo();
		reloadDynamicProps();
		initStaticConfig();
		intGateSlbConf();
		LobbyGameManager.getInst().init();
		CenterActorManager.getInstance().start();
//		DataManager.getInst().init(100000, 50000);
//		DataManager.getInst().start();
		int serverPort = props.getInteger("center.port", 9090);
		logger.debug(" the server port is {}", serverPort);
		server = new NetServer(serverPort);
		server.setDecoder(new MessageDecoder());
		server.setEncoder(new MessageEncoder());
		server.setHandler(new CenterMessageHandler());
		server.start();
		new Thread(() -> initHttpServer()).start();
	}

	@Override
	protected void registerExtraCronTask() {
		CenterActorManager.getDBTimer().register(SAVE_INTERNAL, SAVE_INTERNAL, () -> {
			PlayerManager.getInstance().saveAllPlayers(SAVE_INTERNAL,mem_remove_player_time);
		}, CenterActorManager.getDbCheckActor(), "save_player");
		CenterActorManager.getLogicTimer().register(1000, 1000, () -> DailyFlush.getInstance().update(), CenterActorManager.getDbCheckActor(), "update");
		CenterActorManager.getLogicTimer().register(5 * 60 * 1000, 5 * 60 * 1000, this::logOnlinePeople, CenterActorManager.getDbCheckActor(), "player_onile_log");
		CenterActorManager.getLogicTimer().register(10 * 1000, 10 * 1000, this::logFileOnlinePeople, CenterActorManager.getDbCheckActor(), "player_onile_file");
	}
	
	private void logFileOnlinePeople() {
		Map<String, Integer> roomNum = new TreeMap<>();
		//大厅
		String key = "0" + "_" + "0";
		Integer num = roomNum.get(key);
		if(num == null){
			num = 0;
		}
		roomNum.put(key, num);
		//房间中
		for (GameType type : GameType.values()) {
			List<Pair<Integer, Integer>> list = LobbyGameManager.getInst().getRoomPlayerList(type.getValue());
			for (Pair<Integer, Integer> p : list) {
				int stauts = CoupleRoomInfoProvider.getInst().getRoomStatus(p.getLeft());
				if(stauts != 0){
					key = type.getValue() + "_" + p.getLeft();
					num = roomNum.get(key);
					if(num == null){
						num = 0;
					}
					roomNum.put(key, num);
					
					//这类游戏人数
					key = type.getValue() + "_" + "0";
					num = roomNum.get(key);
					if(num == null){
						num = 0;
					}
					roomNum.put(key, num);
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		Collection<Player> players = PlayerManager.getInstance().getOnlinePlayers();
		int totalNum = players.size();
		for (Player player : players) {
			int gameId = player.getDeskOrMatchGameId();
			int roomId = player.getDeskOrMatchRoomId();
			if(gameId != 0 && roomId != 0){
				if(roomId < 10000){//私房
					//桌子上
					key = gameId + "_" + roomId;
					num = roomNum.get(key);
					if(num == null){
						num = 0;
					}
					num++;
					roomNum.put(key, num);
				}
				
				//这类游戏人数
				key = gameId + "_" + "0"; 
				num = roomNum.get(key);
				if(num == null){
					num = 0;
				}
				num++;
				roomNum.put(key, num);
			}else{
				//大厅
				key = "0" + "_" + "0";
				num = roomNum.get(key);
				num++;
				roomNum.put(key, num);
			}
		}
		buffer.append("total ").append(totalNum).append(lineSeparator);
		for (Entry<String, Integer>  entry : roomNum.entrySet()) {
			buffer.append(entry.getKey()).append(" ").append(entry.getValue()).append(lineSeparator);
		}
		if(onlineNumLogFilePath != null && !"".equals(onlineNumLogFilePath)){ 
			try {
				FileWriter file = new FileWriter(onlineNumLogFilePath);
				file.append(buffer);
				file.flush();
				file.close();
			} catch (IOException e) {
				logger.error("",e);
			}
		}
	}

	private void logOnlinePeople() {
		logger.info("allPlayer num {}", PlayerManager.getInstance().getAllPlayers().size());
		logger.info("online num {}", PlayerManager.getInstance().getPlayerCount());
		for (GameType type : GameType.values()) {
			List<Pair<Integer, Integer>> list = LobbyGameManager.getInst().getRoomPlayerList(type.getValue());
			logger.info("game type : {}", type);
			logger.info("room player num : {}", list);
			logger.info("room status : {}", LobbyGameManager.getInst().getRoomStatusList(type.getValue()));
		}
	}

	private void initHttpServer() {
		Server httpServer = new Server(props.getInteger("http.port", 9093));
		httpServer.setHandler(new CenterHandler());
		try {
			httpServer.start();
			httpServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initStaticConfig() {
		BaseProvider.init();
		BaseProvider.loadAll();
	}
	
	public void intGateSlbConf() {
		Map<String, Object> where = new HashMap<>();
		where.put("status", 1);
		List<ASObject> dataList = DataQueryResult.load("s_gate_slb", where);
		Map<String, List<String>> gate2Slb = new HashMap<String, List<String>>();
		for (ASObject asObj : dataList) {
			String slbAddrsArr = asObj.getString("slbAddr");
			if(slbAddrsArr == null || slbAddrsArr.trim().equals("")){
				continue;
			}
			List<String> slbList = gate2Slb.get(asObj.getString("gateIp"));
			if(slbList == null){
				slbList = new ArrayList<>();
				gate2Slb.put(asObj.getString("gateIp"), slbList);
			}
			slbList.add(slbAddrsArr);
		}
		logger.info("slb配置信息：{} 是否启用{}",dataList,useSlb);
		setGate2Slb(gate2Slb);
	}

	@Override
	protected void afterStart() {
		File stopFile = new File("stop");
		File reloadFile = new File("reload");
		while (true) {
			try {
				Thread.sleep(3 * 1000);
//				Thread.sleep(1 * 1000);
			} catch (Exception e) {
				// do nothing
			}
			checkStop(stopFile);
			checkReloadFile(reloadFile);
		}
	}

	private static void checkReloadFile(File reloadFile) {
		if (reloadFile != null && reloadFile.exists()) {
			try {
				reloadFile.delete();
				CenterServer.getInst().reloadDynamicProps();
			} catch (Exception e) {
				// ignore
				logger.error("", e);
			}
		}
	}


	private static void checkStop(File stopFile) {
		if (stopFile != null && stopFile.exists()) {
			try {
				stopFile.delete();
				CenterServer.getInst().stopServer();
			} catch (Exception e) {
				// ignore
				logger.error("", e);
			}
		}
	}
	
	public void sendStopServerRequest(ServerSession e,int endTime){
		RequestCode req = null;
		switch (e.getAppId()) {
		case LOGIN:
			req = RequestCode.LOGIN_REMOVE_SERVER;
			break;
		case LOGIC:
			req = RequestCode.LOGIC_REMOVE_SERVER;
			break;
		case LOG:
			req = RequestCode.LOG_REMOVE_SERVER;
			break;
		case GATE:
			req = RequestCode.GATE_REMOVE_SERVER;
			break;
		default:
			break;
		}
		if(req == null){
			return;
		}
		e.sendRequest(new CocoPacket(req, CommonCreator.createPBInt32(endTime)));
	}

	private void stopServer() throws InterruptedException {
		logger.info("  check the stop file and server begin stop ");
		stop = true;
//		List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.GATE);
//		if (sessionList == null) {
//			logger.warn(" what the fuck why i can't find any  gate app ");
//			return;
//		}
//		sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.GATE_KICK_ALL_PLAYER, null)));
		for (AppId appId : AppId.values()) {
			List<ServerSession> sessionList = ServerManager.getInst().getSessionList(appId);
			if(sessionList != null && sessionList.size() > 0){
				//指定时间后关闭
				sessionList.forEach(e ->{ 
					sendStopServerRequest(e, MiscUtil.getCurrentSeconds());
				});
			}
		}
				
		for (Player player : PlayerManager.getInstance().getOnlinePlayers()) {
			if (player == null) {
				continue;
			}
			player.logout();
		}
		for (Player player : PlayerManager.getInstance().getAllPlayers()) {
			PlayerSaver.savePlayer(player);
		}
		
		// 判断dbSaveThread是否完成
		CenterActorManager.getInstance().stopWhenEmpty();
		CenterActorManager.getInstance().waitForStop();
//		DataManager.getInst().flush();
		
		logger.info(" system exit  when  db actors success ");
		System.exit(0);
	}

	@Override
	protected IActor getCrontabActor() {
		return CenterActorManager.getDbCheckActor();
	}

	@Override
	protected ActTimer getCrontabTimer() {
		return CenterActorManager.getDBTimer();
	}

	@Override
	protected IActorManager getActorManager() {
		return CenterActorManager.getInstance();
	}

	@Override
	protected String getServerName() {
		return "center_server";
	}

	@Override
	protected AppId getAppId() {
		return AppId.CENTER;
	}

	public static void main(String[] args) {
		CenterServer.getInst().start();
	}

	@Override
	public void stop() {
		try {
			stopServer();
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

}
