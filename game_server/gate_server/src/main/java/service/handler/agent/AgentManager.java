package service.handler.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.CocoPacket;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.GateApp;
import util.NettyUtil;

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

    private Map<Long, CocoAgent> agentMap = new ConcurrentHashMap<>();

    public void registerAgent(CocoAgent agent) {
        agentMap.put(agent.getPlayerId(), agent);
        synGateFactor();
    }

    private void synGateFactor() {
        int factor = agentMap.size();
//		GateApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_GATE_FACTOR, CommonCreator.createPBInt32(factor), 1));
    }

    public void removeAgent(long id) {
//		CocoAgent agent = agentMap.get(id);
//		if (agent == null) {
//			logger.debug(" can't find the agent and the player id is {}", id);
//			return;
//		}
//		agent.closeAgent();
        agentMap.remove(id);
        synGateFactor();
    }

    public void kickAgent(long id) {
        CocoAgent agent = agentMap.remove(id);
        if (agent == null) {
            logger.debug(" can't find the agent and the player id is {}", id);
            return;
        }
        //避免此连接断开给center发送logout
        NettyUtil.setAttribute(agent.getCtx(), "agent", null);

        agent.writeMessage(ResponseCode.ACCOUNT_LOGIN_OTHER_WHERE.getValue(), null);
        agent.closeAgent();

        synGateFactor();
    }

    public CocoAgent getCocoAgent(int playerId) {
        return agentMap.get(playerId);
    }

    public void closeAgent(long playerId) {
        CocoAgent agent = agentMap.get(playerId);
        if (agent == null) {
            logger.debug(" can't find the agent and the player id is {}", playerId);
            return;
        }
        agent.closeAgent();
    }

    public void closeAll() {
        getAllAgents().forEach(e -> {
            if (e != null) {
                e.closeAgent();
            }
        });
    }


    public List<CocoAgent> getAllAgents() {
        return new ArrayList<>(agentMap.values());
    }


    public void writeMessage(CocoPacket packet) {
        CocoAgent agent = agentMap.get(packet.getPlayerId());
        if (agent == null) {
            logger.debug(" can't find the agent and the player id is {}", packet.getPlayerId());
            return;
        }
        agent.writeMessage(packet.getReqId(), packet.getBytes());
    }

}
