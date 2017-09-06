package logic.record.detail;

import logic.majiong.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/4/28.
 */
public class DdzDetail {
	private int type;
	private List<DdzDetailBase> records;

	public DdzDetail(int type) {
		this.type = type;
		records = new ArrayList<>();
	}

	public void addOneRecord(int playerId, int result, int tax, int channel_id, int package_id, String device, long pre_coin, long last_coin, String ip) {
		DdzDetailBase detail = new DdzDetailBase(playerId, result, tax, channel_id, package_id, device, pre_coin, last_coin, ip);
		records.add(detail);
	}

	public void addOneRecord(int playerId, int result, int tax, long pre_coin, long last_coin, PlayerInfo player_info ) {
		DdzDetailBase detail = new DdzDetailBase(playerId, result, tax, player_info.getChannel_id(), player_info.getPackage_id(), player_info.getDevice(), pre_coin, last_coin, player_info.getIp());
		records.add(detail);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<DdzDetailBase> getRecords() {
		return records;
	}

	public void setRecords(List<DdzDetailBase> records) {
		this.records = records;
	}
}
