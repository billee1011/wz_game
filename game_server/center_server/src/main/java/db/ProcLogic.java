package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import actor.CenterActorManager;
import actor.ICallback;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import common.LogHelper;
import config.bean.ChannelConfig;
import config.provider.ChannelInfoProvider;
import data.BankAction;
import database.DBManager;
import database.DBUtil;
import define.AppId;
import define.constant.MessageConst;
import mail.MailEntity;
import network.ServerManager;
import packet.CocoPacket;
import protobuf.creator.CommonCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
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
            if(0 >= player.getIs_send_pay_mail() && true == isPayOrCondPay(player)) {
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
        if(0 >= content.length()) {
            return ;
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
        if(0 >= channelConfig.getPay_send_mail()) {
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
        if(0 == player.getIs_open_exchange() && (true == isPayOrCondPay(player) || true == isGameTotal(player) || true == isOnlineTimeDuration(player))) {
            player.setIs_open_exchange(1);
            return true;
        }
        return  false;
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
    	CenterActorManager.getLogicActor(player_id_out).put(()->{
    		Player player_out = PlayerManager.getInstance().getPlayerById(player_id_out);
    		long pre_bank_coin = 0;
    		String ip = "";
    		long pre_coin = 0;
    		int channel_id = 0;
    		String package_id = "";
    		String device = "";
            if(null != player_out) {
            	if (player_out.getDeskInfo() != null) {
            		updateOfflineId("update agent_pay set status = 2 where id = " + id);
            		logger.error("handleAgentPay 游戏中不能转账   id:" + id + "  player_id_in:" + player_id_in + "  player_id_out:" + player_id_out);
            		return MessageConst.AT_GAME_ROOM_ING_AGENT_PAY;
            	}
                if(player_out.getBankMoney() - amount  >= CenterServer.getInst().getAgentPayRetain() * 100) { // 赠送金币6块 不让转移
                    pre_bank_coin = player_out.getBankMoney();
                    ip = player_out.getIp();
                    pre_coin = player_out.getCoin();
                    channel_id = player_out.getChannelId();
                    package_id = String.valueOf(player_out.getPackageId());
                    device = player_out.getDevice();

                    player_out.updateBankCoin(amount, false);
                    if (type == 2){ // 如果是代理购入  扣钱的玩家为 兑换(代理兑换) 
                    	player_out.addExchange_total_agent(amount);
                    }
					if (now != 0) {  // 游戏内转账的间隔时间
						player_out.setAgentPayTime(now); 
					}
                    PlayerSaver.savePlayer(player_out);
                    logger.info("銀行更新 :玩家 {} 代理商转账， 銀行金币減少 {}，当前銀行总金币数为 {}", player_out.getPlayerId(), amount, player_out.getBankMoney());
                    player_out.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
                            , CommonCreator.createPBString(player_out.getBankMoney()+""));
                }
                else {
                    updateOfflineId("update agent_pay set status = 3 where id = " + id);
                    logger.info("handleAgentPay id:" + id + "  player_id_in:" + player_id_in + "  player_id_out:" + player_id_out + "  amount:" + amount);
                    return MessageConst.TRANSFER_COIN_NOT_ENOUGH;
                }
            } else {
//                Map<String, String> map_data = new HashMap<>();
//                map_data.put("bank_coin", String.valueOf(-1 * amount));
//                ASObject obj = PlayerSaver.offlineSavePlayerData(player_id_out, map_data, "update player set bank_coin = bank_coin - " + amount + " where player_id = " + player_id_out);
//                if(null == obj) {
//                    updateOfflineId("update agent_pay set status = 3 where id = " + id);
//                    logger.info("handleAgentPay id:" + id + "  player_id_in:" + player_id_in + "  player_id_out:" + player_id_out + "  amount:" + amount);
//                    return MessageConst.DB_NOT_OUT_PLAYER;
//                }
//
//                pre_bank_coin = obj.getLong("bank_coin");
//                ip = obj.getString("ip");
//                pre_coin = obj.getLong("coin");
//                channel_id = obj.getInt("channel_id");
//                package_id = String.valueOf(obj.getInt("package_id"));
//                device = obj.getString("device");
            	 return MessageConst.DB_NOT_OUT_PLAYER;
            }
            ServerManager.getInst().getMinLoadSession(AppId.LOG)
                    .sendRequest(new CocoPacket(RequestCode.LOG_BANK
                            , LogHelper.logBankSave_ex(player_id_out, BankAction.AGENT_WITHDRAW.getValue(), amount, pre_coin, pre_bank_coin-amount, pre_bank_coin, ip, channel_id, package_id, device)));
            long tmp_data = pre_bank_coin - amount;
            String sql = "update agent_pay set player_out_last_bank_coin = " +  tmp_data +  ", status = 1 where id = " + id;
            CenterActorManager.getUpdateActor().put(() -> {
                ProcLogic.updateOfflineId(sql);
                return null;
            });
            return -1;
    	}, (e)->{
    		if(e == null){
    			return;
    		}
    		int result = (int) e;
    		if(result != -1){
    			callBack.onResult(result);
    			return;
    		}
    		long pre_bank_coin = 0;
    		String ip = "";
    		long pre_coin = 0;
    		int channel_id = 0;
    		String package_id = "";
    		String device = "";
    		
    		Player player_in = PlayerManager.getInstance().getPlayerById(player_id_in);
            if(null != player_in) {
                pre_bank_coin = player_in.getBankMoney();
                ip = player_in.getIp();
                pre_coin = player_in.getCoin();
                channel_id = player_in.getChannelId();
                package_id = String.valueOf(player_in.getPackageId());
                device = player_in.getDevice();

                player_in.updateBankCoin(amount, true);
                if (type == 1){ // 如果是代理充值  加钱的玩家为 充值(代理充值) 
                	player_in.addPay_money_agent(amount);
                }
                logger.info("銀行更新 :玩家 {} 代理商转账， 銀行金币增加 {}，当前銀行总金币数为 {}", player_in.getPlayerId(), amount, player_in.getBankMoney());
                player_in.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
                        , CommonCreator.createPBString(player_in.getBankMoney()+""));
                player_in.setShowBankMark(1);
                player_in.write(ResponseCode.ACCOUNT_SHOW_BANK_MARK, null); // 转账通知显示红点
                PlayerSaver.savePlayer(player_in);
                
            } else {
//                Map<String, String> map_data = new HashMap<>();
//                map_data.put("bank_coin", String.valueOf(amount));
//                ASObject obj = PlayerSaver.offlineSavePlayerData(player_id_in, map_data, "update player set bank_coin = bank_coin + " + amount + " where player_id = " + player_id_in);
//                if(null == obj) {
//                    logger.info("handleAgentPay id:" + id + "  player_id_in:" + player_id_in + "  player_id_out:" + player_id_out + "  amount:" + amount);
//                    callBack.onResult(MessageConst.DB_NOT_IN_PLAYER);
//                    return;
//                }
//
//                pre_bank_coin = obj.getLong("bank_coin");
//                pre_coin = obj.getLong("coin");
//                ip = obj.getString("ip");
//                channel_id = obj.getInt("channel_id");
//                package_id = String.valueOf(obj.getInt("package_id"));
//                device = obj.getString("device");
            	callBack.onResult(MessageConst.DB_NOT_IN_PLAYER);
            	return;
            }
            ServerManager.getInst().getMinLoadSession(AppId.LOG)
                    .sendRequest(new CocoPacket(RequestCode.LOG_BANK
                            , LogHelper.logBankSave_ex(player_id_in, BankAction.AGENT_SAVE.getValue(), amount, pre_coin, pre_bank_coin+amount, pre_bank_coin, ip, channel_id, package_id, device)));
            long tmp_data = pre_bank_coin + amount;
            String sql1 = "update agent_pay set player_in_last_bank_coin = " +  tmp_data +  ", status = 1 where id = " + id;
            CenterActorManager.getUpdateActor().put(() -> {
                ProcLogic.updateOfflineId(sql1);
                return null;
            });

            if(false == updateOfflineId("update agent_pay set status = 1 where id = " + id)) {
                logger.info("handleAgentPay id:" + id + "  player_id_in:" + player_id_in + "  player_id_out:" + player_id_out + "  amount:" + amount);
                callBack.onResult(MessageConst.NO_TRANSFER_ID);
                return;
            }
            callBack.onResult(200);
    	}, CenterActorManager.getLogicActor(player_id_in));
    }

    public static boolean updateOfflineId(String sql) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sql);
            if(1 != stat.executeUpdate()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, stat);
        }
        return true;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// 渠道当天充值金额
