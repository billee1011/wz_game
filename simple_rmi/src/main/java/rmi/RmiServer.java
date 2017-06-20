package rmi;

import handler.*;
import handler.client.RmiClient;
import handler.client.RmiClientSession;
import handler.client.RmiResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import proxy.WzCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by think on 2017/4/14.
 */
public class RmiServer extends RemoteService {
    RmiServerConfig config;

    private Map<Integer, RmiClientSession> clientSessions;


    private ServerBootstrap boot;

    public RmiServer() {
        clientSessions = new HashMap<>();
    }

    public <T> T getServerObject(String objectName) throws Exception {
        return (T) registerObject.get(objectName);
    }

    //注册的时候最好用密钥验证一下， 这样安全性会高一点
    public void registerClient(ChannelHandlerContext ctx, int appId, int serverId) {
        System.out.println("register rmi client session ");
        RmiClientSession session = RmiClientSession.createRmiClientSession(appId, serverId, ctx);
        clientSessions.put(appId * 10000 + serverId, session);
    }

    class ClientMethodInterceptor implements MethodInterceptor {
        private ChannelHandlerContext session;

        public ClientMethodInterceptor(ChannelHandlerContext session) {
            this.session = session;
        }

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
            if (callbackList.size() == 0) {
                return invokeSyncMethod(session, message);
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

    Object invokeSyncMethod(ChannelHandlerContext session, RmiMessage message) {
        response = new SyncResponse();
        session.writeAndFlush(message);
        response.blockUntilRelease();
        return response.getResult();
    }

    @Override
    public void response(RmiResponse response) {
        this.response.setResult(response.getResult());
    }

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

    //to do  cache the  method interceptor
    public <T> T getClientInterface(int appId, int serverId, Class<T> classType) {
        //这个时候也用代理方法处理一下就行了， session中应该要记住客户端的host and port
        RmiClientSession clientSession = clientSessions.get(appId * 10000 + serverId);
        if (clientSession == null) {
            return null;
        }
        ClientMethodInterceptor methodInterceptor = new ClientMethodInterceptor(clientSession.getCtx());
        return methodInterceptor.getInterface(classType);
    }


    public void serve() {
        if (config == null) {
            throw new RuntimeException(" you need support rmi server config when you want start rmi server");
        }
        boot = new ServerBootstrap();
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup();
        boot.group(parentGroup, childGroup);
        boot.localAddress(config.getPort());
        boot.channel(NioServerSocketChannel.class);
        boot.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2));
                channel.pipeline().addLast(new RmiMessageCodec()); //只接受rmi提供的协议 其他的都不处理
                channel.pipeline().addLast(new RmiServerHandler(RmiServer.this));
            }
        });
        boot.childOption(ChannelOption.SO_KEEPALIVE, true);
        boot.option(ChannelOption.SO_KEEPALIVE, true);
        boot.option(ChannelOption.SO_BACKLOG, 128);
        try {
            boot.bind().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //todo  because it's end with open source
    public RmiServerConfig getConfig() {
        return config;
    }

    public void setConfig(RmiServerConfig config) {
        this.config = config;
    }
}
