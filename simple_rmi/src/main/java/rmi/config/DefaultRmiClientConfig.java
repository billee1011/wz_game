package rmi.config;

/**
 * Created by think on 2017/6/14.
 */
public class DefaultRmiClientConfig implements RmiClientConfig {
    private String host;
    private int port;
    private int appId;
    private int serverId;

    public DefaultRmiClientConfig(String host, int port, int appId, int serverId) {
        this.host = host;
        this.port = port;
        this.appId = appId;
        this.serverId = serverId;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public int getAppId() {
        return this.appId;
    }

    @Override
    public int getServerId() {
        return this.serverId;
    }
}
