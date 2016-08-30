package net.request;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.ITargetServerMessage;

/**
 * Created by WZ on 2016/8/25.
 */
public class RequestMessage implements ITargetServerMessage {
	private byte moduleId;
	private byte actionId;
	private byte serverId;
	private byte srcServerId;
	private byte[] message;
	private int seqId;


	private RequestMessage(int srcServerId, byte moduleId, byte actionId, byte serverId, byte[] message) {
		this.moduleId = moduleId;
		this.actionId = actionId;
		this.message = message;
		this.serverId = serverId;
		this.srcServerId = serverId;
	}

	@Override
	public ByteBuf getBuffer() {
		ByteBuf buffer = Unpooled.buffer();
		int length = 8 + (message == null ? 0 : message.length);
		buffer.writeInt(length);
		buffer.writeInt(seqId);
		buffer.writeByte(moduleId);
		buffer.writeByte(actionId);
		buffer.writeByte(serverId);
		buffer.writeByte(srcServerId);
		if (message != null) {
			buffer.writeBytes(message);
		}
		return buffer;
	}

	public byte getSrcServerId() {
		return srcServerId;
	}

	public void setSrcServerId(byte srcServerId) {
		this.srcServerId = srcServerId;
	}

	@Override
	public byte getServerId() {
		return serverId;
	}

	public void setServerId(byte serverId) {
		this.serverId = serverId;
	}

	public byte getModuleId() {
		return moduleId;
	}

	public void setModuleId(byte moduleId) {
		this.moduleId = moduleId;
	}

	public byte getActionId() {
		return actionId;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public void setActionId(byte actionId) {
		this.actionId = actionId;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public static RequestMessage createRequestMessage(byte srcServerId, byte moduleId, byte actionId, byte serverId, MessageLite message) {
		return new RequestMessage(srcServerId, moduleId, actionId, serverId, message == null ? null : message.toByteArray());
	}

	public static RequestMessage createRequestMessage(byte srcServerId, byte moduleId, byte actionId, byte serverId, byte[] bytes) {
		return new RequestMessage(srcServerId, moduleId, actionId, serverId, bytes);
	}

	@Override
	public String toString() {
		return String.format("module id {} , action id {} , serverId  {}", moduleId, actionId, serverId);
	}
}
