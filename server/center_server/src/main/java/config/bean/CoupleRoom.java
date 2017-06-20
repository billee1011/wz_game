package config.bean;

import config.IConfParseBean;

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
	private String coinIcon;
	private String btnBg;

	private int minReq;
	private int maxReq;


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

	public void setBtnBg(String btnBg) {
		this.btnBg = btnBg;
	}
}
