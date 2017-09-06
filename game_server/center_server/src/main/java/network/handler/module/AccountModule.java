package network.handler.module;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import common.LogHelper;
import config.bean.AgentInfoData;
import config.bean.ChannelConfig;
import config.bean.Province;
import config.provider.AgentInfoProvider;
import config.provider.ChannelInfoProvider;
import data.AccountAction;
import data.MoneySubAction;
import database.DBUtil;
import database.DataQueryResult;
import define.AppId;
import define.Gender;
import define.constant.CodeValidConst;
import define.constant.MessageConst;
import io.netty.channel.ChannelHandlerContext;
import logic.name.PlayerNameManager;
import net.sf.json.JSONObject;
import network.AbstractHandlers;
import network.ServerManager;
import packet.CocoPacket;
import protobuf.Account;
import protobuf.Common;
import protobuf.creator.AccountCreator;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.MiscUtil;

/**
 * Created by think on 2017/4/11.
 */
public class AccountModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(AccountModule.class);

	private static AtomicInteger order_id = new AtomicInteger(1);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
	}
	

}
