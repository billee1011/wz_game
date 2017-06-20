package network.facade;

import com.google.gson.Gson;
import define.AppId;
import facade.center.ILoginFacade;
import facade.db.IDbFacade;
import facade.gate.IGateFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import trans_data.LoginResponseData;
import trans_data.ProtobufData;
import trans_data.RoleData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/6/14.
 */
public class LoginFacade implements ILoginFacade {


    private static final Logger logger = LoggerFactory.getLogger(LoginFacade.class);

    private Map<Integer, String> tokenMap = new HashMap<>();

    @Override
    public String registerLoginInfo2Center(int userId, String token) {
        //玩家如果已经登录了的策略暂时不处理
        tokenMap.put(userId, token);
        //分配一个 gate服务器给玩家进行连接操作   暂时全部都连接一个服务
        return "127.0.0.1:10086";
    }


    //服务器加载所有数据， 但是并不是把所有数据都发给客户端， 做延迟加载还是做什么特别的处理，这个我就不知道了
    @Override
    public LoginResponseData playerLoginGame(int userId, String token) {
        LoginResponseData data = new LoginResponseData();
        if (tokenMap.get(userId).equals(token)) {
            IDbFacade dbFacade = CenterServer.getInst().getDBFacade(IDbFacade.class);
            RoleData roleData = dbFacade.loadRole(userId);
            //数据库里取到玩家的信息， 然后向login服发送登陆 ,login成功则直接登陆游戏
            if (roleData == null) {
                data.setCode(ResponseCode.LOGIN_LOGIN_NO_ROLE.getValue());
                data.setMsg(null);
            } else {
                //发起的登陆请求  暂时不处理这个
            }
            logger.debug("the return role Dtat is {}", new Gson().toJson(roleData));
        } else {
            data.setCode(ResponseCode.LOGIN_IN_VALID_LOGIN.getValue());
        }
        return data;
    }

    @Override
    public void playerCreateRole(int userId, int gender, int name) {
        logger.info("user want create player {} and the gender is {} and the name is {}", userId, gender, name);
    }

    @Override
    public void pushMessage2Client(int playerId, ProtobufData data) {
        //这里管理player => gate 等等相关信息
        IGateFacade facade = CenterServer.getInst().getClientFacade(AppId.GATE.getId(), 1, IGateFacade.class);
        if (facade == null) {
            logger.warn(" the facade is not exist  ");
        }
        facade.pushMsgToClient(playerId, data);
    }
}
