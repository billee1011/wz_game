package handler;

import data.IData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import proxy.WzCallback;
import util.DataUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/17.
 */
public abstract class AbstractRmiMessage implements RmiMessage {
	protected int messageType;

	protected ByteBuf buffer;

	public AbstractRmiMessage() {
	}

	public AbstractRmiMessage(int messageType) {
		this.messageType = messageType;
		this.buffer = Unpooled.buffer();
	}

	@Override
	public int getMessageType() {
		return messageType;
	}

	@Override
	public void write() {
		buffer.writeInt(messageType);
	}

	@Override
	public ByteBuf toPacket() {
		buffer.writeShort(0);
		write();
		int length = buffer.readableBytes();
		buffer.markWriterIndex();
		buffer.writerIndex(0);
		buffer.writeShort(length - 2);
		buffer.resetWriterIndex();
		return buffer;
	}

	@Override
	public void read(ByteBuf buf) {
		messageType = buf.readInt();
	}

	@Override
	public int getPacketLength() {
		return buffer.readableBytes();
	}

	protected List<Pair<String, Object>> readMap() {
		int size = buffer.readShort();
		List<Pair<String, Object>> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(new Pair(DataUtil.readUtf8(buffer), DataUtil.readObject(buffer)));
		}
		return result;
	}


}
