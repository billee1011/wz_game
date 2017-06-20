package handler;

import io.netty.buffer.ByteBuf;

/**
 * Created by think on 2017/4/17.
 */
public interface RmiMessage {
	int getMessageType();
	//how to make uuid in the rmi system

	int getPacketLength();

	void write();

	int getSequenceId();

	ByteBuf toPacket();

	void read(ByteBuf buf);
}
