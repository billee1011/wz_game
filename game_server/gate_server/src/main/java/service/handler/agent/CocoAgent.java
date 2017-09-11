package service.handler.agent;

import io.netty.channel.ChannelHandlerContext;
import proto.creator.PacketCreator;

/**
 * Created by Administrator on 2017/2/7.
 */
public class CocoAgent {
	private long playerId;
	private ChannelHandlerContext ctx;
	private int userId;
	private boolean valid;

	public CocoAgent(long playerId, ChannelHandlerContext ctx, int userId) {
		this.playerId = playerId;
		this.ctx = ctx;
		this.userId = userId;
		this.valid = false;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public void writeMessage(int code, byte[] bytes) {
		ctx.writeAndFlush(PacketCreator.create(code, bytes));
	}

	public void closeAgent() {
		ctx.close();
	}

}
