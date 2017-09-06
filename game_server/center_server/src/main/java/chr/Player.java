package chr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import common.LogHelper;
import config.bean.Province;
import config.bean.TransferData;
import config.provider.ChannelInfoProvider;
import config.provider.DynamicPropertiesPublicProvider;
import config.provider.ProvinceProvider;
import data.OnlineAction;
import define.AppId;
import define.Gender;
import define.Icon;
import define.constant.LoginStatusConst;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import logic.room.LobbyGameManager;
import mail.MailEntity;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import protobuf.CoupleMajiang;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.LogUtil;
import util.MiscUtil;
import util.NettyUtil;

public class Player implements Serializable{
	private static final Logger logger = LoggerFactory.getLogger(Player.class);

	//---player表----
	private int playerId;
	private String name;
	private long coin;
	private int tiyanCoin;
	private String icon;
	private String bankPassword;
	private long bankMoney;
	private int showBankMark;
	private int winScore;                            //胜利获得积分
	private int rechargeScore;                        //充值获得积分
	private int accountId;
	private int complaint_total = 0;
	private int complaint_total_time = 0;
	private int is_send_pay_mail = 0;
	private long pay_total = 0;
	private int game_total = 0;
	private long online_time_duration = 0;
	private int is_open_exchange = 0;
	private long lose_money = 0;
	private long win_money = 0;
	private long pay_money = 0;
	private long pay_money_agent = 0;
	private String gameChannel;
	private int packageId;
	private boolean only_show_agent = false;
	private int exp_level = 0;
	private int agent_plan = 0;
	private int recharge = 0;    //当日充值
	private int current_game_count = 0;    //当日游戏局数
	private int game_time = 0;    //当日游戏时间
	private String ip = "";
	private long exchange_total = 0;
	private long exchange_total_agent = 0;
	private String machine_id = "";
	private long lose_round;
	private long win_round;
	private String game_version;
	private String device = "";
	private Gender gender;
	private int loginStatus;//暂时没有从模块入库
	// `pay_flag` varchar(10) NOT NULL DEFAULT 'A' COMMENT '用户标签:A,B,C,C1,D,D1,E,E1,F,F1',
	// `agent_uptime` int(11) DEFAULT '0' COMMENT '代理状态更新时间',

	//---accounts表--
	private String alipayAccount = "";
	private String alipayName = "";
	private String phone_num = "";
	private String platform_id = "";
	
	//--mail表-
	private List<MailEntity> mailList = new ArrayList<>();
	//---agent_pay--
	private List<TransferData> transferList = new ArrayList<>();
	//---player_score_list--
	private List<CoupleMajiang.PBPairGameNo2Record> recordList = null;
	
	
	/// 内存参数，只在内存中有，不进入数据库
	private int createTime = 0;//这个没有赋值啊
	private int sessionId;
	private long lastSaveTime;
	private int moduleId;
	private int loginTime;
	private DeskInfo deskInfo;
	private boolean logout;
	private String province = "";
	private String city = "";
	private int last_game_time;
	private Map<Integer, Integer> playerCgMap = new HashMap<>();
	private long mem_game_no;
	private String token;
	private Set<Integer> alreadyMatchPlayerIds;
	private int matchingGameId;
	private int matchingRoomId;
	private String app_version;
	private int agentPayTime;
	
	public long crc32;

	/** 离线之后重置在线数据 */
	private void resetOnlineData(){
		this.sessionId = 0;
//		this.lastSaveTime = 0;
		this.moduleId = 0;
		this.loginTime = 0;
		this.last_game_time = 0;
		this.playerCgMap.clear();
		this.mem_game_no = 0;
		this.token = "";
		this.alreadyMatchPlayerIds.clear();
		this.matchingGameId = 0;
		this.matchingRoomId = 0;
	}
	
	//factory method to create default player
	public static Player getDefault(int accountId, String name) {
		Player player = new Player();
		player.playerId = 0;
		player.coin = CenterServer.getInst().getGameInitMoney();
		player.name = name;
		player.gender = Gender.randomGender();
		player.icon = Icon.randomIconString(player.gender.getValue());
		player.tiyanCoin = 200000;
		player.bankMoney = 0;
		player.showBankMark = 0;
		player.bankPassword = "888888";
		player.accountId = accountId;
		player.complaint_total = 0;
		player.complaint_total_time = 0;
		player.is_send_pay_mail = 0;
		player.pay_total = 0;
		player.game_total = 0;
		player.online_time_duration = 0;
		player.is_open_exchange = 0;
		player.lose_money = 0;
		player.win_money = 0;
		player.pay_money = 0;
		player.pay_money_agent = 0;
		player.only_show_agent = false;
		player.exp_level = 0;
		player.agent_plan = 1;
		player.alreadyMatchPlayerIds = new HashSet<>();
		player.complaint_total = 0;
		player.only_show_agent = false;
		player.mem_game_no = 0;
		player.exchange_total = 0;
		player.exchange_total_agent = 0;
		player.machine_id = "";
		player.gameChannel = "0";
		player.lose_round = 0;
		player.win_round = 0;
		player.game_version = "";
		player.matchingGameId = 0;
		player.matchingRoomId = 0;
		return player;
	}

