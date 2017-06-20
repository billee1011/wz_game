package handler.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by think on 2017/6/14.
 */
public class RmiClientSession {
    private int id;
    private int appId;
    private String appName;
    private ChannelHandlerContext ctx;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return ctx.writeAndFlush(msg);
    }

    public static RmiClientSession createRmiClientSession(int appId, int serverId, ChannelHandlerContext ctx) {
        RmiClientSession session = new RmiClientSession();
        session.appId = appId;
        session.id = serverId;
        session.ctx = ctx;
        return session;
    }
}
