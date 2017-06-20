package trans_data;

import com.google.protobuf.MessageLite;
import protocol.s2c.ResponseCode;

/**
 * Created by think on 2017/6/15.
 */
public interface ProtobufData {
    
    int getMessageId();

    byte[] toByteArray();

    MessageLite fromByteArray(byte[] bytes);
}
