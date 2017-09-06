package server;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.IActor;
import actor.IActorManager;
import database.DBManager;
import database.DataQueryResult;
import define.AppId;
import network.AbstractHandlers;
import server.actor.LoginActorManager;
import server.handler.LoginHandler;
import server.handler.LoginServerHandler;
import server.ip.IP;
import service.BaseApp;
import timer.ActTimer;
import util.ASObject;

/**
 * Created by Administrator on 2017/2/4.
 */
public class LoginServer extends BaseApp {
	private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);
	private static LoginServer instance = new LoginServer();

	private LoginServer() {

	}

//	public Map<String, LoginHandler.ValidCodeInfo> validCodeInfoMap = new HashMap<>();
	
	private ConcurrentHashMap<Integer, Integer> package2channel = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<Integer, String> package2plat = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, Boolean> province2open = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, Boolean> openSet = new ConcurrentHashMap<>();
	
	private String provinceOtherPlace = "";
	
//	public void addValidCodeInfo(String phone, LoginHandler.ValidCodeInfo info) {
//		logger.info(" add valid code info and the code info is {} : {}", phone, info.validCode);
//		validCodeInfoMap.put(phone, info);
//	}
//
//	public LoginHandler.ValidCodeInfo getValidCodeInfo(String phone) {
//		return validCodeInfoMap.get(phone);
//	}

	public int getChannelForPackageId(int packageId) {
		return package2channel.get(packageId);
	}

	public String getPlatformForPackageId(int packageId) {
		return package2plat.get(packageId);
	}

	public static LoginServer getInst() {
		return instance;
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
	
	/**
	 * 加载包与渠道,平台对应关系
	 */
	private void initPlatData() {
		ConcurrentHashMap<Integer, String> package2plat = new ConcurrentHashMap<>();
		ConcurrentHashMap<Integer, Integer> package2channel = new ConcurrentHashMap<>();
		List<ASObject> userList = DataQueryResult.load("conf_channel_switch", new HashMap<>());
		for (ASObject data : userList) {
			int packageId = data.getInt("package_id");
			int channelId = data.getInt("id");
			String platfromId = data.getString("platform_id");
			package2plat.put(packageId, platfromId);
			package2channel.put(packageId, channelId);
		}
		this.package2plat = package2plat;
		this.package2channel = package2channel;
		logger.info("加载 包对应平台{}", this.package2plat.toString());
		logger.info("加载 包对应渠道{}", this.package2channel.toString());
	}
	
	/**
	 * 加载全局开关与共享地区名字
	 */
	private void initPublicSet() {
		String provinceOtherPlace = "";
		ConcurrentHashMap<String, Boolean> openSet = new ConcurrentHashMap<>();
		List<ASObject> data = DataQueryResult.load("conf_dynamic_properties_public", new HashMap<>());
		for (ASObject obj : data) {
			openSet.put(obj.getString("key"), obj.getInt("status") == 1);
			if (obj.getString("key").equals("provinceotherplace")) {
				provinceOtherPlace = obj.getString("value");
			}
		}
		this.openSet = openSet;
		this.provinceOtherPlace = provinceOtherPlace;
		logger.info("加载 全局开关状态{}", this.openSet.toString());
		logger.info("加载 共享地区名:{}", this.provinceOtherPlace);
	}
	
	
	/**
	 * 加载 区域维护状态 
	 */
	private void initProvinceOpen() {
		ConcurrentHashMap<String, Boolean> province2open = new ConcurrentHashMap<>();
		List<ASObject> userList = DataQueryResult.load("conf_province", new HashMap<>());
		for (ASObject data : userList) {
			province2open.put(data.getString("province"), data.getInt("maintenance") == 1);
		}
		this.province2open = province2open;
		logger.info("加载 区域维护状态{}", this.province2open.toString());
	}
	
	/**
	 * 是否开启灾备备用短信
	 * @return
	 */
	public boolean isGeneSpare() {
		return openSet.get("ongenespare");
	}

	/**
	 * 是否开启登陆开关
	 * @return
	 */
	public boolean isOpenLogin() {
		return true;
//		return openSet.get("onlogin");
	}
	
	/**
	 * 获取维护状态
	 * @param name
	 * @return
	 */
	public boolean getProvinceOpen(String name) {
		Boolean open = province2open.get(name);
		if (open == null) {
			open = province2open.get(provinceOtherPlace);
		}
		if (open == null) {
			logger.error("找不到 {} 地区配置 且并没有配置共享地址!", name);
		}
		return open == null ? false : open;
	}
	
	/**
	 * 加载缓存配置
	 */
	public void initLoginConf(){
		initPlatData();
		initPublicSet();
		initProvinceOpen();
	}

	@Override
	protected void initServer() {
		initDataBase();
		initLoginConf();
		LoginActorManager.getInstance().start();
		IP.load(getConfDir() + "17monipdb.dat");
		Server server = new Server(props.getInteger("login.port", 9098));
		server.setHandler(new LoginHandler());
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getServerName() {
		return "login_server";
	}

	@Override
	protected void afterStart() {

	}


	@Override
	protected IActor getCrontabActor() {
		return LoginActorManager.getPingActor();
	}

	@Override
	protected ActTimer getCrontabTimer() {
		return LoginActorManager.getTimer();
	}

	@Override
	protected IActorManager getActorManager() {
		return LoginActorManager.getInstance();
	}
	
	@Override
	protected AbstractHandlers getClientHandler() {
		return new LoginServerHandler();
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOGIN;
	}

	public static void main(String[] args) {
		LoginServer.getInst().start();
	}

	@Override
	public void stop() {
		System.exit(0);
	}
}
