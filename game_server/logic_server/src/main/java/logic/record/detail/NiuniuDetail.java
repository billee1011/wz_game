package logic.record.detail;

import logic.poker.PokerCard;
import logic.poker.niuniu.XianPosition;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/28.
 */
public class NiuniuDetail {
	private int type;
	private String banker;
	private int income;
	private int tax;
	private List<OneNiuniuPos> matchs;
	private List<OneNiuniuRecord> records;

	public NiuniuDetail(int type, String banker) {
		this.type = type;
		matchs = new ArrayList<>();
		records = new ArrayList<>();
	}

	public void setBannerIncomeAndTax(int income, int tax) {
		this.income = income;
		this.tax = tax;
	}

	public void addBannerMatch(List<PokerCard> cardList, int cardType, int result) {
		matchs.add(new OneNiuniuPos(0, cardList, cardType, result));
	}

	public void addXianMatch(XianPosition pos,List<PokerCard> cardList, int cardType, int result) {
		matchs.add(new OneNiuniuPos(pos.getValue(), cardList, cardType, result));
	}

	public void addPlayerRecord(String account, int playerId, int bet, int money, int tax, int totalMoney, int totalTax, int channel_id, int package_id, String device, long pre_coin, long last_coin, String ip, Map<Integer, Pair<Integer, Integer>> bet_cast) {
		records.add(new OneNiuniuRecord(account, playerId, 0, bet, money, tax, totalMoney, totalTax, channel_id, package_id, device, pre_coin, last_coin, ip, bet_cast));
	}

	public void updateZhuangRecord(int player_id, int coin, int tax) {
		records.forEach(e -> {
			if(e.getPlayerId() == player_id) {
				e.setMoney(coin);
				e.setTax(tax);
			}
		});
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getBanker() {
		return banker;
	}

	public void setBanker(String banker) {
		this.banker = banker;
	}

	public int getIncome() {
		return income;
	}

	public void setIncome(int income) {
		this.income = income;
	}

	public int getTax() {
		return tax;
	}

	public void setTax(int tax) {
		this.tax = tax;
	}

	public List<OneNiuniuPos> getMatchs() {
		return matchs;
	}

	public void setMatchs(List<OneNiuniuPos> matchs) {
		this.matchs = matchs;
	}

	public List<OneNiuniuRecord> getRecords() {
		return records;
	}

	public void setRecords(List<OneNiuniuRecord> records) {
		this.records = records;
	}
}
