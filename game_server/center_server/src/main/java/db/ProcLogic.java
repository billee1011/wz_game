package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import actor.CenterActorManager;
import actor.ICallback;
import chr.Player;
import chr.PlayerManager;
import config.bean.ChannelConfig;
import config.provider.ChannelInfoProvider;
import database.DBManager;
import database.DBUtil;
import mail.MailEntity;
import proto.creator.MailCreator;
import protocol.s2c.ResponseCode;
import util.MiscUtil;

/**
 * Created by admin on 2017/3/30.
 */
public class ProcLogic {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProcLogic.class);

	/// 充值功能处理
	public static void procPayMail(int player_id, int amount) {
		Player player = PlayerManager.getInstance().getPlayerById(player_id);
//        if(null != player) {
		player.addPay_total(amount);
		if (0 >= player.getIs_send_pay_mail() && true == isPayOrCondPay(player)) {
			int channel_id = player.getChannelId();
			int packageg_id = player.getPackageId();
			paySendMail(player, ChannelInfoProvider.getInst().getChannelConfig(channel_id, packageg_id, player.isReview()).getPay_send_mail_content(), amount);
			player.setIs_send_pay_mail(1);
		}
//        }
//        else {
//            Object data = DataManager.getInst().getCache().query(player_id);
//            if (data == null) {
//                updateAddPayTotal(player_id, amount);
//            } else {
//                //如果在缓存就要修改缓存数据了
//                CharData charData = (CharData) data;
//                ASObject tmp_obj = charData.getModuleData(DBAction.PLAYER);
//                Object[] objArray = (Object[]) tmp_obj.get("" + player_id);
//                ASObject baseData = (ASObject) objArray[0];
//                baseData.put("coin", baseData.getInt("coin") + amount);
//                //修改这个然后存盘了啊
//                DataManager.getInst().saveModule(player_id, DBAction.PLAYER, tmp_obj);
//            }
//
//        }
	}

	/// 发送邮件
	public static void paySendMail(Player player, String content, int amount) {
		if (0 >= content.length()) {
			return;
		}
		int player_id = player.getPlayerId();
		int mail_id = 0;
//        if(null == player) {
////            mail_id = RankManager.getInst().geneOfflineMailId(player_id);
//        }
//        else {
		mail_id = player.getAvailableMailId();
//        }

		MailEntity mail = MailEntity.createMail(player_id, mail_id, 11, 0, content);
		CenterActorManager.getDbActor(player_id).put(() -> {
			MailEntity.insertMailIntoDataBase(mail);
			return null;
		});
//        if(null != player) {
		player.addMail(mail);
		player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
//        }
	}

	/// 充值是否大于配置状态
	public static boolean isPayOrCondPay(Player player) {
		int channel_id = player.getChannelId();
		int packageg_id = player.getPackageId();
		ChannelConfig channelConfig = ChannelInfoProvider.getInst().getChannelConfig(channel_id, packageg_id, player.isReview());
		if (0 >= channelConfig.getPay_send_mail()) {
			return false;
		}
		return player.getPay_total() >= channelConfig.getPay_send_mail();
	}

	/// 充值累计
	public static void updateAddPayTotal(int player_id, int amount) {
		String sql = "UPDATE player SET pay_total = pay_total + " + amount + " WHERE player_id = " + player_id;

		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = DBManager.getConnection();
			stat = conn.prepareStatement(sql);
			stat.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.close(conn, stat);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// 当玩家的累计充值金额，游戏总局数，在线时长达到一定条件后，客户端开启兑换功能，并向玩家发送邮件进行提示，邮件内容后台可配，开启兑换条件后台可配
	public static boolean isOpenExchange(Player player) {
		/// 判断充值金额是否满足需求
		if (0 == player.getIs_open_exchange() && (true == isPayOrCondPay(player) || true == isGameTotal(player) || true == isOnlineTimeDuration(player))) {
			player.setIs_open_exchange(1);
			return true;
		}
		return false;
	}

	public static boolean isGameTotal(Player player) {
		int channel_id = player.getChannelId();
		int packageg_id = player.getPackageId();
		return player.getGame_total() >= ChannelInfoProvider.getInst().getChannelConfig(channel_id, packageg_id, player.isReview()).getPlayer_num();
	}

	public static boolean isOnlineTimeDuration(Player player) {
		int channel_id = player.getChannelId();
		int packageg_id = player.getPackageId();
		return player.getOnline_time_duration() >= ChannelInfoProvider.getInst().getChannelConfig(channel_id, packageg_id, player.isReview()).getOnline_limit();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// 插入联系代理
	public static void saveAgentMessage(int agent_id, int player_id, String content) {

		Map<String, Object> map_data = new HashMap<>();
		map_data.put("agent_id", agent_id);
		map_data.put("player_id", player_id);
		map_data.put("remark", content);
		map_data.put("time", MiscUtil.getCurrentSeconds());

		try {
			DBUtil.executeInsert("agent_message_info", map_data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// 代理商转账
	public static void agentTransfer(int id, int player_id_out, int player_id_in, int amount, int now, int type, ICallback callBack) {

	}

	public static boolean updateOfflineId(String sql) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = DBManager.getConnection();
			stat = conn.prepareStatement(sql);
			if (1 != stat.executeUpdate()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.close(conn, stat);
		}
		return true;
	}


}
