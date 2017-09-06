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
		if (!CenterServer.getInst().isRankReward()) {
			logger.info(" the rank reward is not open ");
			return;
		}
		if (rankItems == null) {
			freshRankList(false);
		}
		player.write(ResponseCode.LOBBY_RANK_INFO, LobbyCreator.createPBRankList(player, rankItems));
	}


	public void freshRankList(boolean sendReward) {
		if (!CenterServer.getInst().isRankReward()) {
			return;
		}
		int currentTime = MiscUtil.getCurrentSeconds();
		List<RankItem> list = new ArrayList<>();
		List<ASObject> objects = DataQueryResult.load("select a.phone_num,b.nickname,b.player_id,b.score from player as b, accounts as a where a.user_id = b.user_id order by score desc limit 100");
		if (objects != null) {
			for (ASObject o : objects) {
				list.add(new RankItem(o));
			}
		}
		for (RankItem item : list) {
			Player player = PlayerManager.getInstance().getPlayerById(item.getId());
			if (player == null) {
				continue;
			}
			item.setScore(player.getScore());
		}
		Collections.sort(list, (e, f) -> f.getScore() - e.getScore());
		rankItems = list.subList(0, list.size() > 100 ? 100 : list.size());
		for (int i = 0, size = rankItems.size(); i < size; i++) {
			rankItems.get(i).setRank(i + 1);
		}

		if (sendReward) {
			sendRewardToPlayer(currentTime);
			Map<String, Object> data = new HashMap<>();
			data.put("score", 0);
			data.put("recharge_score", 0);
			try {
				DBUtil.executeUpdate("player", null, data);
				PlayerManager.getInstance().getAllPlayers().forEach(e -> e.resetScore());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

//	public int geneOfflineMailId(int playerId) {
//		Map<String, Object> where = new HashMap<>();
//		where.put("player_id", playerId);
//		List<ASObject> mailList = DataQueryResult.load("mail", where);
//		for (int i = 1; i <= 1000; i++) {
//			if (mailIdValid(mailList, i)) {
//				return i;
//			}
//		}
//		return -1;
//	}
//
//	private boolean mailIdValid(List<ASObject> list, int id) {
//		for (ASObject o : list) {
//			if (o.getInt("id") == id) {
//				return false;
//			}
//		}
//		return true;
//	}

	public void sendRewardToPlayer(int time) {
		for (RankItem item : rankItems) {
			int rewardCoin = RankInfoProvider.getInst().getRankReward(item.getRank());
			MailEntity mail = null;
			int winScore = 0;
			int rechargeScore = 0;
			Player player = PlayerManager.getInstance().getPlayerById(item.getId());
//			boolean isOnline = player != null;
//			if (!isOnline) {
//				player = PlayerLoader.getOfflineInfo(item.getId());
//			}
			try {
				ChannelConfig conf = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(),
						player.getPackageId(), player.isReview());
				if (conf == null) {
					logger.info("conf is null channel_id:" + player.getChannelId() + "  package_id:"
							+ player.getPackageId());
					continue;
				}
				if (!conf.isRank()) {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			if (!isOnline) {
//				Map<String, Object> where = new HashMap<>();
//				where.put("player_id", item.getId());
//				List<ASObject> list = DataQueryResult.load("player", where);
//				if (list.size() > 0) {
//					winScore = list.get(0).getInt("win_score");
//					rechargeScore = list.get(0).getInt("recharge_score");
//				}
//				mail = MailEntity.createMail(item.getId(), geneOfflineMailId(item.getId()), 3, rewardCoin, String.valueOf(item.getRank()), String.valueOf(rewardCoin));
//			} else {
				mail = MailEntity.createMail(item.getId(),player.getAvailableMailId(), 3, rewardCoin, String.valueOf(item.getRank()), String.valueOf(rewardCoin));
				player.addMail(mail);
				winScore = player.getWinScore();
				rechargeScore = player.getRechargeScore();
				player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
//			}
			MailEntity.insertMailIntoDataBase(mail);
			Map<String, Object> data = new HashMap<>();
			data.put("time", time);
			data.put("playerId", item.getId());
			data.put("account", item.getAccount());
			data.put("ranking", item.getRank());
			data.put("reward", rewardCoin);
			data.put("winScore", winScore);
			data.put("rechargeScore", rechargeScore);
			try {
				DBUtil.executeInsert("rank_history", data);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}