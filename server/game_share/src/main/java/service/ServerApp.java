package service;

import rmi.RmiServer;
import rmi.RmiServerConfig;

/**
 * Created by think on 2017/5/18.
 */
public abstract class ServerApp extends BaseApp {
    private RmiServer rmiServer;

    @Override
    protected void initServer() {
        rmiServer = new RmiServer();
        rmiServer.setConfig(getServerConfig());
        rmiServer.serve();
        registerAllRmiObject();
        initServerApp();
    }

    public <T> T getClientFacade(int appId, int serverId, Class<T> classType) {
        return rmiServer.getClientInterface(appId, serverId, classType);
    }


    protected abstract RmiServerConfig getServerConfig();

    protected void registerRmiObject(Class<?> classType) {
        rmiServer.registerObject(classType);
    }

    protected abstract void initServerApp();

    protected abstract void registerAllRmiObject();
}
