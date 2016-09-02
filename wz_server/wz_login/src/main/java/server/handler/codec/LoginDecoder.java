package server.handler.codec;

import client.LoginClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import util.NettyUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class LoginDecoder extends ByteToMessageDecoder {
	private static final String DECODER_STATE_KEY = "DECODE_STATE";

	private static class DecoderState {
		public int length = -1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
		DecoderState client = NettyUtil.getAttribute(ctx, DECODER_STATE_KEY);
		if (client == null) {
			client = new DecoderState();
			NettyUtil.setAttribute(ctx, DECODER_STATE_KEY, client);
		}
		if (client.length == -1) {
			if (in.readableBytes() < 4) {
				return;
			}
			int length = in.readInt();
			client.length = length;
			if (length > in.readableBytes()) {
				return;
			}
			ByteBuf out = Unpooled.buffer();
			out.writeBytes(in , in.readableBytes());
			list.add(out);
		} else {
			if (client.length > in.readableBytes()) {
				return;
			}
			ByteBuf out = Unpooled.buffer();
			out.writeBytes(in, in.readableBytes());
			list.add(out);
		}
	}
}
