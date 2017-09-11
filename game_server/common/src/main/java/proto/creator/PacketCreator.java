package proto.creator;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import packet.LittleEndianByteBufUtil;
import protocol.s2c.ResponseCode;

public class PacketCreator {


	public static ByteBuf create(ResponseCode code, MessageLite message) {
		ByteBuf buffer = Unpooled.buffer();
		int length = 4;
		if (message != null) {
			length += message.toByteArray().length;
		}
		buffer.writeShort(length);
		buffer.writeShort(code.getValue());
		if (message != null) {
			buffer.writeBytes(message.toByteArray());
		}
		return buffer;
	}

	public static ByteBuf create(int code, byte[] bytes) {
		ByteBuf buffer = Unpooled.buffer();
		int length = 4;
		if (bytes != null) {
			length += bytes.length;
		}
		LittleEndianByteBufUtil.writeShort(buffer, length);
		LittleEndianByteBufUtil.writeShort(buffer, code);
		if (bytes != null) {
			buffer.writeBytes(bytes);
		}
		return buffer;
	}


}
