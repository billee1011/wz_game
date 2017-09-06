package network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import packet.CocoPacket;

/**
 * Created by Administrator on 2017/2/7.
 */
public class MessageEncoder extends MessageToByteEncoder<CocoPacket> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, CocoPacket packet, ByteBuf byteBuf) throws Exception {
		byteBuf.writeBytes(packet.writePacket());
	}
}
