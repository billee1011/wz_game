package proxy;

import handler.client.RmiClient;
import rmi.RmiServer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/4/13.
 */
public class ServerTest {

	public static void main(String[] args) {
		RmiServer server = new RmiServer();
		server.registerObject(RealObject.class);
		server.registerObject(FuckYouIml.class);
		server.serve();
	}
}
