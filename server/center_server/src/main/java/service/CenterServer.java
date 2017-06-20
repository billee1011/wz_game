package service;

import actor.CenterActorManager;
import config.provider.BaseProvider;
import dao.DBManager;
import define.AppId;
import network.facade.LoginFacade;
import network.facade.TestImpl;
import rmi.RmiServerConfig;
import rmi.config.DefaultRmiServerConfig;

import java.sql.SQLException;

/**
 * Created by Administrator on 2017/2/4.
 */
public class CenterServer extends ServerApp {

    private static CenterServer instance = new CenterServer();

    public static final int SAVE_INTERNAL = 20 * 60 * 1000;                            //mills

    private CenterServer() {

    }

    public static CenterServer getInst() {
        return instance;
    }


    @Override
    protected void registerAllRmiObject() {
        registerRmiObject(TestImpl.class);
        registerRmiObject(LoginFacade.class);
    }

    // donn't all the app will load data base
    @Override
    protected void initServerApp() {
        initStaticConfig();
//		initDataBase();
        CenterActorManager.getInstance().start();
    }

    public void initStaticConfig() {
//		BaseProvider.init();
//		BaseProvider.loadAll();
    }

    @Override
    protected void afterStart() {

    }

    @Override
    protected boolean needConnectCenter() {
        return false;
    }


    @Override
    protected boolean needConnectDb() {
        return true;
    }

    @Override
    protected String getServerName() {
        return "center_server";
    }

    @Override
    protected RmiServerConfig getServerConfig() {
        return new DefaultRmiServerConfig(10065);
    }

    @Override
    protected AppId getAppId() {
        return AppId.CENTER;
    }

    public static void main(String[] args) {
        CenterServer.getInst().start();
    }

}
