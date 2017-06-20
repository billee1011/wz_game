package service.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.handler.ClientMessage;
import trans_data.ProtobufData;

import java.util.List;

/**
 * Created by think on 2017/6/15.
 */
public class GateCodec extends ByteToMessageCodec<ProtobufData> {
    private static Logger logger = LoggerFactory.getLogger(GateCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtobufData msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMessageId());
        byte[] bytes = msg.toByteArray();
        int length = bytes.length;
        out.writeInt(length);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //这个位置不知道需要不需要decode以下， 或许需要， 或许不需要
        if (in.readableBytes() < 2) {
            logger.debug("invalid code of message ");
        }

        int code = readShortLittle(in);
        int packetLength = readIntLittle(in);
        int length = packetLength & 0X000FFFFF;
        logger.info("the pakcet length is " + length);
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        out.add(new ClientMessage(code, bytes));
    }

    short readShortLittle(ByteBuf buf) {
        byte[] bytes = new byte[2];
        buf.readBytes(bytes);
        return readShortLittleEndian(bytes);
    }

    int readIntLittle(ByteBuf buf) {
        byte[] bytes = new byte[4];
        buf.readBytes(bytes);
        return readIntLittleEndian(bytes);
    }

    short readShortLittleEndian(byte[] bytes) {
        return (short) (bytes[1] * 256 + bytes[0]);
    }

    int readIntLittleEndian(byte[] bytes) {
        return bytes[3] * 256 * 256 * 256
                + bytes[2] * 256 * 256 +
                bytes[1] * 256 + bytes[0];
    }
}