//    @SuppressWarnings("resource")
	public static void updateChannelDayLimit(int channel_id, int amount) {

//        String sqlUpdateCurLimitByTime = "UPDATE channel_manager SET cur_limt = ?, cur_limt_time = UNIX_TIMESTAMP(NOW()) WHERE id=? AND DAYOFYEAR(FROM_UNIXTIME(cur_limt_time, '%Y-%m-%d %H:%i:%S')) <> DAYOFYEAR(NOW());";
        String sqlUpdateCurLimit = "UPDATE channel_manager SET cur_limt = cur_limt + ?, cur_limt_time = UNIX_TIMESTAMP(NOW()) WHERE id=?;";
        String sqlUpdateStatus = "UPDATE channel_manager SET status = 0, status_description = 2 WHERE max_day_pay_limit <= cur_limt AND id=?;";

        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        
        
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sqlUpdateCurLimit);
            stat.setInt(1, amount);
            stat.setInt(2, channel_id);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, stat, rs);
        }

        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sqlUpdateStatus);
            stat.setInt(1, channel_id);
            int ret = stat.executeUpdate();
			if (ret > 0) { // 如果有改动状态  向后台服发送刷新状态请求
				logger.info("通知后台刷新 channel_manager 限额状态");
				CenterActorManager.getHttpActor().put(() -> {
					http.HttpUtil.sendPost(CenterServer.getInst().getBackUrl() + "/channel/channel_manager_notice",
							new HashMap<>(), e -> {
							});
					return null;
				});
			}
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, stat, rs);
        }

//        int ret1 = 0;
//        try {
//            conn = DBManager.getConnection();
//            stat = conn.prepareStatement(sqlUpdateCurLimitByTime);
//            stat.setInt(1, amount);
//            stat.setInt(2, channel_id);
//            int ret = stat.executeUpdate();
//            if (0 >= ret) {
//                stat = conn.prepareStatement(sqlUpdateCurLimitByTime);
//                stat.setInt(1, amount);
//                ret1 = stat.executeUpdate();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            DBManager.close(conn, stat, rs);
//        }
//
//        if(0 >= ret1) {
//            try {
//                conn = DBManager.getConnection();
//                stat = conn.prepareStatement(sqlUpdateCurLimit);
//                stat.setInt(1, amount);
//                stat.setInt(2, channel_id);
//                stat.executeUpdate();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                DBManager.close(conn, stat, rs);
//            }
//
//            try {
//                conn = DBManager.getConnection();
//                stat = conn.prepareStatement(sqlUpdateStatus);
//                stat.setInt(1, channel_id);
//                stat.executeUpdate();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                DBManager.close(conn, stat, rs);
//            }
//        }
    }
}
