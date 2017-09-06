package logic.record.detail;

import logic.majiong.MJDesk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/28.
 */
public class MjDetail {
	private int type;
	private List<OnePosDetail> records;

	public MjDetail(int type) {
		this.type = type;
		records = new ArrayList<>();
	}

	public void addOneRecord(int playerId, int wind, int result, int tax, int channel_id, int package_id, String device, long pre_coin, long last_coin, String ip) {
		OnePosDetail detail = new OnePosDetail(playerId, wind, result, tax, channel_id, package_id, device, pre_coin, last_coin, ip);
		records.add(detail);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<OnePosDetail> getRecords() {
		return records;
	}

	public void setRecords(List<OnePosDetail> records) {
		this.records = records;
	}
}
