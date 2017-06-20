package data;

import io.netty.buffer.ByteBuf;

/**
 * Created by wangfang on 2017/5/22.
 */
public interface IData {
	void read(ByteBuf buf);

	ByteBuf write();
}
