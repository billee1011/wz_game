package db.facade;

import dao.DBUtil;
import dao.DataObject;
import facade.db.IDbFacade;
import sql.define.EWhereType;
import sql.impl.SelectSql;
import trans_data.RoleData;

/**
 * Created by think on 2017/6/16.
 */
public class DbFacade implements IDbFacade {

    private static final String CHARACTER_TABLE_NAME = "wz_characters";

    @Override
    public RoleData loadRole(int userId) {
        SelectSql sql = new SelectSql(CHARACTER_TABLE_NAME);
        DataObject result = DBUtil.executeSingleQuery(sql.select("player_id").where("user_id", userId, EWhereType.EQUAL));
        if (result == null) {
            return null;
        } else {
            RoleData data = new RoleData();
            data.setPlayerId(result.getInt("player_id"));
            data.setGender(1);
            data.setHello("hello world");
            return data;
        }
    }
}
