package rmi.config;

/**
 * Created by think on 2017/6/14.
 */
public interface RmiClientConfig {
    String getHost();

    int getPort();

    int getAppId();

    int getServerId();
}
