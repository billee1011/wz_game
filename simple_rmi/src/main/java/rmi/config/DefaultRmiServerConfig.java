package rmi.config;

import rmi.RmiServerConfig;

/**
 * Created by think on 2017/6/20.
 */
public class DefaultRmiServerConfig implements RmiServerConfig {
    private int port;

    private int heartBeatTime;

    public DefaultRmiServerConfig(int port) {
        this.port = port;
        this.heartBeatTime = 30000;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getTimeoutSeconds() {
        return heartBeatTime;
    }
}
