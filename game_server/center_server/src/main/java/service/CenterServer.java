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


	private CenterServer() {

	}

	public static CenterServer getInst() {
		return instance;
	}

	private NetServer server;

	public void reloadDynamicProps() {
	}

	public Account.PBDynamicConfig createPBDynamicConfig(int channelId, int packageId, Player player) {
		return null;
	}


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
		initDataBase();
		checkRandomName();
		PlayerNameManager.getInst().loadNameInfo();
		reloadDynamicProps();
		initStaticConfig();
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
			PlayerManager.getInstance().saveAllPlayers(SAVE_INTERNAL, 1000000);
		}, CenterActorManager.getDbCheckActor(), "save_player");
		CenterActorManager.getLogicTimer().register(1000, 1000, () -> DailyFlush.getInstance().update(), CenterActorManager.getDbCheckActor(), "update");
		CenterActorManager.getLogicTimer().register(5 * 60 * 1000, 5 * 60 * 1000, this::logOnlinePeople, CenterActorManager.getDbCheckActor(), "player_onile_log");
		CenterActorManager.getLogicTimer().register(10 * 1000, 10 * 1000, this::logFileOnlinePeople, CenterActorManager.getDbCheckActor(), "player_onile_file");
	}

	private void logFileOnlinePeople() {
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

	@Override
	protected void afterStart() {
		File stopFile = new File("stop");
		File reloadFile = new File("reload");
		while (true) {
			try {
				Thread.sleep(3 * 1000);
			} catch (Exception e) {
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

	public void sendStopServerRequest(ServerSession e, int endTime) {
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
		if (req == null) {
			return;
		}
		e.sendRequest(new CocoPacket(req, CommonCreator.createPBInt32(endTime)));
	}

	private void stopServer() throws InterruptedException {
		logger.info("  check the stop file and server begin stop ");
		for (AppId appId : AppId.values()) {
			List<ServerSession> sessionList = ServerManager.getInst().getSessionList(appId);
			if (sessionList != null && sessionList.size() > 0) {
				//指定时间后关闭
				sessionList.forEach(e -> {
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
