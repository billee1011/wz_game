package service.handler.agent;

import io.netty.channel.ChannelHandlerContext;
import trans_data.ProtobufData;

/**
 * Created by Administrator on 2017/2/7.
 */
public class CocoAgent {
    public static final String KEY = "AGENT";
    private int playerId;
    private int userId;
    private ChannelHandlerContext ctx;

    public CocoAgent(int playerId, ChannelHandlerContext ctx) {
        this.playerId = playerId;
        this.ctx = ctx;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void writeMessage(ProtobufData data) {
        if (this.ctx != null) {
            ctx.write(data);
        }
    }

    public void closeAgent() {
        ctx.channel().close();
    }

}
