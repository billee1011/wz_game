package server;

import dao.DBManager;
import define.AppId;
import facade.ITest;
import org.eclipse.jetty.server.Server;
import server.handler.LoginHandler;
import service.BaseApp;

import java.sql.SQLException;

/**
 * Created by Administrator on 2017/2/4.
 */
public class LoginServer extends BaseApp {

    private static LoginServer instance = new LoginServer();

    private LoginServer() {

    }

    public static LoginServer getInst() {
        return instance;
    }

    private void initDataBase() {
        DBManager.setProps(props);
        DBManager.setDefaultDatabase("wz_accounts");
        try {
            DBManager.touch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initServer() {
        initDataBase();
        Server server = new Server(10024);
        server.setHandler(new LoginHandler());
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getServerName() {
        return "login_server";
    }

    @Override
    protected void afterStart() {
        ITest test = getCenterRmiClient().getInterface(ITest.class);
        System.out.println("the result is " + test.testAdd(1, 2));
    }


    @Override
    protected AppId getAppId() {
        return AppId.LOGIN;
    }

    public static void main(String[] args) {
        LoginServer.getInst().start();
    }
}
