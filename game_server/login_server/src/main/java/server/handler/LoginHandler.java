package server.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DBUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import database.DataQueryResult;
import packet.CocoPacket;
import proto.Common;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import server.LoginServer;
import util.MapObject;
import util.MiscUtil;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LoginHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private Map<String, Integer> tokenMap = new HashMap<>();

    private Object registerLock = new Object();

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        if (s.equals("/fast_login")) {
            handleFastLogin(httpServletRequest, httpServletResponse);
        } else if (s.equals("/user_login")) {
        }
    }


    private class LoginResponse {
        public String gateHost;
        public int gatePort;
        public int userId;
        public String token;
    }

    private void writeResponse(HttpServletResponse response, String result) {
        response.setStatus(HttpStatus.OK_200);
        try {
            response.getWriter().write(result);
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFastLogin(HttpServletRequest request, HttpServletResponse response) {
        logger.info(" handle fast login");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        if (userName == null || userName.isEmpty()) {
            logger.error("empty username ");
            return;
        }
        Map<String, Object> where = new HashMap<>();
        where.put("userName", userName);
        int userId = 0;
        synchronized (registerLock) {
            MapObject user = DataQueryResult.loadSingleResult("accounts", where);
            if (user == null) {
                where.put("password", MiscUtil.getMD5(password));
                try {
                    userId = (int) DBUtil.executeInsert("accounts", where);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                userId = user.getInt("id");
            }
        }
        final int finalUserId = userId;
        String loginToken = MiscUtil.getMD5(System.currentTimeMillis() + userId + "");
        LoginServer.getInst().getClient().sendRequestSync(new CocoPacket(RequestCode.CENTER_DISPATCH_GATE
                , CommonCreator.stringList(userId + ":" + loginToken)), e -> {
            if (e instanceof CocoPacket) {
                byte[] bytes = ((CocoPacket) e).getBytes();
                Common.PBString reply = null;
                try {
                    reply = Common.PBString.parseFrom(bytes);
                } catch (InvalidProtocolBufferException e1) {
                    e1.printStackTrace();
                }
                logger.info("sendCltResult the reply message is " + reply.getValue());
                Gson gson = new Gson();
                LoginResponse res = new LoginResponse();
                res.gateHost = reply.getValue().split(":")[0];
                res.gatePort = Integer.parseInt(reply.getValue().split(":")[1]);
                res.userId = finalUserId;
                res.token = loginToken;
                writeResponse(response, gson.toJson(res));
            }
        });
    }


}
