package data.logdata;

import com.google.protobuf.MessageLite;
import database.DBUtil;
import network.AbstractHandlers;
import protobuf.Log;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/3/21.
 */
public class LogBank implements ILogInfo {
	private static int createDate = 0;
	private int id;
	private int playerId;
	private int time;
	private int action;
	private int amount;
	private long bagCoin;
	private long backCoin;
	private long pre_bank_coin;
	private String ip;
	private int channel_id;
	private String package_id;
	private String device;

	public LogBank() {
	}

	public LogBank(int playerId, int time, int action, int amount, long bagCoin, long backCoin, long pre_bank_coin, String ip, int channel_id, String package_id, String device) {
		this.playerId = playerId;
		this.time = time;
		this.action = action;
		this.amount = amount;
		this.bagCoin = bagCoin;
		this.backCoin = backCoin;
		this.pre_bank_coin = pre_bank_coin;
		this.ip = ip;
		this.channel_id = channel_id;
		this.package_id = package_id;
		this.device = device;
	}

	@Override
	public void read(AbstractHandlers.MessageHolder<MessageLite> messageContainer) {
		Log.PBLogBank message = messageContainer.get();
		this.playerId = message.getPlayerId();
		this.time = message.getTime();
		this.action = message.getAction();
		this.amount = message.getAmount();
		this.backCoin = Long.parseLong(message.getBankCoin());
		this.bagCoin = Long.parseLong(message.getBagCoin());
		this.pre_bank_coin = Long.parseLong(message.getPreBankCoin());
		this.ip = message.getIp();
		this.channel_id = message.getChannelId();
		this.package_id = message.getPackageId();
		this.device = message.getDevice();
	}

	@Override
	public MessageLite write() {
		Log.PBLogBank.Builder builder = Log.PBLogBank.newBuilder();
		builder.setPlayerId(playerId);
		builder.setBagCoin(bagCoin+"");
		builder.setBankCoin(backCoin+"");
		builder.setAction(action);
		builder.setAmount(amount);
		builder.setTime(time);
		builder.setIp(ip);
		builder.setPreBankCoin(pre_bank_coin+"");
		builder.setChannelId(channel_id);
		builder.setPackageId(package_id);
		builder.setDevice(device);
		return builder.build();
	}

	@Override
	public void save() throws SQLException {
		Date now = new Date(System.currentTimeMillis());
		String tableName = getTableName("log_bank", time);
		int nowDays = MiscUtil.getDaysOffset(now);
		if (createDate != nowDays) {
			DBUtil.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
					"`id` int(11) NOT NULL auto_increment," +
					"`time` int(11) NOT NULL," +
					"`player_id` int(11) NOT NULL," +
					"`action` int(11) NOT NULL," +
					"`amount` int(11) NOT NULL," +
					"`bag_coin` bigint(20) NOT NULL," +
					"`bank_coin` bigint(20) NOT NULL," +
					"`pre_bank_coin` bigint(20) NOT NULL," +
					"`ip` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"`channel_id` int(11) NOT NULL," +
					"`package_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"`device` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"PRIMARY KEY  (`id`)" +
					") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_estonian_ci;");
			createDate = nowDays;
		}
		// 2. insert
		Map<String, Object> data = new HashMap<>();
		data.put("time", time);
		data.put("player_id", playerId);
		data.put("action", action);
		data.put("amount", amount);
		data.put("bag_coin", bagCoin);
		data.put("bank_coin", backCoin);
		data.put("ip", ip);
		data.put("pre_bank_coin", pre_bank_coin);
		data.put("channel_id", channel_id);
		data.put("package_id", package_id);
		data.put("device", device);

		DBUtil.executeInsert(tableName, data);
	}
}
