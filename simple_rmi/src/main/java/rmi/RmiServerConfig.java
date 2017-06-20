package rmi;

/**
 * Created by think on 2017/4/14.
 */
public interface RmiServerConfig {
	int getPort();
	//因为是socket连接， 所以还是要有心跳的机智
	int getTimeoutSeconds();

	// to do ,   first we complete a simple rmi  config
}
