package logic.record.detail;

/**
 * Created by Administrator on 2017/2/28.
 */
public class OnePosDetail {
	private String account;
	private int playerId;
	private int wind;
	private int result;
	private int tax;
	private int channel_id;
	private int package_id;
	private String device;
	private long pre_coin;
	private long last_coin;
	private String ip;

	public OnePosDetail(int playerId, int wind, int result, int tax, int channel_id, int package_id, String device, long pre_coin, long last_coin, String ip) {
		this.playerId = playerId;
		this.wind = wind;
		this.result = result;
		this.tax = tax;
		this.account = "";
		this.channel_id = channel_id;
		this.package_id = package_id;
		this.device = device;
		this.pre_coin = pre_coin;
		this.last_coin = last_coin;
		this.ip = ip;
	}

	public long getPre_coin() {
		return pre_coin;
	}

	public void setPre_coin(long pre_coin) {
		this.pre_coin = pre_coin;
	}

	public long getLast_coin() {
		return last_coin;
	}

	public void setLast_coin(long last_coin) {
		this.last_coin = last_coin;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getChannel_id() {
		return channel_id;
	}

	public void setChannel_id(int channel_id) {
		this.channel_id = channel_id;
	}

	public int getPackage_id() {
		return package_id;
	}

	public void setPackage_id(int package_id) {
		this.package_id = package_id;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getWind() {
		return wind;
	}

	public void setWind(int wind) {
		this.wind = wind;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getTax() {
		return tax;
	}

	public void setTax(int tax) {
		this.tax = tax;
	}
}
