package service.handler;

import base.EntityCreator;
import chr.PlayerLoader;
import chr.PlayerSaver;
import chr.RyCharacter;
import database.DBUtil;
import database.DataQueryResult;
import db.CharData;
import db.DataManager;
import define.EMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import define.AppId;
import io.netty.channel.ChannelHandlerContext;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import proto.Login;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.MapObject;
import util.LogUtil;
import util.Pair;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/7.
 */
public class LogicHandler extends AbstractHandlers {
    private static Logger logger = LoggerFactory.getLogger(LogicHandler.class);

    @Override
    protected void registerAction() {
        registerAction(RequestCode.LOGIC_PLAYER_LOGIN.getValue(), this::actionPlayerLogin, Login.PBLoginReq.getDefaultInstance());
    }

    private void actionPlayerLogin(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
        Login.PBLoginReq req = message.get();
        int userId = req.getUserId();
        Map<String, Object> where = new HashMap<>();
        where.put("user_id", userId);
        MapObject player = DataQueryResult.loadSingleResult("player", where);
        if (player == null) {
            RyCharacter newChar = EntityCreator.createChar("user_id", userId);                                                //插入一些必须的数据吧
            try {
                PlayerSaver.insertPlayer(newChar);
                onPlayerLoginSuccess(client, newChar);
            } catch (SQLException e) {
                logger.error("create character failed ", e);
            }
        } else {
            DataManager.getInst().loadChar(player.getLong("player_id"), (e, f) -> {
                if (e == null || e instanceof Exception) {
                    logger.error(f);
                    return;
                }
                CharData data = (CharData) e;
                RyCharacter ch = PlayerLoader.loadFromCharData(data);
                onPlayerLoginSuccess(client, ch);
            });
        }
    }

    private void onPlayerLoginSuccess(ChannelHandlerContext ctx, RyCharacter ch) {
        ch.setIoSession(ctx);
        ch.write(ResponseCode.ACCOUNT_LOGIN_SUCC, null);
        logger.info("player login success ");
    }

    @Override
    public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
        RequestCode reqCode = packet.getReqCode();
        if (reqCode.getSendTo() != getAppId()) {
            getCenterClient().sendRequest(packet);
        } else {
            Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
            if (messageAndHandler == null) {
                logger.debug(" the mssage hand is null and the req code is {}", reqCode);
            } else {
                IActionHandler handler = messageAndHandler.getRight();
                MessageLite protoType = messageAndHandler.getLeft();
                if (handler != null) {
                    MessageLite message = null;
                    try {
                        message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
                        LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("exception; {}", e);
                    }
                    handler.doAction(client, packet, new MessageHolder<>(message));
                }
            }
        }
    }

    private void sendLogicDeskIsRemove(int playerId) {
        getCenterClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_DESK_IS_REMOVE, null, playerId));
    }

    @Override
    protected AppId getAppId() {
        return AppId.LOGIC;
    }

    @Override
    protected NetClient getCenterClient() {
        return LogicApp.getInst().getClient();
    }
}
