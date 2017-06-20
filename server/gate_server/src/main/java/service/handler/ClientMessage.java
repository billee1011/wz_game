package service.handler;

/**
 * Created by think on 2017/6/16.
 */
public class ClientMessage {
    private int code;

    private byte[] data;

    public ClientMessage(int code, byte[] data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
