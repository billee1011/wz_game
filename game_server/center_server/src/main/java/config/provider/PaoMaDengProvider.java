package config.provider;

import config.JsonUtil;
import config.bean.PaoMaDengData;
import database.DataQueryResult;
import util.MapObject;
import util.MiscUtil;

import java.util.*;

/**
 * Created by admin on 2017/3/29.
 */
public class PaoMaDengProvider extends BaseProvider {
    private static PaoMaDengProvider inst = new PaoMaDengProvider();

    private PaoMaDengProvider() {

    }

    public static PaoMaDengProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, PaoMaDengData> pamadeng_map = null;
    private Map<Integer, PaoMaDengData> pamadeng_all_map = null;

    @Override
    protected void initString() {
        confString = JsonUtil.getGson().toJson(pamadeng_map, Map.class);
    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, PaoMaDengData>  pamadeng_map = new LinkedHashMap<>();
    	Map<Integer, PaoMaDengData> pamadeng_all_map = new LinkedHashMap<>();
        List<MapObject> data_list = DataQueryResult.load("select * from conf_marquee ORDER BY `timeTo` DESC");
        for (MapObject data_info : data_list) {
            PaoMaDengData pmdd = new PaoMaDengData();
            pmdd.setId(data_info.getInt("id"));
            pmdd.setTimeFrom(data_info.getString("timeFrom"));
            pmdd.setTimeTo(data_info.getString("timeTo"));
            pmdd.setPlayer_id(data_info.getInt("player_id"));
            pmdd.setContent(data_info.getString("content"));
            pmdd.setDelay(data_info.getInt("delay"));

            pamadeng_map.put(pmdd.getId(), pmdd);
            if(0 == data_info.getInt("player_id")) {
                pamadeng_all_map.put(pmdd.getId(), pmdd);
            }
        }
        
        this.pamadeng_map = pamadeng_map;
        this.pamadeng_all_map = pamadeng_all_map;
    }

    public List<MapObject> getDataAll(int num) {
        int tmp_num = 0;
        List<MapObject> list_object = new ArrayList<>();
        for (Object obj : pamadeng_all_map.keySet()) {
            MapObject object = new MapObject();
            object.put("timeFrom", MiscUtil.getSecondsOfTimeStamp_ex(pamadeng_map.get(obj).getTimeFrom(), "yyyy-MM-dd hh:mm"));
            object.put("timeTo", MiscUtil.getSecondsOfTimeStamp_ex(pamadeng_map.get(obj).getTimeTo(), "yyyy-MM-dd hh:mm"));
            object.put("content", pamadeng_map.get(obj).getContent());
            object.put("delay", pamadeng_map.get(obj).getDelay());
            object.put("id", pamadeng_map.get(obj).getId());
            list_object.add(object);
            tmp_num++;
            if(tmp_num >= num) {
                break;
            }
        }
        return list_object;
    }

    public List<MapObject> getData(int flag, int player_id, int num) {
        int tmp_num = 0;
        List<MapObject> list_object = new ArrayList<>();
        for (Object obj : pamadeng_map.keySet()) {
            /// 取出自己 和 群发
            if(1 == flag && (player_id != pamadeng_map.get(obj).getPlayer_id() && 0 != pamadeng_map.get(obj).getPlayer_id())) {
                continue;
            }

            MapObject object = new MapObject();
            object.put("timeFrom", MiscUtil.getSecondsOfTimeStamp_ex(pamadeng_map.get(obj).getTimeFrom(), "yyyy-MM-dd hh:mm"));
            object.put("timeTo", MiscUtil.getSecondsOfTimeStamp_ex(pamadeng_map.get(obj).getTimeTo(), "yyyy-MM-dd hh:mm"));
            object.put("content", pamadeng_map.get(obj).getContent());
            object.put("delay", pamadeng_map.get(obj).getDelay());
            object.put("id", pamadeng_map.get(obj).getId());
            list_object.add(object);
            tmp_num++;
            if(tmp_num >= num) {
                break;
            }
        }
        return list_object;
    }
}
