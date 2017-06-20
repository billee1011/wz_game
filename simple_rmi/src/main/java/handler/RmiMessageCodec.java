package handler;

import handler.client.RmiResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Created by think on 2017/4/17.
 */
public class RmiMessageCodec extends ByteToMessageCodec<RmiMessage> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, RmiMessage rmiMessage, ByteBuf byteBuf) throws Exception {
		System.out.println("encode packet to buffer");
		ByteBuf buf = rmiMessage.toPacket();
		System.out.println("encode success");
		byteBuf.writeBytes(buf);
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		int length = byteBuf.readShort();
		byteBuf.markReaderIndex();
		int messageType = byteBuf.readInt();
		RmiMessage message = null;
		byteBuf.resetReaderIndex();
		if (messageType == 1) {
			message = new RmiRequest();
		} else if (messageType == 2) {
			message = new RmiResponse();
		} else if (messageType == 3) {
			message = new RmiCallbackMessage();
		} else {
			throw new RuntimeException("unknow type of rmi message");
		}
		message.read(byteBuf);
		list.add(message);
	}
}
