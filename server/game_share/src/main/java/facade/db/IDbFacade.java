package facade.db;

import trans_data.RoleData;

/**
 * Created by think on 2017/6/16.
 */
public interface IDbFacade {
    RoleData loadRole(int userId);
}
