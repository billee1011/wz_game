package service;

import java.io.File;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.IActor;
import actor.IActorManager;
import actor.LogicActorManager;
import config.provider.BaseProvider;
import database.DBManager;
import define.AppId;
import logic.DeskMgr;
import logic.majiong.cpstragety.StragetyManager;
import logic.majiong.xnStragety.XnStragetyManager;
import network.AbstractHandlers;
import service.handler.LogicHandler;
import timer.ActTimer;
import util.XProperties;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LogicApp extends BaseApp {
	private static LogicApp instance = new LogicApp();

	private static Logger logger = LoggerFactory.getLogger(LogicApp.class);
	
	/**
	 * 是否开启客户端排牌
	 */
	private boolean isArrayPai = false;
	
	/**
	 * 是否開啓牌型番數檢測
	 */
	private boolean isCheckFanType = false;

	private LogicApp() {

	}

	public static LogicApp getInst() {
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
	
	public void loadDebugProps() {
		XProperties debug = new XProperties();
        debug.load(getConfDir() + "debug.properties");
        isArrayPai = debug.getBoolean("isArrayPai", false);
        isCheckFanType = debug.getBoolean("isCheckFanType", false);
	}
	
	@Override
	protected void initServer() {
		initDataBase();
//		loadDebugProps();
		BaseProvider.init();
		BaseProvider.loadAll();
		LogicActorManager.getInstance().start();
	}

	@Override
	protected void afterStart() {
		File reloadFile = new File("reload");
		while (true) {
			try {
				Thread.sleep(3 * 1000);
//				Thread.sleep(1 * 1000);
			} catch (Exception e) {
				// do nothing
			}
			checkReloadFile(reloadFile);
		}
	}

	private static void checkReloadFile(File reloadFile) {
		if (reloadFile != null && reloadFile.exists()) {
			try {
				reloadFile.delete();
				LogicApp.getInst().loadDebugProps();
			} catch (Exception e) {
				// ignore
				logger.error("", e);
			}
		}
	}

	@Override
	protected AbstractHandlers getClientHandler() {
		return new LogicHandler();
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOGIC;
	}

	@Override
	protected String getServerName() {
		return "logic_server";
	}

	public static void main(String[] args) {
		LogicApp.getInst().start();
	}

	@Override
	protected IActor getCrontabActor() {
		return LogicActorManager.getLogicActor();
	}

	@Override
	protected ActTimer getCrontabTimer() {
		return LogicActorManager.getTimer();
	}

	@Override
	protected IActorManager getActorManager() {
		return LogicActorManager.getInstance();
	}

	public boolean isArrayPai() {
		return isArrayPai;
	}

	public void setArrayPai(boolean isArrayPai) {
		this.isArrayPai = isArrayPai;
	}

	public boolean isCheckFanType() {
		return isCheckFanType;
	}

	public void setCheckFanType(boolean isCheckFanType) {
		this.isCheckFanType = isCheckFanType;
	}

	@Override
	public void stop() {
		  logger.info("服务器正在关闭....");
		  DeskMgr.getInst().onStopServer();
		  System.exit(0);
		  logger.info("服务器正在成功....");
	}
	
}
