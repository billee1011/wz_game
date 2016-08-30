package net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import net.request.RequestMessage;
import net.response.ResponseMessage;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.util.List;

/**
 * Created by WZ on 2016/8/25.
 */
public class GameMessageDecoder extends ByteToMessageDecoder {
	private static final String DECODER_STATE_KEY = "DECODE_STATE";

	private static class DecoderState {
		public int length = -1;
	}


	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		AttributeKey<DecoderState> key = AttributeKey.valueOf(DECODER_STATE_KEY);
		DecoderState state = ctx.channel().attr(key).get();
		if (state == null) {
			state = new DecoderState();
			ctx.channel().attr(key).set(state);
		}
		if (in.readableBytes() < 4 && state.length == -1) {
			return;
		}
		if (state.length != -1) {
			if (in.readableBytes() < state.length) {
				return;
			}
		} else {
			state.length = in.readInt();
			if (in.readableBytes() < state.length) {
				return;
			}
		}
		int seqId = in.readInt();
		if (seqId > 0x10000000) {
			int byteLength = state.length - 8;
			byte[] bytes = new byte[byteLength];
			byte srcServer = in.readByte(), moduleId = in.readByte(), actionId = in.readByte(), serverId = in.readByte();
			in.readBytes(bytes, 0, byteLength);
			RequestMessage message = RequestMessage.createRequestMessage(srcServer, moduleId, actionId, serverId, bytes);
			message.setSeqId(seqId);
			out.add(message);
		} else {
			int byteLength = state.length - 7;
			byte[] bytes = new byte[byteLength];
			short resId = in.readShort();
			byte serverId = in.readByte();
			in.readBytes(bytes, in.readerIndex(), byteLength);
			ResponseMessage message = ResponseMessage.createResponseMessage(resId, seqId, serverId, bytes);
			message.setSeqId(seqId);
			out.add(message);
		}
		state.length = -1;
	}
}
