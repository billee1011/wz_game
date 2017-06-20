package handler.client;

import handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import proxy.WzCallback;
import rmi.RemoteService;
import rmi.RmiServer;
import rmi.config.RmiClientConfig;
import rmi.facade.IRegister;
import sun.security.krb5.internal.NetClient;

import javax.security.auth.callback.Callback;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by think on 2017/4/17.
 */
public class RmiClient extends RemoteService {
    //客户端需要 连接到服务器

    Bootstrap boot = null;

    RmiClientConfig config;

    private ChannelHandlerContext session;

    private Map<Class<?>, Object> objectCache;

    public RmiClient() {
        objectCache = new HashMap<>();
    }


    public void setConfig(RmiClientConfig config) {
        this.config = config;
    }


    @Override
    public void response(RmiResponse response) {
        this.response.setResult(response.getResult());
    }

    public void serverCallback(RmiCallbackMessage message) {
        List<Object> argsList = message.getArgsList();
        for (int i = 0, size = argsList.size(); i < size; i++) {
            WzCallback callback = (WzCallback) callbackList.get(i);
            callback.onResult(argsList.get(0));                        //to do  now the message just support just one callback param					主要是实用， 所以还是会不断的调整
        }
    }

    public <T> T getInterface(Class<T> classType) {
        Object result = objectCache.get(classType);
        if (result != null) {
            return (T) result;
        }
        result = methodInterceptor.getInterface(classType);
        objectCache.put(classType, result);
        return (T) result;
    }


    ClientProxy proxyObject = new ClientProxy();

    class SyncResponse {
        private Object result;

        private Semaphore semaphore = new Semaphore(0);

        public void blockUntilRelease() {
            semaphore.acquireUninterruptibly();
        }

        public void setResult(Object result) {
            this.result = result;
            release();
        }

        public void release() {
            semaphore.release();
        }

        public Object getResult() {
            return result;
        }
    }

    SyncResponse response;

    List<Object> callbackList;


    class ClientMethodInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            String name = o.getClass().getInterfaces()[0].getSimpleName();
            String methodName = method.getName();
            int length = objects == null ? 0 : objects.length;
            String returnType = method.getReturnType().getName();
            List<Pair<String, Object>> paramMap = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                paramMap.add(new Pair(objects[i].getClass().getSimpleName(), objects[i]));
            }
            System.out.println(String.format("name %s  method name %s args length %d return type %s", name, methodName, length, returnType));            //log something
            //call back result you can call back any count of function
            callbackList = new ArrayList<>();
            paramMap.forEach(e -> {
                if (e.getK().equals("")) {
                    callbackList.add(e.getV());
                }
            });
            RmiMessage message = new RmiRequest(name, methodName, paramMap);
            if (callbackList.size() == 0 && !method.getName().equals("registerRmiClient")) {
                return invokeSyncMethod(message);
            } else {
                session.writeAndFlush(message);
                return Void.class;
            }
        }

        public <T> T getInterface(Class<T> classType) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(classType);
            enhancer.setCallback(this);
            return (T) enhancer.create();
        }


    }


    ClientMethodInterceptor methodInterceptor = new ClientMethodInterceptor();

    class ClientProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    Object invokeSyncMethod(RmiMessage message) {
        response = new SyncResponse();
        session.writeAndFlush(message);
        response.blockUntilRelease();
        return response.getResult();
    }

    public void setSession(ChannelHandlerContext session) {
        this.session = session;
    }

    public void connect() {
        if (config == null) {
            throw new RuntimeException("init failed");
        }
        boot = new Bootstrap();
        boot.group(new NioEventLoopGroup());
        boot.channel(NioSocketChannel.class);
        boot.option(ChannelOption.SO_KEEPALIVE, true);
        boot.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2));
                socketChannel.pipeline().addLast(new RmiMessageCodec());
                socketChannel.pipeline().addLast(new RmiClientHandler(RmiClient.this));
            }
        });
        try {
            ChannelFuture future = boot.connect(config.getHost(), config.getPort()).sync();
            future.addListener(e -> {
                if (e.isSuccess()) {
                    System.out.println("connect success ");
                    IRegister register = getInterface(IRegister.class);
                    register.registerRmiClient(config.getAppId(), config.getServerId());
                } else {
                    System.out.println("connect failed");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //连接之后注册appid  与server id
    }
}