	public void clearTodayStatus() {
		this.current_game_count = 0;
		this.recharge = 0;
		this.game_time = 0;
	}

	public String getGame_version() {
		return game_version;
	}

	public void setGame_version(String game_version) {
		this.game_version = game_version;
	}

	public void addLose_round(int value) {
		this.lose_round += value;
	}

	public long getLose_round() {
		return lose_round;
	}

	public void setLose_round(long lose_round) {
		this.lose_round = lose_round;
	}

	public void addWin_round(int value) {
		this.win_round += value;
	}

	public long getWin_round() {
		return win_round;
	}

	public void setWin_round(long win_round) {
		this.win_round = win_round;
	}

	public String getMachine_id() {
		return machine_id;
	}

	public void setMachine_id(String machine_id) {
		this.machine_id = machine_id;
	}

	public long getMem_game_no() {
		return mem_game_no;
	}

	public void addExchange_total(long value) {
		this.exchange_total += value;
	}

	public long getExchange_total() {
		return exchange_total;
	}

	public void setExchange_total(long exchange_total) {
		this.exchange_total = exchange_total;
	}
	
	public void addExchange_total_agent(long value) {
		this.exchange_total_agent += value;
	}

	public long getExchange_total_agent() {
		return exchange_total_agent;
	}

	public void setExchange_total_agent(long exchange_total_agent) {
		this.exchange_total_agent = exchange_total_agent;
	}
	
	public long getExchangeAll(){
		return exchange_total + exchange_total_agent;
	}

	public void setMem_game_no(long mem_game_no) {
		this.mem_game_no = mem_game_no;
	}

	public int getExp_level() {
		return exp_level;
	}

	public void setExp_level(int exp_level) {
		this.exp_level = exp_level;
	}

	public int getAgent_plan() {
		return agent_plan;
	}

	public void setAgent_plan(int agent_plan) {
		this.agent_plan = agent_plan;
	}

	public boolean isOnly_show_agent() {
		return only_show_agent;
	}

	public void setOnly_show_agent(boolean only_show_agent) {
		this.only_show_agent = only_show_agent;
	}

	public long getPay_money() {
		return pay_money;
	}

	public void setPay_money(long pay_money) {
		this.pay_money = pay_money;
	}

	public void addPay_money(long value) {
		this.pay_money += value;
	}
	
	public long getPay_money_agent() {
		return pay_money_agent;
	}

	public void setPay_money_agent(long pay_money_agent) {
		this.pay_money_agent = pay_money_agent;
	}
	
	public void addPay_money_agent(long value) {
		this.pay_money_agent += value;
	}
	
	public long getPayAll() {
		return pay_money + pay_money_agent;
	}

	public long getLose_money() {
		return lose_money;
	}

	public void setLose_money(long lose_money) {
		this.lose_money = lose_money;
	}

	public long getWin_money() {
		return win_money;
	}

	public void setWin_money(long win_money) {
		this.win_money = win_money;
	}

	public int getIs_open_exchange() {
		return is_open_exchange;
	}

	public void setIs_open_exchange(int is_open_exchange) {
		this.is_open_exchange = is_open_exchange;
	}

//	public List<Player> getAlreadyMatchPlayers() {
//		return alreadyMatchPlayers;
//	}

