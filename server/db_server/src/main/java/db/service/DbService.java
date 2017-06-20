package db.service;

import dao.DBManager;
import db.facade.DbFacade;
import define.AppId;
import rmi.RmiServerConfig;
import rmi.config.DefaultRmiServerConfig;
import service.BaseApp;
import service.ServerApp;

import java.sql.SQLException;

/**
 * Created by wangfang on 2017/5/22.
 * all the game db operation will in this app
 */
public class DbService extends ServerApp {
    private static DbService instance = new DbService();

    private DbService() {

    }

    public static DbService getInst() {
        return instance;
    }

    @Override
    protected void afterStart() {
        initDataBase();
    }

    @Override
    protected AppId getAppId() {
        return AppId.DATABASE;
    }


    @Override
    protected RmiServerConfig getServerConfig() {
        return new DefaultRmiServerConfig(10066);
    }


    @Override
    protected boolean needConnectCenter() {
        return false;
    }

    private void initDataBase() {
        DBManager.setProps(props);
        DBManager.setDefaultDatabase("wz_game_dev");
        try {
            DBManager.touch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initServerApp() {

    }

    @Override
    protected void registerAllRmiObject() {
        registerRmiObject(DbFacade.class);
    }

    @Override
    protected String getServerName() {
        return "db_server";
    }


    public static void main(String[] args) {
        DbService.getInst().start();
    }
}
