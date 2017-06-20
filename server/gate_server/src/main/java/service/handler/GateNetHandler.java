package service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.handler.agent.CocoAgent;
import util.NettyUtil;

/**
 * Created by think on 2017/6/15.
 */
public class GateNetHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GateNetHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        CocoAgent agent = new CocoAgent(0, ctx);
        NettyUtil.setAttribute(ctx, CocoAgent.KEY, agent);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (false == msg instanceof ClientMessage) {
            logger.warn(" unknow type of message in gate net handler");
            return;
        }
        GateMessageManager.getInst().processClientMessage(NettyUtil.getAttribute(ctx, CocoAgent.KEY), (ClientMessage) msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
