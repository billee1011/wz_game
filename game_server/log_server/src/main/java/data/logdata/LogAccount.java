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
public class LogAccount implements ILogInfo {
	private static int createDate = 0;
	private int id;
	private int userId;
	private int action;
	private String ip;
	private String device;
	private int channel;
	private int packageId;
	private int time;
	private String province;
	private String city;

	public LogAccount() {
	}

	public LogAccount(int userId, int action, String ip, String device, int channel, int packageId, int time, String province, String city) {
		this.userId = userId;
		this.action = action;
		this.ip = ip;
		this.device = device;
		this.channel = channel;
		this.packageId = packageId;
		this.time = time;
		this.province = province;
		this.city = city;
	}

	@Override
	public void read(AbstractHandlers.MessageHolder<MessageLite> messageContainer) {
		Log.PBLogAccount message = messageContainer.get();
		this.userId = message.getUserId();
		this.action = message.getAction();
		this.ip = message.getIp();
		this.device = message.getDevice();
		this.channel = message.getChannel();
		this.packageId = message.getPackageId();
		this.time = message.getTime();
		this.province = message.getProvince();
		this.channel = message.getChannel();
		this.city = message.getCity();
	}

	@Override
	public MessageLite write() {
		Log.PBLogAccount.Builder builder = Log.PBLogAccount.newBuilder();
		builder.setUserId(userId);
		builder.setAction(action);
		builder.setIp(ip);
		builder.setDevice(device);
		builder.setChannel(channel);
		builder.setPackageId(packageId);
		builder.setTime(time);
		builder.setProvince(province);
		builder.setCity(city);
		return builder.build();
	}

	@Override
	public void save() throws SQLException {
		Date now = new Date(System.currentTimeMillis());
		String tableName = getTableName("log_account", time);
		int nowDays = MiscUtil.getDaysOffset(now);
		if (createDate != nowDays) {
			DBUtil.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
					"`id` int(11) NOT NULL auto_increment," +
					"`time` int(11) NOT NULL," +
					"`user_id` int(11) NOT NULL," +
					"`action` int(11) NOT NULL," +
					"`ip` varchar(20) NOT NULL," +
					"`province` varchar(20) NOT NULL," +
					"`city` varchar(20) NOT NULL," +
					"`device` varchar(10) NOT NULL," +
					"`package_id` int(11) NOT NULL," +
					"`channel_id` int(11) NOT NULL," +
					"PRIMARY KEY  (`id`)" +
					") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_estonian_ci;");
			createDate = nowDays;
		}
		// 2. insert
		Map<String, Object> data = new HashMap<>();
		data.put("time", time);
		data.put("user_id", userId);
		data.put("action", action);
		data.put("ip", ip);
		data.put("province", province);
		data.put("city", city);
		data.put("device", device);
		data.put("package_id", packageId);
		data.put("channel_id", channel);
		DBUtil.executeInsert(tableName, data);
	}
}
