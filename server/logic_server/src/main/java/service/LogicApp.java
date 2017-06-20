package service;

import actor.LogicActorManager;
import conf.BaseProvider;
import define.AppId;

import java.io.File;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LogicApp extends BaseApp {
    private static LogicApp instance = new LogicApp();

    private LogicApp() {

    }

    public static LogicApp getInst() {
        return instance;
    }

    @Override
    protected void initServer() {
        BaseProvider.CONF_PATH = System.getProperty("user.dir") + File.separator + "logic_server" + File.separator + "conf" + File.separator;


        String providerPackage = props.getProperty("provider.package");
        BaseProvider.PROVIDER_PACKAGE = providerPackage;
        String providerPath = providerPackage.replace(".", File.separator);
        BaseProvider.PROVIDER_PATH = getServerName() + File.separator + "src" + File.separator
                + "main" + File.separator + "java" + File.separator + providerPath;
        BaseProvider.loadAll();
        LogicActorManager.getInstance().start();
    }

    @Override
    protected void afterStart() {
        System.out.println("hello world ");
    }


    @Override
    protected boolean needConnectDb() {
        return true;
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
}
