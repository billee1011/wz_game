package config.provider;

import config.JsonUtil;
import config.bean.ConfException;
import database.DBUtil;
import database.DataQueryResult;
import util.MapObject;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/3/28.
 */
public class ConfPlayerExceptionProvider extends BaseProvider  {
    private static ConfPlayerExceptionProvider inst = new ConfPlayerExceptionProvider();

    private ConfPlayerExceptionProvider() {

    }

    public static ConfPlayerExceptionProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, ConfException> conf_exceptioin_map = null;

    @Override
    protected void initString() {
        confString = JsonUtil.getGson().toJson(conf_exceptioin_map, Map.class);
    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, ConfException> conf_exceptioin_map = new HashMap<>();
        List<MapObject> data_list = DataQueryResult.load("select * from conf_player_exception");
        for (MapObject data_info : data_list) {
            ConfException ce = new ConfException();
            ce.setId(data_info.getInt("id"));
            ce.setMin_coin(data_info.getInt("min_coin"));
            ce.setMax_coin(data_info.getInt("max_coin"));
            ce.setRate_lose_win(data_info.getInt("rate_lose_win") / 1000f);

            conf_exceptioin_map.put(ce.getId(), ce);
        }
        this.conf_exceptioin_map = conf_exceptioin_map;
    }

}
