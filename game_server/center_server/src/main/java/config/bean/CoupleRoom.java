package config.bean;

import java.util.List;

import config.IConfParseBean;
import util.ASObject;

/**
 * Created by Administrator on 2016/12/2.
 */
public class CoupleRoom implements IConfParseBean {
	private int id;
	private int order;
	private int mode;
	private float base;
	private String enterLimit;
	private String limitStr;
	private boolean isDouble;
	private int classify;
	private String coinIcon;
	private String btnBg;
	private String peopleIcon;
	private int minReq;
	private int maxReq;
	private int tax_rate;
	private int status;
	private List<Integer> grabZhuang;
	private List<Integer> bet;

	public CoupleRoom() {

	}

	public CoupleRoom(ASObject o) {
		this.id = o.getInt("id");
		this.base = (float) (o.getInt("lowScore")) / 100;
		this.minReq = o.getInt("startValue");
		this.maxReq = o.getInt("endValue");
		this.limitStr = o.getString("descript");
		this.mode = o.getInt("mode");
		this.order = o.getInt("order1");
		this.btnBg = o.getString("btnBg");
		this.classify = o.getInt("classify");
		this.coinIcon = o.getString("coin_icon");
		this.peopleIcon = o.getString("people_icon");
		this.tax_rate = o.getInt("tax_rate");
		this.status = o.getInt("status");
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMinReq() {
		return minReq;
	}

	public int getMaxReq() {
		return maxReq;
	}

	@Override
	public boolean parse() {
		if (enterLimit.equals("")) {
			return true;
		}
		String[] strs = enterLimit.split(",");
		if (strs.length > 0) {
			minReq = (int) (Float.parseFloat(strs[0]) * 100);
			maxReq = -1;
		}
		if (strs.length > 1) {
			maxReq = (int) (Float.parseFloat(strs[1]) * 100);
		}
		return true;
	}

	public int getTax_rate() {
		return tax_rate;
	}

	public void setTax_rate(int tax_rate) {
		this.tax_rate = tax_rate;
	}

	public int getClassify() {
		return classify;
	}

	public void setClassify(int classify) {
		this.classify = classify;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public float getBase() {
		return base;
	}

	public void setBase(float base) {
		this.base = base;
	}

	public String getEnterLimit() {
		return enterLimit;
	}

	public void setEnterLimit(String enterLimit) {
		this.enterLimit = enterLimit;
	}

	public void setMinReq(int minReq) {
		this.minReq = minReq;
	}

	public void setMaxReq(int maxReq) {
		this.maxReq = maxReq;
	}

	public String getLimitStr() {
		return limitStr;
	}

	public void setLimitStr(String limitStr) {
		this.limitStr = limitStr;
	}

	public boolean isDouble() {
		return isDouble;
	}

	public void setDouble(boolean aDouble) {
		isDouble = aDouble;
	}

	public String getCoinIcon() {
		return coinIcon;
	}

	public void setCoinIcon(String coinIcon) {
		this.coinIcon = coinIcon;
	}

	public String getBtnBg() {
		return btnBg;
	}

	public String getPeopleIcon() {
		return peopleIcon;
	}

	public void setPeopleIcon(String peopleIcon) {
		this.peopleIcon = peopleIcon;
	}

	public void setBtnBg(String btnBg) {
		this.btnBg = btnBg;
	}

	public List<Integer> getGrabZhuang() {
		return grabZhuang;
	}

	public void setGrabZhuang(List<Integer> grabZhuang) {
		this.grabZhuang = grabZhuang;
	}

	public List<Integer> getBet() {
		return bet;
	}

	public void setBet(List<Integer> bet) {
		this.bet = bet;
	}

}
