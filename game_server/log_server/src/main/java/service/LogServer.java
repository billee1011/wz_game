package service;

import java.sql.SQLException;

import acotr.LogActorManager;
import actor.IActor;
import actor.IActorManager;
import database.DBManager;
import define.AppId;
import network.AbstractHandlers;
import service.handler.LogHandler;
import timer.ActTimer;

/**
 * Created by think on 2017/3/21.
 */
public class LogServer extends BaseApp {

	private static LogServer instance = new LogServer();

	private LogServer() {

	}

	public static LogServer getInst() {
		return instance;
	}

	@Override
	protected void initServer() {
		initDataBase();
		LogActorManager.getInst().start();
	}

	@Override
	protected void afterStart() {

	}

	@Override
	protected void beforeStart() {

	}

	private void initDataBase() {
		DBManager.setProps(props);
		DBManager.setDefaultDatabase("yc_log");
		try {
			DBManager.touch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected AbstractHandlers getClientHandler() {
		return new LogHandler();
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOG;
	}

	@Override
	protected String getServerName() {
		return "log_server";
	}

	@Override
	protected IActor getCrontabActor() {
		return LogActorManager.getActor();
	}

	@Override
	protected ActTimer getCrontabTimer() {
		return LogActorManager.getTimer();
	}

	@Override
	protected IActorManager getActorManager() {
		return LogActorManager.getInst();
	}

	public static void main(String[] args) {
		LogServer.getInst().start();
	}

	@Override
	public void stop() {
		System.exit(0);
	}
}
