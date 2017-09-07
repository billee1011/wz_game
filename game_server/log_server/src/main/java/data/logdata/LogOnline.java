package data.logdata;

import com.google.protobuf.MessageLite;
import database.DBUtil;
import network.AbstractHandlers;
import proto.Log;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/3/21.
 */
public class LogOnline implements ILogInfo {
	private static int createDate = 0;
	private int id;
	private int playerId;
	private int userId;
	private int action;
	private int time;
	private String ip;
	private String province;
	private String city;
	private int packageId;
	private int channelId;
	private String device;
	private String machineId;

	public LogOnline(int playerId, int action, int time, String ip, String province
			, String city, int packageId, int channelId, String device, int userId, String machineId) {
		this.playerId = playerId;
		this.action = action;
		this.time = time;
		this.ip = ip;
		this.province = province;
		this.city = city;
		this.packageId = packageId;
		this.channelId = channelId;
		this.device = device;
		this.userId = userId;
		this.machineId = machineId;
	}

	public LogOnline() {
	}


	@Override
	public void read(AbstractHandlers.MessageHolder<MessageLite> messageContainer) {
		Log.PBLogonline message = messageContainer.get();
		this.playerId = message.getPlayerId();
		this.packageId = message.getPackageId();
		this.channelId = message.getChannelId();
		this.action = message.getAction();
		this.city = message.getCity();
		this.province = message.getProvince();
		this.ip = message.getIp();
		this.userId = 0;
		this.device = message.getDevice();
		this.time = message.getTime();
		this.machineId = message.getMachineId();
	}

	@Override
	public MessageLite write() {
		Log.PBLogonline.Builder builder = Log.PBLogonline.newBuilder();
		builder.setPackageId(packageId);
		builder.setChannelId(channelId);
		builder.setAction(action);
		builder.setCity(city);
		builder.setProvince(province);
		builder.setIp(ip);
		builder.setTime(time);
		builder.setDevice(device);
		builder.setPlayerId(playerId);
		builder.setMachineId(machineId);
		return builder.build();
	}

	@Override
	public void save() throws SQLException {
		Date now = new Date(System.currentTimeMillis());
		String tableName = getTableName("log_online", time);
		int nowDays = MiscUtil.getDaysOffset(now);
		if (createDate != nowDays) {
			DBUtil.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
					"`id` int(11) NOT NULL auto_increment," +
					"`time` int(11) NOT NULL," +
					"`player_id` int(11) NOT NULL," +
					"`action` int(11) NOT NULL," +
					"`ip` varchar(20) NOT NULL," +
					"`province` varchar(20) NOT NULL," +
					"`city` varchar(20) NOT NULL," +
					"`device` varchar(10) NOT NULL," +
					"`package_id` int(11) NOT NULL," +
					"`channel_id` int(11) NOT NULL," +
					"`user_id` int(11) NOT NULL," +
					"`machineId` varchar(60) NOT NULL," +
					"PRIMARY KEY  (`id`)" +
					") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_estonian_ci;");
			createDate = nowDays;
		}
		// 2. insert
		Map<String, Object> data = new HashMap<>();
		data.put("time", time);
		data.put("player_id", playerId);
		data.put("action", action);
		data.put("ip", ip);
		data.put("province", province);
		data.put("city", city);
		data.put("device", device);
		data.put("package_id", packageId);
		data.put("channel_id", channelId);
		data.put("user_id", userId);
		data.put("machineId", machineId);
		DBUtil.executeInsert(tableName, data);
	}
}
