package config.bean;

/**
 * 区域配置
 * @author User
 *
 */
public class Province {
	
	/**
	 * 区域编号
	 */
	private int id;
	
	/**
	 * 区域名
	 */
	private String provinceName;
	
	/**
	 * 公告url
	 */
	private String annoUrl;
	
	/**
	 * 公告id
	 */
	private int annoId;
	
	/**
	 * 支付地址
	 */
	private String payUrl;
	
//	/**
//	 * 支付密钥
//	 */
//	private String payKey;
	
	/**
	 * 兑换地址
	 */
	private String exchangeUrl;
	
//	/**
//	 * 兑换密钥
//	 */
//	private String exchangeKey;
	
	/**
	 * 账号升级开关
	 */
	private int accountUp;
	
	/**
	 * 支付宝开关
	 */
	private int alipay;
	
	/**
	 * 微信支付开关
	 */
	private int weixin;
	
	/**
	 * 银联开关
	 */
	private int unionpay;
	
	/**
	 * 代理转账开关
	 */
	private int agent;
	
	/**
	 * 兑换开关
	 */
	private int exchange;
	
	/**
	 * 维护状态开关
	 */
	private int maintenance;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getAnnoUrl() {
		return annoUrl;
	}

	public void setAnnoUrl(String annoUrl) {
		this.annoUrl = annoUrl;
	}

	public int getAnnoId() {
		return annoId;
	}

	public void setAnnoId(int annoId) {
		this.annoId = annoId;
	}

	public String getPayUrl() {
		return payUrl;
	}

	public void setPayUrl(String payUrl) {
		this.payUrl = payUrl;
	}

//	public String getPayKey() {
//		return payKey;
//	}
//
//	public void setPayKey(String payKey) {
//		this.payKey = payKey;
//	}

	public String getExchangeUrl() {
		return exchangeUrl;
	}

	public void setExchangeUrl(String exchangeUrl) {
		this.exchangeUrl = exchangeUrl;
	}

//	public String getExchangeKey() {
//		return exchangeKey;
//	}
//
//	public void setExchangeKey(String exchangeKey) {
//		this.exchangeKey = exchangeKey;
//	}

	public boolean getAccountUp() {
		return accountUp == 1;
	}

	public void setAccountUp(int accountUp) {
		this.accountUp = accountUp;
	}

	public boolean getAlipay() {
		return alipay == 1;
	}

	public void setAlipay(int alipay) {
		this.alipay = alipay;
	}

	public boolean getWeixin() {
		return weixin == 1;
	}

	public void setWeixin(int weixin) {
		this.weixin = weixin;
	}

	public boolean getUnionpay() {
		return unionpay == 1;
	}

	public void setUnionpay(int unionpay) {
		this.unionpay = unionpay;
	}

	public boolean getAgent() {
		return agent == 1;
	}

	public void setAgent(int agent) {
		this.agent = agent;
	}

	public boolean getExchange() {
		return exchange == 1;
	}

	public void setExchange(int exchange) {
		this.exchange = exchange;
	}

	public boolean getMaintenance() {
		return maintenance == 1;
	}

	public void setMaintenance(int maintenance) {
		this.maintenance = maintenance;
	}

}
