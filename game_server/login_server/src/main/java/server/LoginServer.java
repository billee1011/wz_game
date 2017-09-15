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
import util.MapObject;

/**
 * Created by Administrator on 2017/2/4.
 */
public class LoginServer extends BaseApp {
	private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);
	private static LoginServer instance = new LoginServer();

	private LoginServer() {

	}


	@Override
	protected void beforeStart() {

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
		List<MapObject> userList = DataQueryResult.load("conf_channel_switch", new HashMap<>());
		for (MapObject data : userList) {
			int packageId = data.getInt("package_id");
			int channelId = data.getInt("id");
			String platfromId = data.getString("platform_id");
			package2plat.put(packageId, platfromId);
			package2channel.put(packageId, channelId);
		}
	}

	@Override
	protected void initServer() {
		initDataBase();
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
