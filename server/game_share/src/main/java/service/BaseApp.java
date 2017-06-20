package service;


import define.AppId;
import handler.client.RmiClient;
import rmi.config.DefaultRmiClientConfig;
import util.XProperties;

import java.io.File;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */
public abstract class BaseApp implements Service {

    protected XProperties props;

    protected abstract void initServer();

    protected abstract void afterStart();

    protected abstract AppId getAppId();

    protected RmiClient client;

    protected RmiClient dbClient;

    protected boolean needConnectDb() {
        return false;
    }

    protected boolean needConnectCenter() {
        return true;
    }

    //all properties will load from  config service
    private void initServerProps() {
        props = new XProperties();
        props.load(getServerName() + File.separator + "conf" + File.separator + "server.properties");
    }

    protected abstract String getServerName();

    protected void registerToCenterServer() {
        String host = props.getString("center.host");
        int port = props.getInteger("center.port", 10065);
        client = new RmiClient();
        client.setConfig(new DefaultRmiClientConfig(host, port, getAppId().getId(), 1));
        client.connect();
    }

    protected void registerToDbServer() {
        String host = props.getString("db.host");
        int port = props.getInteger("db.port", 10066);
        dbClient = new RmiClient();
        dbClient.setConfig(new DefaultRmiClientConfig(host, port, getAppId().getId(), 1));
        dbClient.connect();
    }


    public void start() {
        initServerProps();
        if (needConnectCenter())
            registerToCenterServer();
        if (needConnectDb())
            registerToDbServer();
        initServer();
        afterStart();
    }


    public <T> T getFacade(Class<T> classType) {
        return getCenterRmiClient().getInterface(classType);
    }

    public <T> T getDBFacade(Class<T> classType) {
        return dbClient.getInterface(classType);
    }

    public RmiClient getCenterRmiClient() {
        return client;
    }

    protected void registerRmiClientObject(Class<?> classType) {
        client.registerObject(classType);
    }

    public void stop() {

    }
}