	public boolean isAlreadyMatchPlayer(Player player) {
		for (Integer id : alreadyMatchPlayerIds) {
			if (id == player.getPlayerId()) {
				return true;
			}
		}
		return false;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void addMatchPlayers(List<Player> playerList) {
		playerList.forEach(e -> {
			if (!alreadyMatchPlayerIds.contains(e.getPlayerId())) {
				alreadyMatchPlayerIds.add(e.getPlayerId());
			}
		});
	}

	public void updateCoin(long value, boolean add) {
		if (add) {
			this.coin += value;
		} else {
			this.coin -= value;
		}
	}

	public void updateBankCoin(long value, boolean add) {
		if (add) {
			this.bankMoney += value;
		} else {
			this.bankMoney -= value;
		}
	}

	public int getComplaint_total() {
		return complaint_total;
	}

	public long getOnline_time_duration() {
		return online_time_duration;
	}

	public void setOnline_time_duration(long online_time_duration) {
		this.online_time_duration = online_time_duration;
	}

	public void addOnline_time_duration(long value) {
		this.online_time_duration += value;
	}

	public int getGame_total() {
		return game_total;
	}

	public void setGame_total(int game_total) {
		this.game_total = game_total;
	}

	public void addGame_total(int value) {
		this.game_total += value;
	}

	public long getPay_total() {
		return pay_total;
	}

	public void setPay_total(long pay_total) {
		this.pay_total = pay_total;
	}

	public void addPay_total(long value) {
		this.pay_total += value;
	}

	public int getIs_send_pay_mail() {
		return is_send_pay_mail;
	}

	public void setIs_send_pay_mail(int is_send_pay_mail) {
		this.is_send_pay_mail = is_send_pay_mail;
	}

	public int getComplaint_total_time() {
		return complaint_total_time;
	}

	public void setComplaint_total_time(int complaint_total_time) {
		this.complaint_total_time = complaint_total_time;
	}

	public void setComplaint_total(int complaint_total) {
		this.complaint_total = complaint_total;
	}

	public void addComplaint_total(int value) {
		this.complaint_total += value;
	}

//	public int getRoomId() {
//		return roomId;
//	}
//
//	public void setRoomId(int roomId) {
//		this.roomId = roomId;
//	}

	public long getLastSaveTime() {
		return lastSaveTime;
	}

	public void setLastSaveTime(long lastSaveTime) {
		this.lastSaveTime = lastSaveTime;
	}

	public static Player getDefault() {
		return getDefault(0, "");
	}

//	public int getGameId() {
//		return gameId;
//	}
//
//	public void setGameId(int gameId) {
//		this.gameId = gameId;
//	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getBankPassword() {
		return bankPassword;
	}

	public void setBankPassword(String bankPassword) {
		this.bankPassword = bankPassword;
	}

	public long getBankMoney() {
		return bankMoney;
	}

	public void setBankMoney(long bankMoney) {
		this.bankMoney = bankMoney;
	}

	public boolean getShowBankMark() {
		return showBankMark == 1;
	}

	public void setShowBankMark(int showBankMark) {
		this.showBankMark = showBankMark;
	}

	public static Logger getLogger() {
		return logger;
	}

