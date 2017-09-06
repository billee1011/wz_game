package config.provider;

import config.JsonUtil;
import config.bean.AgentInfoData;
import database.DataQueryResult;
import util.ASObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/3/31.
 */
public class AgentInfoProvider extends BaseProvider  {
    private static int rand_num = 8;

    private static AgentInfoProvider inst = new AgentInfoProvider();

    private AgentInfoProvider() {

    }

    public static AgentInfoProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<String, Map<Integer, AgentInfoData>> agent_info_map1 = null;
    private Map<String, Map<Integer, AgentInfoData>> agent_info_map2 = null;
    private Map<String, Map<Integer, AgentInfoData>> agent_info_map3 = null;
    private Map<String, Map<Integer, AgentInfoData>> agent_info_map4 = null;
    private Map<String, Map<Integer, AgentInfoData>> agent_info_map_all = null;
    private Map<String, Map<String, AgentInfoData>> agent_info_for_wx = null;


    @Override
    protected void initString() { }

    @Override
    public void doLoad() {
        getInfo();
        getInfoAll();
    }

    private void getInfo() {
    	Map<String, Map<Integer, AgentInfoData>> agent_info_map1 = new HashMap<>();
        loadDBData("show_status_1", agent_info_map1);

        Map<String, Map<Integer, AgentInfoData>> agent_info_map2 = new HashMap<>();
        loadDBData("show_status_2", agent_info_map2);

        Map<String, Map<Integer, AgentInfoData>> agent_info_map3 = new HashMap<>();
        loadDBData("show_status_3", agent_info_map3);

        Map<String, Map<Integer, AgentInfoData>> agent_info_map4 = new HashMap<>();
        loadDBData("show_status_4", agent_info_map4);
        
        Map<String, Map<Integer, AgentInfoData>> agent_info_map_all = getInfoAll();
        
        this.agent_info_map1 = agent_info_map1;
        this.agent_info_map2 = agent_info_map2;
        this.agent_info_map3 = agent_info_map3;
        this.agent_info_map4 = agent_info_map4;
        this.agent_info_map_all = agent_info_map_all;
    }

    private void loadDBData(String show_status, Map<String, Map<Integer, AgentInfoData>> agent_info_map) {
        List<ASObject> data_list = DataQueryResult.load("select * from agent_info right join " +
                "(select agent_ids as agent_id from agent_show_status where " + show_status + " = 1 LIMIT " + rand_num + ") as tmp_ass on agent_info.agent_id = tmp_ass.agent_id " +
                "where `status` = 1 ");
        for (ASObject data_info : data_list) {
            AgentInfoData aid = new AgentInfoData();
            int agent_id = data_info.getInt("agent_id");
            aid.setAgent_id(agent_id);
            aid.setPlatform_id(data_info.getString("platform_id"));
            aid.setPhone_num(data_info.getString("phone_num"));
            aid.setQq(data_info.getString("qq"));
            aid.setWeixin(data_info.getString("weixin"));
            aid.setAlipay(data_info.getString("alipay"));
            aid.setAgent_name(data_info.getString("agent_name"));
            aid.setPlayer_id(data_info.getInt("player_id"));
            aid.setLevel(data_info.getInt("level"));

			Map<Integer, AgentInfoData> map = agent_info_map.get(aid.getPlatform_id());
			if (map == null) {
				map = new HashMap<>();
			}
			map.put(agent_id, aid);
			agent_info_map.put(aid.getPlatform_id(), map);
        }
    }

    private Map<String, Map<Integer, AgentInfoData>> getInfoAll() {
    	agent_info_for_wx = new HashMap<>();
    	Map<String, Map<Integer, AgentInfoData>> map_all = new HashMap<>();
        List<ASObject> data_list = DataQueryResult.load("select * from agent_info");
        for (ASObject data_info : data_list) {
            AgentInfoData aid = new AgentInfoData();
            int agent_id = data_info.getInt("agent_id");
            aid.setAgent_id(agent_id);
            aid.setPlatform_id(data_info.getString("platform_id"));
            aid.setPhone_num(data_info.getString("phone_num"));
            aid.setQq(data_info.getString("qq"));
            aid.setWeixin(data_info.getString("weixin"));
            aid.setAlipay(data_info.getString("alipay"));
            aid.setAgent_name(data_info.getString("agent_name"));
            aid.setPlayer_id(data_info.getInt("player_id"));
            
            Map<String, AgentInfoData> wxMap = agent_info_for_wx.get(aid.getPlatform_id());
            Map<Integer, AgentInfoData> map = map_all.get(aid.getPlatform_id());
			if (map == null) {
				wxMap = new HashMap<>();
				map = new HashMap<>();
			}
			wxMap.put(aid.getWeixin(), aid);
			agent_info_for_wx.put(aid.getPlatform_id(), wxMap);
			map.put(agent_id, aid);
			map_all.put(aid.getPlatform_id(), map);
        }
        return map_all;
    }

    public String getAgentInfoByPlan(String paltform, int plan) {
        switch (plan) {
            case 1:
            	return agent_info_map1.get(paltform) == null ? "" : JsonUtil.getGson().toJson(agent_info_map1.get(paltform), Map.class);
            case 2:
            	return agent_info_map2.get(paltform) == null ? "" : JsonUtil.getGson().toJson(agent_info_map2.get(paltform), Map.class);
            case 3:
            	return agent_info_map3.get(paltform) == null ? "" : JsonUtil.getGson().toJson(agent_info_map3.get(paltform), Map.class);
            case 4:
            	return agent_info_map4.get(paltform) == null ? "" : JsonUtil.getGson().toJson(agent_info_map4.get(paltform), Map.class);
            default:
            	return "";
        }
    }

	public AgentInfoData getAgentInfoByAgentId(int agent_id, String platformId) {
		Map<Integer, AgentInfoData> map = agent_info_map_all.get(platformId);
		if (map != null) {
			for (Map.Entry<Integer, AgentInfoData> entry : agent_info_map_all.get(platformId).entrySet()) {
				if (entry.getKey() == agent_id) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	public AgentInfoData getAgentInfoPlayerId(int player_id, String platformId) {
		Map<Integer, AgentInfoData> map = agent_info_map_all.get(platformId);
		if (map != null) {
			for (Map.Entry<Integer, AgentInfoData> entry : map.entrySet()) {
				if (entry.getValue().getPlayer_id() == player_id) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	public AgentInfoData getAgentInfoForWx(String wx, String platformId) {
		Map<String, AgentInfoData> map = agent_info_for_wx.get(platformId);
		if (map != null) {
			return map.get(wx);
		}
		return null;
	}

    public void delAgentInfo(int flag, String platform, int agent_id) {
        switch (flag) {
            case 1:
                agent_info_map1.get(platform).remove(agent_id);
                break;
            case 2:
                agent_info_map2.get(platform).remove(agent_id);
                break;
            case 3:
                agent_info_map3.get(platform).remove(agent_id);
                break;
            case 4:
                agent_info_map4.get(platform).remove(agent_id);
                break;
        }
    }
}
