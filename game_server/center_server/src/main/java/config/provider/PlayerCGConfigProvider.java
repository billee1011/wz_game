package config.provider;

import config.JsonUtil;
import config.bean.PlayerCGConfigData;
import database.DataQueryResult;
import util.ASObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/4/13.
 */
public class PlayerCGConfigProvider extends BaseProvider {
    public static int completion_type_pay = 0;          /// 完成类型 0:充值
    public static int completion_type_round = 1;          /// 完成类型 1:游戏局数

    public static int completion_time_today = 0;          ///  完成时间限制 0:当日
    public static int completion_time_add = 1;          ///  完成时间限制 0:累计

    private static PlayerCGConfigProvider inst = new PlayerCGConfigProvider();

    private PlayerCGConfigProvider() {

    }

    public static PlayerCGConfigProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, PlayerCGConfigData> agent_info_map = null;


    @Override
    protected void initString() {
        confString = JsonUtil.getGson().toJson(agent_info_map, Map.class);
    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, PlayerCGConfigData> agent_info_map = new HashMap<>();
        List<ASObject> data_list = DataQueryResult.load("select * from player_cg_config");
        for (ASObject data_info : data_list) {
            PlayerCGConfigData pCGcd = new PlayerCGConfigData();
            int id = data_info.getInt("id");
            pCGcd.setId(id);
            pCGcd.setCompletion(data_info.getInt("completion"));
            pCGcd.setForward_url(data_info.getString("forward_url"));
            pCGcd.setCompletion_type(data_info.getInt("completion_type"));
            pCGcd.setCompletion_time(data_info.getInt("completion_time"));
            pCGcd.setLimit_detail(data_info.getString("limit_detail"));
            agent_info_map.put(id, pCGcd);
        }
        this.agent_info_map = agent_info_map;
    }

    public List<PlayerCGConfigData> getAll(){
        return new ArrayList<>(agent_info_map.values());
    }
}
