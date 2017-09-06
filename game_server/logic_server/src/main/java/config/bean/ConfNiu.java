package config.bean;

import java.util.Map;

public class ConfNiu {
	/** 系统坐庄初始金币 */
	private int bankCoin;
	/** 上庄条件 */
	private int bannerLimit;
	/** 连庄次数 */
	private int bannerTimes;
	private transient int niuniuWinRate;
	private int niuniu_chip_limit;
	private int niuniu_small_chip;
	private transient Map<Integer, Integer> niuResultMap;

	public int getBankCoin() {
		return bankCoin;
	}

	public void setBankCoin(int bankCoin) {
		this.bankCoin = bankCoin;
	}

	public int getBannerLimit() {
		return bannerLimit;
	}

	public void setBannerLimit(int bannerLimit) {
		this.bannerLimit = bannerLimit;
	}

	public int getBannerTimes() {
		return bannerTimes;
	}

	public void setBannerTimes(int bannerTimes) {
		this.bannerTimes = bannerTimes;
	}

	public int getNiuniuWinRate() {
		return niuniuWinRate;
	}

	public void setNiuniuWinRate(int niuniuWinRate) {
		this.niuniuWinRate = niuniuWinRate;
	}

	public int getNiuniu_chip_limit() {
		return niuniu_chip_limit;
	}

	public void setNiuniu_chip_limit(int niuniu_chip_limit) {
		this.niuniu_chip_limit = niuniu_chip_limit;
	}

	public int getNiuniu_small_chip() {
		return niuniu_small_chip;
	}

	public void setNiuniu_small_chip(int niuniu_small_chip) {
		this.niuniu_small_chip = niuniu_small_chip;
	}

	public Map<Integer, Integer> getNiuResultMap() {
		return niuResultMap;
	}

	public void setNiuResultMap(Map<Integer, Integer> niuResultMap) {
		this.niuResultMap = niuResultMap;
	}

}
