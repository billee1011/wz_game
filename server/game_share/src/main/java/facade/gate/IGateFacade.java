package facade.gate;

import trans_data.ProtobufData;

/**
 * Created by think on 2017/6/20.
 */
public interface IGateFacade {
    void pushMsgToClient(int playerId, ProtobufData data);
}
