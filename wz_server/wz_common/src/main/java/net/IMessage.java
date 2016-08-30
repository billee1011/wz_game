package net;

import io.netty.buffer.ByteBuf;

/**
 * Created by WZ on 2016/8/27.
 */
public interface IMessage {
	public ByteBuf getBuffer();
}
