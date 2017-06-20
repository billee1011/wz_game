package service.facade;

import facade.gate.IGateFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.handler.agent.AgentManager;
import service.handler.agent.CocoAgent;
import trans_data.ProtobufData;

/**
 * Created by think on 2017/6/20.
 */
public class GateFacade implements IGateFacade {
    private Logger logger = LoggerFactory.getLogger(GateFacade.class);

    @Override
    public void pushMsgToClient(int playerId, ProtobufData data) {
        CocoAgent agent = AgentManager.getInst().getAgent(playerId);
        if (agent == null) {
            logger.warn("warning server push msg to client that doesn't exist");
            return;
        }
        agent.writeMessage(data);
    }
}
