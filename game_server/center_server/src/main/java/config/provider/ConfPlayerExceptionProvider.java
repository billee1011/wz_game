package config.provider;

import chr.Player;
import config.JsonUtil;
import config.bean.ConfException;
import database.DBUtil;
import database.DataQueryResult;
import util.ASObject;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        List<ASObject> data_list = DataQueryResult.load("select * from conf_player_exception");
        for (ASObject data_info : data_list) {
            ConfException ce = new ConfException();
            ce.setId(data_info.getInt("id"));
            ce.setMin_coin(data_info.getInt("min_coin"));
            ce.setMax_coin(data_info.getInt("max_coin"));
            ce.setRate_lose_win(data_info.getInt("rate_lose_win") / 1000f);

            conf_exceptioin_map.put(ce.getId(), ce);
        }
        this.conf_exceptioin_map = conf_exceptioin_map;
    }

    public int isPlayerException(Player player) {
        if(null == player) {
            return 0;
        }

        /// 结果比例 = 输的金额 / 赢的金额
        float ret_rate = Math.abs((0 == player.getWin_money()) ? 1 : player.getLose_money() / player.getWin_money());

        for(Map.Entry<Integer, ConfException> entry : conf_exceptioin_map.entrySet()) {
            if(player.getPayAll() >= entry.getValue().getMin_coin() && player.getPay_money() <= entry.getValue().getMax_coin()) {
                /// 充值金额 对应的输赢比例 〈 结果比例
                if(entry.getValue().getRate_lose_win() > ret_rate) {
                     if(player.getExp_level() < entry.getValue().getId()) {
                         player.setExp_level(entry.getValue().getId());
                     }
                    return entry.getValue().getId();
                }
            }
        }
        return 0;
    }

    public void updateException(Player player, int danger_leve) {

        Map<String, Object> map_data = new HashMap<>();
        map_data.put("exp_level", danger_leve);
        Map<String, Object> map_where = new HashMap<>();
        map_where.put("player_id", player.getPlayerId());

        try {
            DBUtil.executeUpdate("player", map_where, map_data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long insertException(Player player, int danger_leve) {

        Map<String, Object> map_data = new HashMap<>();
        map_data.put("danger_leve", danger_leve);
        map_data.put("player_id", player.getPlayerId());
        map_data.put("time", MiscUtil.getCurrentSeconds());
        map_data.put("coin", player.getCoin());
        map_data.put("bank_coin", player.getBankMoney());
        map_data.put("channel_id", player.getChannelId());
        map_data.put("package_id", player.getPackageId());
        map_data.put("device", player.getDevice());
        map_data.put("account_id", player.getAccountId());

        try {
            return DBUtil.executeInsert("player_exception", map_data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
