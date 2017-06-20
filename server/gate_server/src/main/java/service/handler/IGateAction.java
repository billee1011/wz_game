package service.handler;

import com.google.protobuf.MessageLite;
import service.handler.agent.CocoAgent;
import trans_data.ProtobufData;

/**
 * Created by think on 2017/6/16.
 */
public interface IGateAction {
    //异步发送请求

    void action(CocoAgent agent, GateMessageManager.MessageHolder<MessageLite> message);
}
