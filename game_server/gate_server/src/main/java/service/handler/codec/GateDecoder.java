package service.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import network.codec.MessageDecoder;
import packet.CocoPacket;
import packet.LittleEndianByteBufUtil;
import service.handler.agent.CocoAgent;
import sun.management.Agent;
import util.NettyUtil;

import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */
public class GateDecoder extends ByteToMessageDecoder {
	private static final int REQUEST_FLAG = 0x10000000;
	private static final String DECODER_STATE_KEY = "DECODE_STATE";

	private static class DecoderState {
		public int length = -1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		GateDecoder.DecoderState state = NettyUtil.getAttribute(ctx, DECODER_STATE_KEY);
		if (state == null) {
			state = new GateDecoder.DecoderState();
			NettyUtil.setAttribute(ctx, DECODER_STATE_KEY, state);
		}
		if (in.readableBytes() < 2 && state.length == -1) {
			return;
		}
		if (state.length != -1) {
			if (in.readableBytes() < state.length) {
				return;
			}
		} else {
			state.length = LittleEndianByteBufUtil.readShort(in);
			if (in.readableBytes() < state.length) {
				return;
			}
		}
		ByteBuf obj = Unpooled.buffer();
		obj.writeBytes(in, state.length);
		int reqCode = LittleEndianByteBufUtil.readShort(obj);
		int length = obj.readableBytes();
		byte[] bytes = new byte[obj.readableBytes()];
		obj.readBytes(bytes, 0, length);
//		CocoAgent agent = NettyUtil.getAttribute(ctx, "agent");
//		if (agent == null) {
		out.add(new CocoPacket(reqCode + REQUEST_FLAG, bytes));
//		} else {
//			out.add(new CocoPacket(reqCode + REQUEST_FLAG, bytes, agent.getPlayerId()));
//		}
		state.length = -1;
	}


}
