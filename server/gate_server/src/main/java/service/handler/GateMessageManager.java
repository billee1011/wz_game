package service.handler;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import facade.center.ILoginFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Login;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.GateApp;
import service.handler.agent.CocoAgent;
import trans_data.LoginResponseData;
import util.MiscUtil;
import util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/6/16.
 */
public class GateMessageManager {
    private static final Logger logger = LoggerFactory.getLogger(GateMessageManager.class);

    private static GateMessageManager instance = new GateMessageManager();

    private GateMessageManager() {
        actionMap.clear();
        actionMap.put(RequestCode.LOGIN_REQUEST_LOGIN, MiscUtil.newPair(this::actionRequestLogin, Login.PBLoginReq.getDefaultInstance()));
        actionMap.put(RequestCode.LOGIN_CREATE_ROLE, MiscUtil.newPair(this::actionRequestCreateRole, Login.PBCreateRoleReq.getDefaultInstance()));
    }

    private Map<RequestCode, Pair<IGateAction, MessageLite>> actionMap = new HashMap<>();


    public static GateMessageManager getInst() {
        return instance;
    }

    private void actionRequestCreateRole(CocoAgent agent, MessageHolder<MessageLite> message) {
        Login.PBCreateRoleReq req = message.get();

    }

    private void actionRequestLogin(CocoAgent agent, MessageHolder<MessageLite> message) {
        Login.PBLoginReq req = message.get();

        ILoginFacade facade = GateApp.getInst().getFacade(ILoginFacade.class);
        LoginResponseData data = facade.playerLoginGame(req.getUserId(), req.getToken());           //有时需要同步
        logger.debug("the data is {}", new Gson().toJson(data));
        if (data.getCode() == ResponseCode.LOGIN_IN_VALID_LOGIN.getValue()) {
            //非法登陆
        } else {
            agent.setUserId(req.getUserId());
            agent.writeMessage(data);
        }
    }


    //agent is a session  of client
    public void processClientMessage(CocoAgent agent, ClientMessage message) {
        if (agent.getPlayerId() == 0 && message.getCode() != RequestCode.LOGIN_REQUEST_LOGIN.getValue()) {
            logger.warn(" player valid failed ");
            return;
        }
        Pair<IGateAction, MessageLite> actionHandleMessage = actionMap.get(RequestCode.getByValue(message.getCode()));
        if (actionHandleMessage == null) {
            logger.warn("un handler the code {}", message.getCode());
            return;
        }
        IGateAction handler = actionHandleMessage.getLeft();
        MessageLite protoType = actionHandleMessage.getRight();

        final MessageLite targetMessage;
        try {
            targetMessage = protoType == null ? null : protoType.getParserForType().parseFrom(message.getData());
            handler.action(agent, new MessageHolder<>(targetMessage));
        } catch (InvalidProtocolBufferException e) {
            logger.error("", e);
        }
    }

    class MessageHolder<MessageLite> {
        private MessageLite t;

        public MessageHolder(MessageLite t) {
            this.t = t;
        }

        public <T> T get() {
            return (T) t;
        }
    }
}
