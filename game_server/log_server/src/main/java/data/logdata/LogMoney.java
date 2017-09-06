package data.logdata;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.MessageLite;

import database.DBUtil;
import network.AbstractHandlers;
import protobuf.Log;
import util.MiscUtil;

/**
 * Created by think on 2017/3/21.
 */
public class LogMoney implements ILogInfo {
	private static int createDate = 0;
	private int id;
	private int playerId;
	private long coin;
	private int action;
	private int subAction;
	private int time;
	private int gameId;
	private long pre_coin;
	private long last_coin;
	private String ip;
	private int channel_id;
	private String package_id;
	private String device;
	private long game_no;

	public LogMoney() {
	}

	public LogMoney(int playerId, long coin, int action, int subAction, int time, int gameId, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		this.playerId = playerId;
		this.coin = coin;
		this.action = action;
		this.subAction = subAction;
		this.time = time;
		this.gameId = gameId;
		this.pre_coin = pre_coin;
		this.last_coin = last_coin;
		this.ip = ip;
		this.channel_id = channel_id;
		this.package_id = package_id;
		this.device = device;
		this.game_no = game_no;
	}

	@Override
	public void read(AbstractHandlers.MessageHolder<MessageLite> messageContainer) {
		Log.PBLogMoney message = messageContainer.get();
		this.playerId = message.getPlayerId();
		this.gameId = message.getGameId();
		this.action = message.getAction();
		this.subAction = message.getSubAction();
		this.time = message.getTime();
		this.coin = Long.valueOf(message.getCoin());
		this.pre_coin = Long.parseLong(message.getPreCoin());
		this.last_coin = Long.parseLong(message.getLastCoin());
		this.ip = message.getIp();
		this.channel_id = message.getChannelId();
		this.package_id = message.getPackageId();
		this.device = message.getDevice();
		this.game_no = Long.parseLong(message.getGameNo());
	}

	@Override
	public MessageLite write() {
		Log.PBLogMoney.Builder builder = Log.PBLogMoney.newBuilder();
		builder.setPlayerId(playerId);
		builder.setAction(action);
		builder.setCoin(coin+"");
		builder.setSubAction(subAction);
		builder.setGameId(gameId);
		builder.setTime(time);
		builder.setPreCoin(pre_coin+"");
		builder.setLastCoin(last_coin+"");
		builder.setIp(ip);
		builder.setChannelId(channel_id);
		builder.setPackageId(package_id);
		builder.setDevice(device);
		builder.setGameNo(game_no+"");
		return builder.build();
	}

	@Override
	public void save() throws SQLException {
		Date now = new Date(System.currentTimeMillis());
		String tableName = getTableName("log_money", time);
		int nowDays = MiscUtil.getDaysOffset(now);
		if (createDate != nowDays) {
			DBUtil.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
					"`id` int(11) NOT NULL auto_increment," +
					"`time` int(11) NOT NULL," +
					"`player_id` int(11) NOT NULL," +
					"`action` int(11) NOT NULL," +
					"`sub_action` int(11) NOT NULL," +
					"`coin` int(11) NOT NULL," +
					"`game_id` int(11) NOT NULL," +
					"`pre_coin` bigint(20) NOT NULL," +
					"`last_coin` bigint(20) NOT NULL," +
					"`ip` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"`channel_id` int(11) NOT NULL," +
					"`package_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"`device` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL," +
					"`game_no` bigint(20)  NOT NULL," +
					"PRIMARY KEY  (`id`)," +
					"KEY `game_id` (`game_id`)," +
					"KEY `player_id` (`player_id`)" +
					") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_estonian_ci;");
			createDate = nowDays;
		}
		// 2. insert
		Map<String, Object> data = new HashMap<>();
		data.put("time", time);
		data.put("player_id", playerId);
		data.put("action", action);
		data.put("sub_action", subAction);
		data.put("coin", coin);
		data.put("game_id", gameId);
		data.put("pre_coin", pre_coin);
		data.put("last_coin", last_coin);
		data.put("ip", ip);
		data.put("channel_id", channel_id);
		data.put("package_id", package_id);
		data.put("device", device);
		data.put("game_no", game_no);

		DBUtil.executeInsert(tableName, data);
	}
}
