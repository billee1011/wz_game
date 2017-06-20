package packet;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import protocol.c2s.RequestCode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/2/4.
 */
public class CocoPacket implements IPacket {
	private static AtomicInteger SEQ_GENE = new AtomicInteger(1);
	private static final int REQUEST_FLAG = 0x10000000;
	private MessageHeader header;
	private byte[] bytes;

	public CocoPacket() {
	}

	public CocoPacket(RequestCode code, MessageLite message, int playerId) {
		this(code.getValue(), message == null ? null : message.toByteArray(), playerId);
	}

	public CocoPacket(RequestCode code, MessageLite message) {
		this(code.getValue(), message);
	}

	public CocoPacket(int reqId, byte[] bytes, int playerId) {
		this.bytes = bytes;
		header = new MessageHeader(SEQ_GENE.getAndIncrement(), reqId, System.currentTimeMillis(), getPacketLength(), playerId);
	}

	public CocoPacket(int reqId, byte[] bytes) {
		this(reqId, bytes, 0);
	}

	public CocoPacket(int reqId, MessageLite message) {
		this(reqId, message == null ? null : message.toByteArray());
	}

	@Override
	public void readPacket(ByteBuf buffer) {
		header = new MessageHeader();
		header.readHead(buffer);
		int remainLength = buffer.readableBytes();
		bytes = new byte[remainLength];
		buffer.readBytes(bytes);
	}

	@Override
	public ByteBuf writePacket() {
		ByteBuf buffer = Unpooled.buffer();
		header.writeHead(buffer);
		if (bytes != null) {
			buffer.writeBytes(bytes);
		}
		return buffer;
	}

	public void setSeqId(int seqId) {
		header.setSeqId(seqId);
	}

	public void setReqId(int reqId) {
		header.setReqId(reqId);
	}

	public void resetReqIdToRequest() {
		if (header.getReqId() < REQUEST_FLAG) {
			header.setReqId(getReqId() + REQUEST_FLAG);
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getSeqId() {
		return header.getSeqId();
	}

	public int getPlayerId() {
		return header.getPlayerId();
	}

	public void setPlayerId(int playerId) {
		header.setPlayerId(playerId);
	}

	public int getReqId() {
		return header.getReqId();
	}

	public RequestCode getReqCode() {
		return RequestCode.getByValue(header.getReqId());
	}

	private int getPacketLength() {
		return MessageHeader.LENGTH + (bytes == null ? 0 : bytes.length);
	}

	public void setPacketLength(short packetLength) {
		header.setPacketLength(packetLength);
	}

	@Override
	public MessageHeader getMessageHeader() {
		return header;
	}
}
