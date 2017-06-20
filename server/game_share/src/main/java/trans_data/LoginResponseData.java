package trans_data;

import com.google.protobuf.MessageLite;
import data.BaseData;
import protocol.s2c.ResponseCode;


/**
 * Created by think on 2017/6/15.
 */
public class LoginResponseData extends BaseData implements ProtobufData {
    private LoginResultMsg msg;

    private int code;


    @Override
    public int getMessageId() {
        return code;
    }

    public LoginResultMsg getMsg() {
        return msg;
    }

    public void setMsg(LoginResultMsg msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }


    @Override
    public MessageLite fromByteArray(byte[] bytes) {
        return null;
    }
}
