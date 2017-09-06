package logic.record.detail;

import util.Pair;

import java.util.Map;

/**
 * Created by Administrator on 2017/2/28.
 */
public class OneNiuniuRecord {
	private String account;
	private int playerId;
	private int identity;
	private int bet;
	private int money;
	private int tax;
	private int totalMoney;
	private int totalTax;
	private int channel_id;
	private int package_id;
	private String device;
	private long pre_coin;
	private long last_coin;
	private String ip;
	private Map<Integer, Pair<Integer, Integer>> map_bet;

	public OneNiuniuRecord(String account, int playerId, int identity, int bet, int money, int tax, int totalMoney, int totalTax, int channel_id, int package_id, String device, long pre_coin, long last_coin, String ip, Map<Integer, Pair<Integer, Integer>> map_bet) {
		this.account = account;
		this.playerId = playerId;
		this.identity = identity;
		this.bet = bet;
		this.money = money;
		this.tax = tax;
		this.totalMoney = totalMoney;
		this.totalTax = totalTax;
		this.channel_id = channel_id;
		this.package_id = package_id;
		this.device = device;
		this.pre_coin = pre_coin;
		this.last_coin = last_coin;
		this.ip = ip;
		this.map_bet = map_bet;
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

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getTax() {
		return tax;
	}

	public void setTax(int tax) {
		this.tax = tax;
	}

	public int getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}

	public int getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(int totalTax) {
		this.totalTax = totalTax;
	}
}
