package config.bean;

import util.MapObject;

/**
 * Created by Administrator on 2017/3/5.
 */
public class ChannelConfig {
	private int id;
	private String platform_id;
	private int packageId;
	private boolean recharge;
	private boolean exchange;
	private boolean rank;
	private boolean alipay;
	private boolean changePwd;
	private boolean bank;
	private boolean bulletin;
	private boolean customer;
	private boolean privyRoom;
	private String url_share;
	private int inviteMethod;
	private int shareMethod;
	private int broadcastMethod;
	private int qq_icon;
	private String url_qq;
	private long pay_send_mail;
	private String pay_send_mail_content;
	private long pay_exchange;
	private int player_num;
	private int online_limit;
	private int complaint_total;
	private String url_agent_request;
	private boolean is_agent;
	private String pay_method;
	private String cur_version = "1";
	private String review_version;
	private boolean is_agent_charge;
	private boolean is_tranfer_open;
	private String voice_id;
	private String voice_key;
	private String gameTypeLimit;
	private String anno_url;
	private int anno_id;

	public ChannelConfig(MapObject o) {
		this.id = o.getInt("id");
		this.platform_id = o.getString("platform_id");
		this.recharge = o.getBoolean("recharge");
		this.exchange = o.getBoolean("exchange");
		this.rank = o.getBoolean("rank");
		this.alipay = o.getBoolean("alipay");
		this.changePwd = o.getBoolean("changePwd");
		this.bank = o.getBoolean("bank");
		this.privyRoom = o.getBoolean("privyRoom");
		this.customer = o.getBoolean("customer");
		this.bulletin = o.getBoolean("bulletin");
		this.packageId = o.getInt("package_id", 1);
		this.url_share = o.getString("url_share");
		this.inviteMethod = o.getInt("inviteMethod");
		this.shareMethod = o.getInt("shareMethod");
		this.broadcastMethod = o.getInt("broadcastMethod");
		this.qq_icon = o.getInt("qq_icon");
		this.url_qq = o.getString("url_qq");
		this.pay_send_mail = o.getLong("pay_send_mail");
		this.pay_send_mail_content = o.getString("pay_send_mail_content");
		this.pay_exchange = o.getLong("pay_exchange");
		this.player_num = o.getInt("player_num");
		this.online_limit = o.getInt("online_limit");
		this.complaint_total = o.getInt("complaint_total");
		this.url_agent_request = o.getString("url_agent_request");
		this.is_agent = o.getBoolean("is_agent");
		this.pay_method = o.getString("pay_method");
		this.is_agent_charge = o.getBoolean("is_agent_charge");
		this.is_tranfer_open = o.getBoolean("is_tranfer_open");
		this.voice_id = o.getString("voice_id");
		this.voice_key = o.getString("voice_key");
		this.review_version = o.getString("review_version");
		this.cur_version = o.getString("cur_version");
		this.gameTypeLimit = o.getString("gameTypeLimit");
		this.anno_url = o.getString("anno_url");
		this.anno_id = o.getInt("anno_id");
	}

	
	public String getPlatform_id() {
		return platform_id;
	}

	public void setPlatform_id(String platform_id) {
		this.platform_id = platform_id;
	}

	public ChannelConfig() {
	}

	public String getVoice_id() {
		return voice_id;
	}

	public void setVoice_id(String voice_id) {
		this.voice_id = voice_id;
	}

	public String getVoice_key() {
		return voice_key;
	}

	public void setVoice_key(String voice_key) {
		this.voice_key = voice_key;
	}

	public boolean is_tranfer_open() {
		return is_tranfer_open;
	}

	public void setIs_tranfer_open(boolean is_tranfer_open) {
		this.is_tranfer_open = is_tranfer_open;
	}

	public String getPay_method() {
		return pay_method;
	}

	public void setPay_method(String pay_method) {
		this.pay_method = pay_method;
	}

	public boolean is_agent() {
		return is_agent;
	}

	public void setIs_agent(boolean is_agent) {
		this.is_agent = is_agent;
	}

	public String getUrl_agent_request() {
		return url_agent_request;
	}

	public void setUrl_agent_request(String url_agent_request) {
		this.url_agent_request = url_agent_request;
	}

	public long getPay_send_mail() {
		return pay_send_mail;
	}

