package net.response;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.IMessage;
import net.ITargetServerMessage;

/**
 * Created by WZ on 2016/8/25.
 */
public class ResponseMessage implements ITargetServerMessage {
	private short responseCode;
	private int seqId;
	private byte serverId;
	private byte[] message;

	private ResponseMessage(short responseCode, int seqId, byte serverId, byte[] message) {
		this.responseCode = responseCode;
		this.message = message;
		this.seqId = seqId;
		this.serverId = serverId;
	}

	@Override
	public ByteBuf getBuffer() {
		ByteBuf buffer = Unpooled.buffer();
		int length = 7 + (message == null ? 0 : message.length);
		buffer.writeInt(length);
		buffer.writeInt(seqId);
		buffer.writeByte(serverId);
		buffer.writeShort(responseCode);
		if (message != null) {
			buffer.writeBytes(message);
		}
		return buffer;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	@Override
	public byte getServerId() {
		return serverId;
	}

	public void setServerId(byte serverId) {
		this.serverId = serverId;
	}

	public short getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(short responseCode) {
		this.responseCode = responseCode;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public static ResponseMessage createResponseMessage(short responseCode, int seqId, byte serverId, MessageLite message) {
		return new ResponseMessage(responseCode, seqId, serverId, message == null ? null : message.toByteArray());
	}

	public static ResponseMessage createResponseMessage(short responseCode, int seqId, byte serverId,byte[] bytes) {
		return new ResponseMessage(responseCode, seqId, serverId, bytes);
	}

}
