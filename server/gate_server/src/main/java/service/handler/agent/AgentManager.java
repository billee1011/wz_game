package service.handler.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/7.
 */
public class AgentManager {
    private static AgentManager instance = new AgentManager();

    private static Logger logger = LoggerFactory.getLogger(AgentManager.class);

    private AgentManager() {

    }

    public static AgentManager getInst() {
        return instance;
    }

    private Map<Integer, CocoAgent> agentMap = new HashMap<>();


    public void registerAgent(CocoAgent agent) {
        agentMap.put(agent.getPlayerId(), agent);
    }

    public CocoAgent getAgent(int playerId) {
        return agentMap.get(playerId);
    }

    public void removeAgent(int id) {
        CocoAgent agent = agentMap.get(id);
        if (agent == null) {
            logger.debug(" can't find the agent and the player id is {}", id);
            return;
        }
        agent.closeAgent();
        agentMap.remove(id);
    }

//	public void writeMessage(CocoPacket packet) {
//		CocoAgent agent = agentMap.get(packet.getPlayerId());
//		if (agent == null) {
//			logger.debug(" can't find the agent and the player id is {}", packet.getPlayerId());
//			return;
//		}
//		agent.writeMessage(packet.getReqId(), packet.getBytes());
//	}

}
