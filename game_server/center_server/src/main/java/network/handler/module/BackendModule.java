package network.handler.module;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import com.google.protobuf.MessageLite;
import config.bean.AgentInfoData;
import config.bean.ChannelConfig;
import config.bean.TransferData;
import config.provider.AgentAuickReplyProvider;
import config.provider.AgentInfoProvider;
import config.provider.AnnouncementProvider;
import config.provider.ChannelInfoProvider;
import config.provider.DynamicPropertiesPublicProvider;
import database.DBUtil;
import db.ProcLogic;
import define.constant.DynamicPublicConst;
import define.constant.MessageConst;
import handle.CenterHandler;
import io.netty.channel.ChannelHandlerContext;
import mail.MailEntity;
import network.AbstractHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.CommonCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/11.
 */
public class BackendModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(BackendModule.class);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
	}


}
