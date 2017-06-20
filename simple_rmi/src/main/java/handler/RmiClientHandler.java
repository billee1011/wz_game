package handler;

import handler.client.RmiClient;
import handler.client.RmiResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by think on 2017/4/17.
 */
//哪里有彩虹告诉我， 能不能把我的还给我， 也许时间是一种煎熬， 也是我现在服下的解药
public class RmiClientHandler extends ChannelInboundHandlerAdapter {

    private RmiClient client;

    public RmiClientHandler(RmiClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client.setSession(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RmiResponse) {
            client.response((RmiResponse) msg);
        } else if (msg instanceof RmiCallbackMessage) {
            client.serverCallback((RmiCallbackMessage) msg);
        } else {
            //也可以接受request
            client.executeServerMethod(ctx, (RmiRequest) msg);
            System.out.println("error message type ");                        // is wz rmi system and you can do anything if you want
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
