package config.provider;

import config.bean.AnnouncementData;
import database.DataQueryResult;
import util.MapObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/4/16.
 */
public class AnnouncementProvider extends BaseProvider  {
    private static AnnouncementProvider inst = new AnnouncementProvider();

    private AnnouncementProvider() {

    }

    public static AnnouncementProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, AnnouncementData> announcement_map = null;

    @Override
    protected void initString() {

    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, AnnouncementData> announcement_map = new LinkedHashMap<>();
        List<MapObject> data_list = DataQueryResult.load("select * from announcement");
        for (MapObject data_info : data_list) {
            AnnouncementData ad = new AnnouncementData();
            int id = data_info.getInt("id");
//            ad.setBegin_time(data_info.getInt("begin_time"));
//            ad.setEnd_time(data_info.getInt("end_time"));
            ad.setContent(data_info.getString("content"));
//            ad.setPrior(data_info.getInt("prior"));
//            ad.setStatus(data_info.getInt("status"));
            ad.setType(data_info.getInt("type"));

            announcement_map.put(id, ad);
        }
        
        this.announcement_map  = announcement_map;
    }

    public String getData(int id) {
        String str_ret = "";
        AnnouncementData announcementData = announcement_map.get(id);
        str_ret = announcementData.getContent();
        return str_ret;
    }
}
