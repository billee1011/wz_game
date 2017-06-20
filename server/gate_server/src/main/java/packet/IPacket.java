package packet;

import io.netty.buffer.ByteBuf;

/**
 * Created by Administrator on 2017/2/4.
 */
public interface IPacket {
	public MessageHeader getMessageHeader();

	public void readPacket(ByteBuf buffer);

	public ByteBuf writePacket();
}
