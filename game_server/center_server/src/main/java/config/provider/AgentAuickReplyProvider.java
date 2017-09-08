package config.provider;

import config.JsonUtil;
import config.bean.AgentAuickReplyData;
import database.DataQueryResult;
import util.MapObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/4/11.
 */
public class AgentAuickReplyProvider extends BaseProvider {
    private static AgentAuickReplyProvider inst = new AgentAuickReplyProvider();

    private AgentAuickReplyProvider() {

    }

    public static AgentAuickReplyProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, AgentAuickReplyData> agent_info_map = null;

    @Override
    protected void initString() {
    	String temp = JsonUtil.getGson().toJson(agent_info_map, Map.class);
    	confString = temp;
    	temp = null;
    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, AgentAuickReplyData> temp = new HashMap<>();
    	List<MapObject> data_list = DataQueryResult.load("SELECT * FROM agent_quick_reply WHERE selected = 1");
        for (MapObject data_info : data_list) {
            AgentAuickReplyData aar = new AgentAuickReplyData();
            int agent_id = data_info.getInt("agent_id");
            aar.setAgent_id(agent_id);
            aar.setContents(data_info.getString("contents"));
            aar.setSelected(data_info.getBoolean("selected"));

            temp.put(agent_id, aar);
        }
    	
        agent_info_map = temp;
        temp = null;
    }

    public String getAuickReply(int agent_id) {
        AgentAuickReplyData data = agent_info_map.get(agent_id);
        if(null == data) {
            return "";
        }
        return data.getContents();
    }
}