	public void setPay_send_mail(long pay_send_mail) {
		this.pay_send_mail = pay_send_mail;
	}

	public String getPay_send_mail_content() {
		return pay_send_mail_content;
	}

	public void setPay_send_mail_content(String pay_send_mail_content) {
		this.pay_send_mail_content = pay_send_mail_content;
	}

	public long getPay_exchange() {
		return pay_exchange;
	}

	public void setPay_exchange(long pay_exchange) {
		this.pay_exchange = pay_exchange;
	}

	public int getPlayer_num() {
		return player_num;
	}

	public void setPlayer_num(int player_num) {
		this.player_num = player_num;
	}

	public int getOnline_limit() {
		return online_limit;
	}

	public void setOnline_limit(int online_limit) {
		this.online_limit = online_limit;
	}

	public int getComplaint_total() {
		return complaint_total;
	}

	public void setComplaint_total(int complaint_total) {
		this.complaint_total = complaint_total;
	}

	public int getQq_icon() {
		return qq_icon;
	}

	public void setQq_icon(int qq_icon) {
		this.qq_icon = qq_icon;
	}

	public String getUrl_qq() {
		return url_qq;
	}

	public void setUrl_qq(String url_qq) {
		this.url_qq = url_qq;
	}

	public int getBroadcastMethod() {
		return broadcastMethod;
	}

	public void setBroadcastMethod(int broadcastMethod) {
		this.broadcastMethod = broadcastMethod;
	}

	public int getInviteMethod() {
		return inviteMethod;
	}

	public void setInviteMethod(int inviteMethod) {
		this.inviteMethod = inviteMethod;
	}

	public int getShareMethod() {
		return shareMethod;
	}

	public void setShareMethod(int shareMethod) {
		this.shareMethod = shareMethod;
	}

	public String getUrl_share() {
		return url_share;
	}

	public void setUrl_share(String url_share) {
		this.url_share = url_share;
	}

	public boolean isBulletin() {
		return bulletin;
	}

	public void setBulletin(boolean bulletin) {
		this.bulletin = bulletin;
	}

	public boolean isCustomer() {
		return customer;
	}

	public void setCustomer(boolean customer) {
		this.customer = customer;
	}

	public boolean isPrivyRoom() {
		return privyRoom;
	}

	public void setPrivyRoom(boolean privyRoom) {
		this.privyRoom = privyRoom;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isRecharge() {
		return recharge;
	}

	public void setRecharge(boolean recharge) {
		this.recharge = recharge;
	}

	public boolean isExchange() {
		return exchange;
	}

	public void setExchange(boolean exchange) {
		this.exchange = exchange;
	}

	public boolean isRank() {
		return rank;
	}

	public void setRank(boolean rank) {
		this.rank = rank;
	}

	public boolean isAlipay() {
		return alipay;
	}

	public void setAlipay(boolean alipay) {
		this.alipay = alipay;
	}

	public boolean isChangePwd() {
		return changePwd;
	}

	public void setChangePwd(boolean changePwd) {
		this.changePwd = changePwd;
	}

	public boolean isBank() {
		return bank;
	}

	public void setBank(boolean bank) {
		this.bank = bank;
	}

	public int getPackageId() {
		return packageId;
	}

	public void setPackageId(int packageId) {
		this.packageId = packageId;
	}

	public boolean is_agent_charge() {
		return is_agent_charge;
	}

	public void setIs_agent_charge(boolean is_agent_charge) {
		this.is_agent_charge = is_agent_charge;
	}
	
	public String getCur_version() {
		return cur_version;
	}

	public void setCur_version(String cur_version) {
		this.cur_version = cur_version;
	}

	public String getReview_version() {
		return review_version;
	}

	public void setReview_version(String review_version) {
		this.review_version = review_version;
	}

	public String getGameTypeLimit() {
		return gameTypeLimit;
	}

	public void setGameTypeLimit(String gameTypeLimit) {
		this.gameTypeLimit = gameTypeLimit;
	}

	public String getAnno_url() {
		return anno_url;
	}

	public void setAnno_url(String anno_url) {
		this.anno_url = anno_url;
	}

	public int getAnno_id() {
		return anno_id;
	}

	public void setAnno_id(int anno_id) {
		this.anno_id = anno_id;
	}
	
	
}
