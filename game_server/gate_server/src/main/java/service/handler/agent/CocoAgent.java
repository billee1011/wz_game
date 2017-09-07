package service.handler.agent;

import io.netty.channel.ChannelHandlerContext;
import proto.creator.PacketCreator;

/**
 * Created by Administrator on 2017/2/7.
 */
public class CocoAgent {
	private int playerId;
	private ChannelHandlerContext ctx;
	private String sessionId;

	public CocoAgent(int playerId, ChannelHandlerContext ctx, String sessionId) {
		this.playerId = playerId;
		this.ctx = ctx;
		this.sessionId = sessionId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
