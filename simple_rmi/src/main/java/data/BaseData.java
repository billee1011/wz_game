package data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import util.DataUtil;

import java.lang.reflect.Field;

/**
 * Created by wangfang on 2017/5/22.
 */
public class BaseData implements IData {
	@Override
	public void read(ByteBuf buf) {
		Field[] fields = this.getClass().getDeclaredFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			fields[i].setAccessible(true);
			try {
				fields[i].set(this, DataUtil.readObject(buf));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ByteBuf write() {
		ByteBuf buf = Unpooled.buffer();
		DataUtil.writeUtf8(buf, getClass().getName());
		Field[] fields = this.getClass().getDeclaredFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			fields[i].setAccessible(true);
			try {
				DataUtil.writeObject(buf, fields[i].get(this));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return buf;
	}
}
