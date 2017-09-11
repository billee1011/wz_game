package packet;

import io.netty.buffer.ByteBuf;

/**
 * Created by think on 2017/9/7.
 */
public class LittleEndianByteBufUtil {

	public static short readShort(ByteBuf buf) {
		byte a = buf.readByte();
		byte b = buf.readByte();
		return (short) ((b << 8) + a);
	}

	public static void writeShort(ByteBuf buf, int value) {
		byte a = (byte) value;
		byte b = (byte) (value >>> 8);
		buf.writeByte(a);
		buf.writeByte(b);
	}


	public static int readIndex(ByteBuf buf) {
		return readShort(buf);
	}

	public static int readStatus(ByteBuf buf) {
		return buf.readByte();
	}

	public static int readInt(ByteBuf buf) {
		int a = buf.readByte();
		int b = buf.readByte();
		int c = buf.readByte();
		int d = buf.readByte();
		return (d << 24) + (c << 16) + (b << 8) + a;
	}

}
