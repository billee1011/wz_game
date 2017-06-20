package server.recource;

import annotation.Path;
import com.google.gson.Gson;
import dao.DBUtil;
import dao.DataObject;
import facade.center.ILoginFacade;
import server.LoginServer;
import sql.IInsertSql;
import sql.define.EWhereType;
import sql.impl.InsertSql;
import sql.impl.SelectSql;
import sql.impl.WhereCondition;
import util.HttpUtil;
import util.MiscUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by think on 2017/5/18.
 */
@Path("/login")
public class FastLoginHandler {

    private static final String ACCOUNT_TABLE_NAME = "accounts";
    private static final String ID_COLUMN_KEY = "id";

    @Path("/fast_login")
    public void testFastLogin(HttpServletRequest request, HttpServletResponse response) {
        SelectSql sql = new SelectSql(ACCOUNT_TABLE_NAME);
        String userName = request.getParameter("userName");
        sql.select(ID_COLUMN_KEY).where(new WhereCondition("username", userName, EWhereType.EQUAL));
        DataObject data = DBUtil.executeSingleQuery(sql);
        LoginResponse res = null;
        if (data == null) {
            res = onUserFirstLogin(userName);
        } else {
            res = new LoginResponse();
            res.userId = data.getInt(ID_COLUMN_KEY);
            res.token = geneToken(res.userId);
        }
        String result = LoginServer.getInst().getFacade(ILoginFacade.class).registerLoginInfo2Center(res.userId, res.token);
        System.out.println(" the result is " + result);
        String[] hostAndPost = result.split(":");
        res.gateHost = hostAndPost[0];
        res.gatePort = Integer.parseInt(hostAndPost[1]);
        HttpUtil.writeResponse(response, new Gson().toJson(res));
    }

    private LoginResponse onUserFirstLogin(String userName) {
        IInsertSql insertSql = new InsertSql(ACCOUNT_TABLE_NAME);
        insertSql.data("username", userName).data("password", "");
        int user_id = DBUtil.executeInsert(insertSql);                //这个时候应该有user_id， 然后拿着user_id过去认证就行了， 我搞不懂我们究竟怎么了
        LoginResponse res = new LoginResponse();
        res.userId = user_id;
        res.token = geneToken(user_id);
        return res;
    }

    private String geneToken(int userId) {
        String tokenOriginalStr = "token:" + userId;
        return MiscUtil.getMD5(tokenOriginalStr);
    }

    private class LoginResponse {
        public String gateHost;
        public int gatePort;
        public int userId;
        public String token;
    }
}