	public boolean isLogout() {
		return logout;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public int getTiyanCoin() {
		if (tiyanCoin <= 0) {
			tiyanCoin = 200000;
		}
		return tiyanCoin;
	}

	public void setTiyanCoin(int tiyanCoin) {
		this.tiyanCoin = tiyanCoin;
	}

	public void setMailList(List<MailEntity> mailList) {
		this.mailList = mailList;
	}

	public Gender getGender() {
		return gender;
	}

	public void switchGender() {
		this.gender = this.gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}


	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public long getCoin() {
		return coin;
	}

	public String getGameChannel() {
		return gameChannel;
	}


	public int getPackageId() {
		return packageId;
	}

	public void setPackageId(int packageId) {
		this.packageId = packageId;
	}

	public int getChannelId() {
		try {
			return Integer.parseInt(gameChannel);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void setGameChannel(String gameChannel) {
		this.gameChannel = gameChannel;
	}

	public void setCoin(long coin) {
		this.coin = coin;
	}

	public int getLoginTime() {
		return loginTime;
	}

	public DeskInfo getDeskInfo() {
		return deskInfo;
	}

	/// 玩家是否在游戏中
	public boolean isGameing() {
		return this.getDeskInfo() != null && 0 != this.getDeskInfo().getSessionId();
	}

	public void setDeskInfo(DeskInfo deskInfo) {
		this.deskInfo = deskInfo;
		if(deskInfo == null){
		}else if(!deskInfo.isPrivateRoom()){
			matchingGameId = 0;
			matchingRoomId = 0;
		}
	}

	public void setLoginTime(int loginTime) {
		this.loginTime = loginTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getScore() {
		return this.winScore + this.rechargeScore;
	}

	public int getRechargeScore() {
		return rechargeScore;
	}

	public void setRechargeScore(int rechargeScore) {
		this.rechargeScore = rechargeScore;
	}

	public int getWinScore() {
		return winScore;
	}

	public void setWinScore(int winScore) {
		this.winScore = winScore;
	}

	public void resetScore() {
		this.winScore = 0;
		this.rechargeScore = 0;
	}

	public void addTransferList(TransferData transfer_data) {
		if (transferList == null) {
			transferList = new ArrayList<>();
		}
		transferList.add(transfer_data);
	}

	public List<TransferData> getTransferDataList() {
		return this.transferList;
	}

	public void addMail(MailEntity entity) {
		if (mailList == null) {
			mailList = new ArrayList<>();
		}
		mailList.add(entity);
	}

	public List<MailEntity> getMailList() {
		return this.mailList;
	}

	public MailEntity getMailEntity(int id) {
		for (MailEntity entity : mailList) {
			if (entity.getId() == id) {
				return entity;
			}
		}
		return null;
	}


	public int getAvailableMailId() {
		for (int i = 1; i <= 1000; i++) {
			if (mailIdAvailable(i)) {
				return i;
			}
		}
		return -1;
	}

	private boolean mailIdAvailable(int id) {
		for (MailEntity entity : mailList) {
			if (entity.getId() == id) {
				return false;
			}
		}
		return true;
	}


	public void removeMail(List<Integer> mailList) {
		for (Integer mail : mailList) {
			removeMail(mail);
		}
	}

	public void removeMail(int mail) {
		for (int i = 0; i < mailList.size(); i++) {
			if (mailList.get(i).getId() == mail) {
				mailList.remove(i);
				break;
			}
		}
	}


	public void addRoomRecordList(CoupleMajiang.PBPairGameNo2Record record) {
		if (recordList == null) {
			recordList = new ArrayList<>();
		}
		recordList.add(record);
	}

	public List<CoupleMajiang.PBPairGameNo2Record> getRecordList() {
		return recordList;
	}

	public CoupleMajiang.PBOneRoomRecord getRecordByGameNo(long gameNo) {
		if (recordList == null) {
			return null;
		}
		for (CoupleMajiang.PBPairGameNo2Record record : recordList) {
			if (Long.parseLong(record.getGameNo()) == gameNo) {
				return record.getRecord();
			}
		}
		return null;
	}

	public void logout() {
		logout = false;
		CenterActorManager.getLogicActor(playerId).put(()->{
			logger.info("player {} logout  player id : {}", getName(), getPlayerId());
//			this.addOnline_time_duration(MiscUtil.getCurrentSeconds() - this.getLoginTime());
			LobbyGameManager.getInst().playerExitModule(this);
			
			if (PlayerManager.getInstance().isOnline(playerId)) {
				// 在线时间
				addOnline_time_duration(MiscUtil.getCurrentSeconds() - getLoginTime());
				// 下线日志
				ServerManager.getInst().getMinLoadSession(AppId.LOG).sendRequest(new CocoPacket(RequestCode.LOG_ONLINE,
					LogHelper.logOnline(playerId, getChannelId(), getPackageId(),OnlineAction.LOGOUT.getValue(), getIp(),
                        getProvince(), getCity(), getDevice(), getMachine_id())));
			}
			PlayerManager.getInstance().removePlayer(this);
			updateLoginStatus(LoginStatusConst.EXIT_GAME);
			resetOnlineData();
			PlayerSaver.savePlayer(this);
			return null;
		});
		//这个位置是记录玩家登出的时间					为了做在线的检测
	}

	public void updateLoginStatus(int login) {
		loginStatus = login;
//		CenterActorManager.getDbActor(getPlayerId()).put(() -> {
//			Map<String, Object> data = new HashMap<>();
//			data.put("login", login);
//			Map<String, Object> where = new HashMap<>();
//			where.put("player_id", getPlayerId());
//			try {
//				DBUtil.executeUpdate("player", where, data);
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			return null;
//		});
		PlayerSaver.savePlayerLoginStatus(this);
	}

	public void setSession(ChannelHandlerContext session) {
		if (session == null) {
			return;
		}
		ServerSession serverSession = NettyUtil.getAttribute(session, ServerSession.KEY);
		if (serverSession == null) {
			return;
		}
		this.sessionId = serverSession.getServerId();
	}

	public ChannelHandlerContext getSession() {
		ServerSession serverSession = ServerManager.getInst().getServerSession(AppId.GATE, sessionId);
		if (serverSession == null) {
			return null;
		}
		return serverSession.getIoSession();
	}

	public void write(ResponseCode code, MessageLite message) {
		if (message != null) {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getPlayerId(), code, message.toByteArray().length, message);
		} else {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getPlayerId(), code);
		}
		write(new CocoPacket(code.getValue(), message == null ? null : message.toByteArray(), getPlayerId()));
	}

	public void write(CocoPacket packet) {
		ChannelHandlerContext session = getSession();
		if (session == null) {
			logger.error(" the gate session {} in player {} is null ",sessionId, getPlayerId());
			return;
		}
//		if (packet.getReqId() == ResponseCode.ACCOUNT_LOGIN_SUCC.getValue()) {
//			if (session != null) {
//				ServerSession serverSession = NettyUtil.getAttribute(session, ServerSession.KEY);
//				serverSession.addLoadFactor();
//			}
//		}
		packet.resetReqIdToRequest();
		if (session != null) {
			session.writeAndFlush(packet);
		}
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	public Province getProvinceData() {
		return ProvinceProvider.getInst().getData(province);
	}

	public String getAlipayName() {
		return alipayName;
	}

	public void setAlipayName(String alipayName) {
		this.alipayName = alipayName;
	}

	public String getAlipayAccount() {
		return alipayAccount;
	}

	public void setAlipayAccount(String alipayAccount) {
		this.alipayAccount = alipayAccount;
	}

	public String getPhone_num() {
		return phone_num;
	}

	public void setPhone_num(String phone_num) {
		this.phone_num = phone_num;
	}
	
	public String getPlatform_id() {
		return platform_id;
	}

	public void setPlatform_id(String platform_id) {
		this.platform_id = platform_id;
	}

	public int getRecharge() {
		return recharge;
	}

	public int getCurrent_game_count() {
		return current_game_count;
	}

	public int getGame_time() {
		return game_time;
	}

	public void setRecharge(int recharge) {
		this.recharge = recharge;
	}

	public void setCurrent_game_count(int current_game_count) {
		this.current_game_count = current_game_count;
	}

	public void setGame_time(int game_time) {
		this.game_time = game_time;
	}

	public void addRecharge(int value) {
		this.recharge += value;
	}

	public void addGame_count(int value) {
		this.current_game_count += value;
	}

	public void addGameTime(int value) {
		this.game_time += value;
	}

	public int getLast_game_time() {
		return last_game_time;
	}

	public void setLast_game_time(int last_game_time) {
		this.last_game_time = last_game_time;
	}

	public Map<Integer, Integer> getPlayerCgMap() {
		return playerCgMap;
	}

	public void putPlayerCg(Integer playerCg) {
		playerCgMap.put(playerCg, 1);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getLoginStatus() {
		return loginStatus;
	}

	public int getMatchingGameId() {
		return matchingGameId;
	}

	public void setMatchingGameId(int matchingGameId) {
		this.matchingGameId = matchingGameId;
	}

	public int getMatchingRoomId() {
		return matchingRoomId;
	}

	public void setMatchingRoomId(int matchingRoomId) {
		this.matchingRoomId = matchingRoomId;
	}

	public int getDeskOrMatchGameId(){
		if(deskInfo != null){
			return deskInfo.getGameId();
		}
		return matchingGameId;
	}
	
	public int getDeskOrMatchRoomId(){
		if(deskInfo != null){
			return deskInfo.getRoomId();
		}
		return matchingRoomId;
	}

	public int getDeskRoomId() {
		if(deskInfo != null){
			return deskInfo.getRoomId();
		}
		return 0;
	}
	
	public String getApp_version() {
		return app_version;
	}
	
	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}
	
	public int getAgentPayTime() {
		return agentPayTime;
	}

	public void setAgentPayTime(int agentPayTime) {
		this.agentPayTime = agentPayTime;
	}

	public boolean isReview() {  
		// 区域控制进入审核
		if (getProvince().equals(DynamicPropertiesPublicProvider.getInst().getProvinceOtherName()) &&
				DynamicPropertiesPublicProvider.getInst().isProvinceReview()){
			logger.info("player province:{} and province review is open", getProvince());
			return true;
		}
		// 等于当前版本不等于审核版本才收后台控制
		int appVersion = (int) Double.parseDouble(app_version);
//		int curVersion = Integer.parseInt(
//				ChannelInfoProvider.getInst().getChannelConfig(getChannelId(), getPackageId(), false).getCur_version());
//		int reviewVersion = Integer.parseInt(ChannelInfoProvider.getInst()
//				.getChannelConfig(getChannelId(), getPackageId(), false).getReview_version());
		int curVersion = 1;
		int reviewVersion = 1;
		return !(appVersion == curVersion && appVersion != reviewVersion);
	}
	
	public boolean isReviewRechange() {
		int appVersion = (int) Double.parseDouble(app_version);
		int curVersion = Integer.parseInt(
				ChannelInfoProvider.getInst().getChannelConfig(getChannelId(), getPackageId(), false).getCur_version());
		int reviewVersion = Integer.parseInt(ChannelInfoProvider.getInst()
				.getChannelConfig(getChannelId(), getPackageId(), false).getReview_version());
		return appVersion == reviewVersion && appVersion != curVersion;
	}
	
}
