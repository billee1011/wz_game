package handler;

import handler.client.RmiResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import rmi.RmiServer;

/**
 * Created by think on 2017/4/17.
 */
public class RmiServerHandler extends ChannelInboundHandlerAdapter {

    private RmiServer server;

    public RmiServerHandler(RmiServer server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //这个时候还没有注册
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (false == msg instanceof RmiMessage) {
            System.out.println(" error message ");
        } else {
            if (msg instanceof RmiResponse) {
                System.out.println(" server handler  rmi response ");
                server.response((RmiResponse) msg);
            } else {
                System.out.println(" server handler  rmi request ");
                RmiRequest request = (RmiRequest) msg;                  //response 其实客户端也会直接给服务器发送消息的
                if (request.getMethodName().equals("registerRmiClient")) {
                    int appId = (int) request.getParamsMap().get(0).getV();
                    int serverId = (int) request.getParamsMap().get(1).getV();
                    server.registerClient(ctx, appId, serverId);
                } else {
                    server.executeServerMethod(ctx, (RmiRequest) msg);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
