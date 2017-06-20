package facade.logic;

import trans_data.LoginResultMsg;
import trans_data.RoleData;

/**
 * Created by think on 2017/6/20.
 */
public interface ILogicFacade {
    LoginResultMsg playerLoginGame(RoleData data);
}
