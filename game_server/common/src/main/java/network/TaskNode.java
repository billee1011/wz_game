package network;

import actor.ICallback;
import io.netty.channel.ChannelHandlerContext;
import packet.CocoPacket;

/**
 * Created by Administrator on 2017/2/6.
 */
public class TaskNode {
	private long sendTime;
	private int seqId;
	private ChannelHandlerContext ioSession;
	private CocoPacket packet;
	private boolean isSendOk;
	private ICallback callback;

	public TaskNode(ChannelHandlerContext ioSession, CocoPacket packet, ICallback callback) {
		this.ioSession = ioSession;
		this.packet = packet;
		this.callback = callback;
		this.sendTime = System.currentTimeMillis();
		this.seqId = packet.getSeqId();
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public ChannelHandlerContext getIoSession() {
		return ioSession;
	}

	public void setIoSession(ChannelHandlerContext ioSession) {
		this.ioSession = ioSession;
	}

	public CocoPacket getPacket() {
		return packet;
	}

	public void setPacket(CocoPacket packet) {
		this.packet = packet;
	}

	public boolean isSendOk() {
		return isSendOk;
	}

	public void setSendOk(boolean sendOk) {
		isSendOk = sendOk;
	}

	public ICallback getCallback() {
		return callback;
	}

	public void setCallback(ICallback callback) {
		this.callback = callback;
	}
}
