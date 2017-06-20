package handler.client;

import handler.AbstractRmiMessage;
import io.netty.buffer.ByteBuf;
import util.DataUtil;

/**
 * Created by think on 2017/4/17.
 */
public class RmiResponse extends AbstractRmiMessage {
    private Object result;

    @Override
    public int getSequenceId() {
        return 0;
    }

    public RmiResponse() {
    }

    public RmiResponse(Object result) {
        super(2);
        this.result = result;
    }

    @Override
    public void write() {
        super.write();
        DataUtil.writeObject(buffer, result);
        System.out.println("write response success and the length is " + buffer.readableBytes());
    }

    @Override
    public void read(ByteBuf buf) {
        this.buffer = buf;
        super.read(buf);
        result = DataUtil.readObject(buf);
    }

    public Object getResult() {
        return result;
    }
}
