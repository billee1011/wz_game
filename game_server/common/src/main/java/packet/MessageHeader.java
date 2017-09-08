package packet;

import io.netty.buffer.ByteBuf;

/**
 * Created by Administrator on 2017/2/4.
 */
public class MessageHeader {
    public static final int LENGTH = 4 + 4 + 8 + 8;
    private int seqId;
    private int reqId;
    private long playerId;
    private short packetLength;
    private long sendTime;

    public MessageHeader() {
    }

    public MessageHeader(int seqId, int reqId, long sendTime, int length, long playerId) {
        this.seqId = seqId;
        this.reqId = reqId;
        this.sendTime = sendTime;
        this.playerId = playerId;
        this.packetLength = (short) length;
    }

    public short getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(short packetLength) {
        this.packetLength = packetLength;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public int getReqId() {
        return reqId;
    }

    public void setReqId(int reqId) {
        this.reqId = reqId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public void readHead(ByteBuf buffer) {
        playerId = buffer.readLong();
        seqId = buffer.readInt();
        reqId = buffer.readInt();
        sendTime = buffer.readLong();
    }

    public void writeHead(ByteBuf buffer) {
        buffer.writeShort(packetLength);
        buffer.writeLong(playerId);
        buffer.writeInt(seqId);
        buffer.writeInt(reqId);
        buffer.writeLong(sendTime);
    }
}
