package service;

import java.io.File;
import java.sql.SQLException;

import chr.PlayerManager;
import db.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.IActor;
import actor.IActorManager;
import actor.LogicActorManager;
import config.provider.BaseProvider;
import database.DBManager;
import define.AppId;
import network.AbstractHandlers;
import service.handler.LogicHandler;
import timer.ActTimer;
import util.XProperties;

import javax.xml.ws.handler.LogicalHandler;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LogicApp extends BaseApp {
    private static LogicApp instance = new LogicApp();

    private static Logger logger = LoggerFactory.getLogger(LogicApp.class);

    private int serverId = 1;

    public static final int SAVE_INTERNAL = 2 * 60 * 1000;                            //mills


    private LogicApp() {

    }


    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
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
    }

    @Override
    protected void initServer() {
        initDataBase();
        BaseProvider.CONF_PATH = getConfDir();
        BaseProvider.init();
        BaseProvider.loadAll();
        LogicActorManager.getInstance().start();
        DataManager.getInst().start();
    }

    @Override
    protected void afterStart() {
        File reloadFile = new File("reload");
        while (true) {
            try {
                Thread.sleep(3 * 1000);
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
    protected void registerCronTask() {
        LogicActorManager.getDBTimer().register(SAVE_INTERNAL, SAVE_INTERNAL, () -> {
            PlayerManager.getInst().saveAllCharacter();
        }, LogicActorManager.getDBCheckActor(), "save_player");
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

    @Override
    public void stop() {
        logger.info("服务器正在关闭....");
        System.exit(0);
        logger.info("服务器正在成功....");
    }

}
