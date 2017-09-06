package rank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import chr.PlayerManager;
import config.bean.ChannelConfig;
import config.provider.ChannelInfoProvider;
import config.provider.RankInfoProvider;
import database.DBUtil;
import database.DataQueryResult;
import mail.MailEntity;
import protobuf.creator.LobbyCreator;
import protobuf.creator.MailCreator;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.MiscUtil;

/**
 * Created by Administrator on 2016/11/28.
 */
public class RankManager {
	private static Logger logger = LoggerFactory.getLogger(RankManager.class);
	private static RankManager instance = new RankManager();

	private RankManager() {

	}

	public static RankManager getInst() {
		return instance;
	}

	private static final int COUNT = 100;


	private List<RankItem> rankItems = null;

	public void getRankItemList(Player player) {

	}


	public void freshRankList(boolean sendReward) {

	}

}