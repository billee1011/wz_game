package service;

import define.AppId;
import facade.ITest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rmi.RmiServerConfig;
import service.handler.network.GateNetServer;
import util.Randomizer;

/**
 * Created by Administrator on 2017/2/6.
 */
public class GateApp extends BaseApp {
    private static final Logger logger = LoggerFactory.getLogger(GateApp.class);
    private GateNetServer netServer;

    private static GateApp instance = new GateApp();

    private GateApp() {
    }

    public static GateApp getInst() {
        return instance;
    }

    @Override
    protected void initServer() {
        int port = Randomizer.nextInt(10000) + 10000;

        new GateNetServer(10086).start();
    }

    @Override
    protected String getServerName() {
        return "gate_server";
    }

    @Override
    protected void afterStart() {


    }



    @Override
    protected AppId getAppId() {
        return AppId.GATE;
    }

    public int getGatePort() {
        return netServer.getPort();
    }

    public static void main(String[] args) {
        GateApp.getInst().start();
    }

}
