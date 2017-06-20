package facade.center;

import trans_data.LoginResponseData;
import trans_data.ProtobufData;

/**
 * Created by think on 2017/6/14.
 */
public interface ILoginFacade {
    String registerLoginInfo2Center(int userId, String token);


    LoginResponseData playerLoginGame(int userId, String token);

    //这个架构慢慢的优化，暂时 还是一个雏形
    void playerCreateRole(int userId, int gender, int name);

    void pushMessage2Client(int playerId, ProtobufData data);

}
