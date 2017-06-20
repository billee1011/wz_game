package handler;

import io.netty.buffer.ByteBuf;
import util.DataUtil;

import java.util.List;

/**
 * Created by think on 2017/4/18.
 */
public class RmiCallbackMessage extends AbstractRmiMessage {
	List<Object> argsList;

	@Override
	public int getSequenceId() {
		return 0;
	}

	public RmiCallbackMessage() {
		super(3);
	}

	public RmiCallbackMessage(List<Object> argsList) {
		this();
		this.argsList = argsList;
	}

	@Override
	public void write() {
		super.write();
		DataUtil.writeList(buffer, argsList);
	}

	@Override
	public void read(ByteBuf buf) {
		this.buffer = buf;
		super.read(buf);
		argsList = DataUtil.readList(buf);
	}

	public List<Object> getArgsList() {
		return this.argsList;
	}

}
